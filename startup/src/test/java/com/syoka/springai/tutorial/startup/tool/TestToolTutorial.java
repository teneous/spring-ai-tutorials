package com.syoka.springai.tutorial.startup.tool;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * @author syoka
 * @version ToolTutorial.java, v 0.1 2025-03-31 15:30 syoka
 */
@ExtendWith(SpringExtension.class)
public class TestToolTutorial {

    @Test
    public void testSimpleTool() {
        OpenAiApi openAiApi = OpenAiApi.builder()
                .baseUrl("")
                .apiKey("")
                .build();
        ChatModel chatModel = OpenAiChatModel.builder().openAiApi(openAiApi)
                .defaultOptions(OpenAiChatOptions.builder().model("gpt-4o-mini").temperature(0.2d).build()).build();

        String content = ChatClient.create(chatModel)
                .prompt("明天是多少号")
                .tools(new CurrentDateTimeTools())
                .call()
                .content();
        System.out.println(content);
    }

    @Test
    public void testMultiTool() {
        OpenAiApi openAiApi = OpenAiApi.builder()
                .baseUrl("")
                .apiKey("")
                .build();
        ChatModel chatModel = OpenAiChatModel.builder().openAiApi(openAiApi)
                .defaultOptions(OpenAiChatOptions.builder().model("gpt-4o-mini").temperature(0.2d).build()).build();

        String content = ChatClient.create(chatModel)
                .prompt("为我设置10分钟后的闹钟")
                .tools(new CurrentDateTimeTools())
                .call()
                .content();
        System.out.println(content);
    }
}
