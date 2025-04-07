# MCP 天气查询示例项目

本项目展示了如何使用MCP（Model Control Panel）框架构建一个天气查询服务，包含服务端和客户端两个组件，采用mvc模式。项目使用Spring AI进行AI模型集成，并实现了函数调用（Function Calling）功能。

## 项目结构

```
mcp/
├── mcp-client/        # mcp调用客户端
└── mcp-weather/       # mcp天气服务端
```

## 环境要求

- Java 17+
- Spring Boot 3.x
- Spring AI
- OpenAI API Key（用于AI模型调用）
- OpenWeather API Key（用于天气数据查询） https://api.openweathermap.org

## 快速开始

### 1. 启动服务端

```bash
cd mcp-server
./mvnw spring-boot:run
```

服务端默认运行在 `http://localhost:8080`

### 2. 启动客户端

```bash
cd mcp-weather
./mvnw spring-boot:run
```
### 3. 业务执行流程

```markdown
客户端启动后会自动发起天气查询请求，执行以下步骤：
1. 先调用城市经纬度查询工具获取目标城市的地理坐标
2. 使用获取到的经纬度调用天气查询工具获取天气信息
```

## 配置说明

### 服务端配置

在 `application.properties` 中配置：

```properties
# OpenAI配置
spring.ai.openai.api-key=your-openai-api-key
spring.ai.openai.model=gpt-4o  # 必须使用支持function calling的模型
```

### 客户端配置

在 `application.properties` 中配置：

```properties
# OpenWeather API配置
weather.api.key=your-openweather-api-key
```

## 工具说明

1. 城市经纬度查询工具`com.syoka.springai.client.weather.WeatherToolService#getCityLatAndLon`
   - 功能：查询指定城市的经纬度信息
   - 输入：城市名称
   - 输出：经度和纬度

2. 天气查询工具`com.syoka.springai.client.weather.WeatherToolService#getWeatherForecastByCity`
   - 功能：基于经纬度获取天气信息
   - 输入：经度和纬度
   - 输出：天气描述、温度等信息

## 注意事项

1. 确保使用支持Function Calling功能的AI模型（如GPT-4o）
2. 服务端必须先于客户端启动
3. 检查API密钥配置是否正确
4. 确保网络环境能够访问OpenAI和OpenWeather API

## 常见问题

1. Q: 为什么工具调用顺序不正确？
   A: 确保工具描述准确，并使用支持Function Calling的模型。

2. Q: 获取不到天气数据？
   A: 检查OpenWeather API Key是否配置正确，网络是否正常。

## 示例请求

```java
// 客户端示例代码
String prompt = "查询成都的天气情况";
// MCP客户端会自动处理以下流程：
// 1. 调用getCityLatAndLon("成都")获取经纬度
// 2. 调用getWeatherForecastByCity(lat, lon)获取天气
```

## 许可证

MIT License 