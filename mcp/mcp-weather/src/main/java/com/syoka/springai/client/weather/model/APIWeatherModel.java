package com.syoka.springai.client.weather.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;


/**
 * 代表天气 API 响应的数据结构。
 *
 * @param coord      坐标信息（经纬度）
 * @param weather    天气情况列表
 * @param base       内部参数
 * @param main       主要天气数据（温度、气压等）
 * @param visibility 可见度（单位：米，最大值为 10,000 米）
 * @param wind       风力信息
 * @param rain       降水信息（可选）
 * @param clouds     云层覆盖信息
 * @param dt         数据计算时间（Unix 时间戳，UTC 时间）
 * @param sys        太阳升降信息及国家信息
 * @param timezone   与 UTC 的时差（单位：秒）
 * @param id         城市 ID
 * @param name       城市名称
 * @param cod        API 响应状态码
 * @author syoka
 * @version APIWeatherModel.java, v 0.1 2025-04-03 16:50 syoka
 */
public record APIWeatherModel(
        Coord coord,
        List<Weather> weather,
        String base,
        Main main,
        int visibility,
        Wind wind,
        Rain rain,
        Clouds clouds,
        long dt,
        Sys sys,
        int timezone,
        int id,
        String name,
        int cod
) {

    /**
     * 表示地理坐标（经纬度）。
     *
     * @param lon 经度
     * @param lat 纬度
     */
    public record Coord(double lon, double lat) {
    }

    /**
     * 代表天气信息，包括天气类型、描述和图标等。
     *
     * @param id          天气条件 ID
     * @param main        天气主要类别（如 "Rain", "Snow", "Clouds"）
     * @param description 天气情况描述（如 "moderate rain"）
     * @param icon        天气图标 ID
     */
    public record Weather(int id, String main, String description, String icon) {
    }

    /**
     * 主要天气参数，如温度、湿度和气压等。
     *
     * @param temp      当前温度（单位：开尔文）
     * @param feelsLike 体感温度
     * @param tempMin   最低温度
     * @param tempMax   最高温度
     * @param pressure  气压（单位：hPa）
     * @param humidity  湿度（单位：%）
     * @param seaLevel  海平面气压（单位：hPa）
     * @param grndLevel 地面气压（单位：hPa）
     */
    public record Main(
            double temp,
            @JsonProperty("feels_like") double feelsLike,
            @JsonProperty("temp_min") double tempMin,
            @JsonProperty("temp_max") double tempMax,
            int pressure,
            int humidity,
            @JsonProperty("sea_level") int seaLevel,
            @JsonProperty("grnd_level") int grndLevel
    ) {
    }

    /**
     * 代表风力信息，如风速、风向和阵风。
     *
     * @param speed 风速（单位：米/秒）
     * @param deg   风向（单位：度，气象角度）
     * @param gust  阵风风速（单位：米/秒）
     */
    public record Wind(double speed, int deg, double gust) {
    }

    /**
     * 代表降水信息，表示过去 1 小时内的降水量（单位：毫米）。
     *
     * @param oneHour 过去 1 小时的降水量（单位：mm）
     */
    public record Rain(@JsonProperty("1h") double oneHour) {
    }

    /**
     * 代表云层覆盖信息，表示云的覆盖率（单位：%）。
     *
     * @param all 云层覆盖百分比（0-100%）
     */
    public record Clouds(int all) {
    }

    /**
     * 代表太阳升降时间及国家信息。
     *
     * @param type    内部参数
     * @param id      内部参数
     * @param country 国家代码（如 "CN", "US"）
     * @param sunrise 日出时间（Unix 时间戳，UTC 时间）
     * @param sunset  日落时间（Unix 时间戳，UTC 时间）
     */
    public record Sys(int type, int id, String country, long sunrise, long sunset) {
    }
}