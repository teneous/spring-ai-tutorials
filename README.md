# MCP 天气查询示例项目

本项目展示了如何使用MCP（Model Control Panel）框架构建一个天气查询服务，支持WebMVC和WebFlux两种模式，包含服务端和客户端两个组件。项目使用Spring AI进行AI模型集成，并实现了函数调用（Function Calling）功能。

## 项目结构

```
mcp/
├── mcp-mvc-weather/    # 基于WebMVC的天气服务端
└── mcp-webflux-weather/ # 基于WebFlux的天气服务端
```

## 环境要求

- Java 17+
- Spring Boot 3.x
- Spring AI
- OpenAI API Key（用于AI模型调用）
- OpenWeather API Key（用于天气数据查询） https://api.openweathermap.org

## 快速开始

### 1. 启动服务端

选择以下任一方式启动服务端：

#### WebMVC模式
```bash
cd mcp-mvc-weather
./mvnw spring-boot:run
```

#### WebFlux模式
```bash
cd mcp-webflux-weather
./mvnw spring-boot:run
```

服务端默认运行在 `http://localhost:8081`

### 2. 客户端调用方式

#### WebMVC模式（SSE）
```javascript
// 使用JavaScript EventSource
const eventSource = new EventSource('http://localhost:8081/mcp/messages');

eventSource.onmessage = function(event) {
    console.log('收到消息:', event.data);
};

eventSource.onerror = function(error) {
    console.error('错误:', error);
    eventSource.close();
};
```

#### WebFlux模式（SSE）
```javascript
// 使用JavaScript EventSource
const eventSource = new EventSource('http://localhost:8081/mcp/messages');

eventSource.onmessage = function(event) {
    console.log('收到消息:', event.data);
};

eventSource.onerror = function(error) {
    console.error('错误:', error);
    eventSource.close();
};

// 或者使用curl命令行
curl -N http://localhost:8081/mcp/messages
```

#### 使用Spring WebClient（WebFlux）
```java
WebClient client = WebClient.create("http://localhost:8081");
client.get()
    .uri("/mcp/messages")
    .accept(MediaType.TEXT_EVENT_STREAM)
    .retrieve()
    .bodyToFlux(String.class)
    .subscribe(message -> {
        System.out.println("收到消息: " + message);
    });
```

### 3. 业务执行流程

```markdown
客户端启动后会自动发起天气查询请求，执行以下步骤：
1. 先调用城市经纬度查询工具获取目标城市的地理坐标
2. 使用获取到的经纬度调用天气查询工具获取天气信息
```

## 配置说明

### WebMVC服务端配置

在 `application.yml` 中配置：

```yaml
spring:
  main:
    web-application-type: servlet
  ai:
    mcp:
      server:
        type: SYNC
        sse-message-endpoint: /mcp/messages
```

### WebFlux服务端配置

在 `application.yml` 中配置：

```yaml
spring:
  main:
    web-application-type: reactive
  ai:
    mcp:
      server:
        type: ASYNC
        sse-message-endpoint: /mcp/messages
```

### 通用配置

```yaml
# OpenWeather API配置
weather:
  api:
    key: your-openweather-api-key
    base-url: https://api.openweathermap.org
```

## 工具说明

1. 城市经纬度查询工具`WeatherToolService#getCityLatAndLon`
   - 功能：查询指定城市的经纬度信息
   - 输入：城市名称
   - 输出：经度和纬度

2. 天气查询工具`WeatherToolService#getWeatherForecastByCity`
   - 功能：基于经纬度获取天气信息
   - 输入：经度和纬度
   - 输出：天气描述、温度等信息

## 注意事项

1. 确保使用支持Function Calling功能的AI模型（如GPT-4）
2. WebMVC和WebFlux模式的主要区别：
   - WebMVC：同步处理，适合传统的请求-响应模式
   - WebFlux：异步非阻塞，适合高并发场景
3. SSE连接注意事项：
   - 确保服务端配置了正确的CORS策略
   - WebFlux模式支持背压（backpressure）
   - 客户端需要妥善处理重连逻辑
4. 检查API密钥配置是否正确
5. <mark>确保网络环境能够访问OpenAI和OpenWeather API</mark>

## 常见问题

1. Q: SSE连接报404错误？
   A: 检查配置文件中的`web-application-type`和`sse-message-endpoint`是否正确。

2. Q: WebFlux模式下SSE连接断开？
   A: 实现重连机制，设置适当的超时时间。

3. Q: 为什么工具调用顺序不正确？
   A: 确保工具描述准确，并使用支持Function Calling的模型。

4. Q: 获取不到天气数据？
   A: 检查OpenWeather API Key是否配置正确，网络是否正常。

## 示例请求

```java
// 发送天气查询请求
String prompt = "查询成都的天气情况";
// MCP客户端会自动处理以下流程：
// 1. 调用getCityLatAndLon("成都")获取经纬度
// 2. 调用getWeatherForecastByCity(lat, lon)获取天气
```

## 许可证

MIT License 