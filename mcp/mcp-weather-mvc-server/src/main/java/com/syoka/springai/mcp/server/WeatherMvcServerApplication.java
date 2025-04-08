package com.syoka.springai.mcp.server;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.syoka.springai.mcp.server.weather.WeatherToolService;

@SpringBootApplication
public class WeatherMvcServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(WeatherMvcServerApplication.class, args);
    }

    @Bean
    public ToolCallbackProvider weatherTools(WeatherToolService weatherToolService) {
        return MethodToolCallbackProvider.builder().toolObjects(weatherToolService).build();
    }
}
