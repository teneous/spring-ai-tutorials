package com.syoka.springai.client.weather;


import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.syoka.springai.client.weather.model.APIWeatherModel;

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
    @Value("spring.application.open-weather-map.api-key")
    private String apiKey;
    @Value("spring.application.open-weather-map.base-url")
    private String baseUrl;

    private final RestClient restClient;


    public WeatherToolService() {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setSupportedMediaTypes(Arrays.asList(MediaType.TEXT_HTML, MediaType.APPLICATION_JSON));

        this.restClient = RestClient.builder().baseUrl(baseUrl)
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
        return switch (cityName) {
            case "北京" -> Map.of("lat", "39.90", "lng", "116.41");
            case "上海" -> Map.of("lat", "31.23", "lng", "121.47");
            case "成都" -> Map.of("lat", "30.57", "lng", "104.07");
            case "新加坡" -> Map.of("lat", "1.35", "lng", "103.82");
            case "纽约" -> Map.of("lat", "40.71", "lng", "-74.01");
            default -> Map.of(); // 或抛出异常、返回null视具体需求
        };
    }

    /**
     * 构建一个function tools
     *
     * @param lat 纬度
     * @param lon 经度
     */
    // @Tool(description = "基于城市名称获取天气情况")
    // ⚠️这里的方法描述非常重要，如果描述有误，比如这里的“基于城市名称获取天气情况”，那么回调会跳过查询城市经纬度方法，从而直接调用此方法并传入错误参数
    @Tool(description = "基于城市经纬度获取天气情况")
    public APIWeatherModel.Weather getWeatherForecastByCity(@ToolParam(description = "纬度值") String lat,
                                                            @ToolParam(description = "经度值") String lon) {
        log.info("[getWeatherForecastByCity] lat: {} lon: {}]", lat, lon);
        var response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/data/2.5/weather")
                        .queryParam("lat", lat)
                        .queryParam("lon", lon)
                        .queryParam("appid", apiKey)
                        .queryParam("units", "metric") // 使用摄氏度
                        .build())
                .retrieve()
                .body(APIWeatherModel.class);

        if (Objects.nonNull(response) && !response.weather().isEmpty()) {
            List<APIWeatherModel.Weather> weather = response.weather();
            return weather.get(0);
        }
        return null;
    }

    @Bean
    public ToolCallbackProvider weatherTools(WeatherToolService weatherToolService) {
        return MethodToolCallbackProvider.builder().toolObjects(weatherToolService).build();
    }
}
