package com.syoka.springai.mcp.server.weather;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;

import com.syoka.springai.mcp.server.weather.model.APIWeatherModel;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

/**
 * 基于中国国家气象局天气预报接口获取数据
 *
 * @author syoka
 * @version WeatherToolService.java, v 0.1 2025-04-03 14:06 syoka
 */
@Service
@Slf4j
public class WeatherToolService {
    @Value("${weather.api.key}")
    private String apiKey;
    @Value("${weather.api.base-url}")
    private String baseUrl;

    private RestClient restClient;

    @PostConstruct
    public void init() {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setSupportedMediaTypes(Arrays.asList(MediaType.TEXT_HTML, MediaType.APPLICATION_JSON));

        log.info("Initializing RestClient with baseUrl: {}", baseUrl);
        
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Content-Type", "application/json; charset=utf-8")
                .defaultHeader("Accept", "application/json")
                .messageConverters(converters -> converters.add(converter))
                .build();
    }

    /**
     * 构建一个假的数据集
     * 北京
     * 上海
     * 成都
     * 新加坡
     * 纽约
     *
     * @param cityName 城市名
     * @return 城市经纬度
     */
    @Tool(description = "查询城市的经纬度")
    public Map<String, String> getCityLatAndLon(@ToolParam(description = "城市名") String cityName) {
        if (cityName == null || cityName.trim().isEmpty()) {
            throw new IllegalArgumentException("城市名不能为空");
        }

        Map<String, String> coordinates = switch (cityName) {
            case "北京" -> Map.of("lat", "39.90", "lng", "116.41");
            case "上海" -> Map.of("lat", "31.23", "lng", "121.47");
            case "成都" -> Map.of("lat", "30.57", "lng", "104.07");
            case "新加坡" -> Map.of("lat", "1.35", "lng", "103.82");
            case "纽约" -> Map.of("lat", "40.71", "lng", "-74.01");
            default -> null;
        };

        if (coordinates == null) {
            throw new IllegalArgumentException("不支持的城市: " + cityName);
        }

        return coordinates;
    }

    /**
     * 构建一个function tools
     *
     * @param lat 纬度
     * @param lon 经度
     */
    // @Tool(description = "基于城市名称获取天气情况")
    // ⚠️这里的方法描述非常重要，如果描述有误，比如这里的"基于城市名称获取天气情况"，那么回调会跳过查询城市经纬度方法，从而直接调用此方法并传入错误参数
    @Tool(description = "基于城市经纬度获取天气情况")
    public APIWeatherModel.Weather getWeatherForecastByCity(
            @ToolParam(description = "纬度值") String lat,
            @ToolParam(description = "经度值") String lon) {

        // 参数验证
        if (lat == null || lon == null || lat.trim().isEmpty() || lon.trim().isEmpty()) {
            throw new IllegalArgumentException("经纬度不能为空");
        }

        try {
            // 验证经纬度格式
            double latitude = Double.parseDouble(lat);
            double longitude = Double.parseDouble(lon);
            if (latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) {
                throw new IllegalArgumentException("经纬度超出有效范围");
            }

            var response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/data/2.5/weather")
                            .queryParam("lat", lat)
                            .queryParam("lon", lon)
                            .queryParam("appid", apiKey)
                            .queryParam("units", "metric")
                            .build())
                    .retrieve()
                    .onStatus(status -> status == HttpStatus.UNAUTHORIZED,
                            (req, resp) -> {
                                throw new RuntimeException("API密钥无效或已过期");
                            })
                    .onStatus(status -> status == HttpStatus.TOO_MANY_REQUESTS,
                            (req, resp) -> {
                                throw new RuntimeException("超出API调用限制");
                            })
                    .onStatus(HttpStatusCode::is4xxClientError,
                            (req, resp) -> {
                                throw new RuntimeException("请求参数错误: " + resp.getStatusText());
                            })
                    .onStatus(HttpStatusCode::is5xxServerError,
                            (request, resp) -> {
                                throw new RuntimeException("天气服务暂时不可用");
                            })
                    .body(APIWeatherModel.class);

            if (Objects.isNull(response) || response.weather().isEmpty()) {
                throw new RuntimeException("未能获取天气数据");
            }
            return response.weather().get(0);
        } catch (NumberFormatException e) {
            log.error("经纬度格式无效");
            throw new IllegalArgumentException("经纬度格式无效");
        } catch (HttpClientErrorException e) {
            log.error("请求天气API失败: {}", e.getMessage());
            throw new RuntimeException("获取天气数据失败: " + e.getMessage());
        } catch (HttpServerErrorException e) {
            log.error("天气服务器错误: {}", e.getMessage());
            throw new RuntimeException("天气服务暂时不可用");
        } catch (Exception e) {
            log.error("获取天气数据时发生未知错误: {}", e.getMessage());
            throw new RuntimeException("获取天气数据失败，请稍后重试");
        }
    }

    @Bean
    public ToolCallbackProvider weatherTools(WeatherToolService weatherToolService) {
        return MethodToolCallbackProvider.builder().toolObjects(weatherToolService).build();
    }
}
