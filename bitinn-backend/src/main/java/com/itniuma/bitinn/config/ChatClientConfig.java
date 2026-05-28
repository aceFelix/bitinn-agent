package com.itniuma.bitinn.config;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ChatClient Bean 配置
 *
 * ChatClient 是 Spring AI 提供的高层 API，封装了与 AI 模型交互的细节。
 * 普通模式和识图模式均通过此 ChatClient 发起流式对话请求。
 *
 * 注意：ChatClient 本身不绑定模型，模型名在每次请求时通过
 * DashScopeChatOptions 动态传入（见 ModelRouter.buildDashScopeOptions()）。
 *
 * @author aceFelix
 */
@Configuration
public class ChatClientConfig {

    /**
     * 创建基于 DashScope（阿里云百炼）的 ChatClient
     *
     * @param chatModel  DashScopeChatModel 实例，由 spring-ai-alibaba 自动配置
     * @return ChatClient 实例，注入到 ModelRouter 中使用
     */
    @Bean
    public ChatClient chatClient(DashScopeChatModel chatModel) {
        return ChatClient.builder(chatModel).build();
    }
}
