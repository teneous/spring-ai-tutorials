package com.syoka.springai.mcp.features;

import java.util.List;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@Slf4j
public class McpFeaturesClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(McpFeaturesClientApplication.class, args);
    }


    @Bean
    public CommandLineRunner predefinedQuestions(ChatClient.Builder chatClientBuilder,
                                                 ToolCallbackProvider provider,
                                                 ConfigurableApplicationContext context,
                                                 List<McpSyncClient> mcpSyncClients) {
        return args -> {
            // find mcp-advance-features-server
            McpSyncClient client = mcpSyncClients.stream().filter(e -> e.getServerInfo().name().equals("mcp-device-server")).findFirst()
                    .orElseThrow(() -> new RuntimeException("No mcp sync client found"));
            // list all resource
            McpSchema.ListResourcesResult remoteResources = client.listResources();
            for (McpSchema.Resource resource : remoteResources.resources()) {
                McpSchema.ReadResourceResult result = client.readResource(resource);
                List<McpSchema.ResourceContents> contents = result.contents();
                for (McpSchema.ResourceContents content : contents) {
                    if (content instanceof McpSchema.TextResourceContents) {
                        String text = ((McpSchema.TextResourceContents) content).text();
                        log.info("[mcp-device-server has resource].text:{}", text);
                    }
                }
            }

            // list all prompt
            McpSchema.ListPromptsResult remotePrompts = client.listPrompts();
            for (McpSchema.Prompt prompt : remotePrompts.prompts()) {
                String name = prompt.name();
                String description = prompt.description();
                log.info("[mcp-device-server has prompt].name:{}. description:{}", name, description);
            }
            context.close();
        };
    }
}