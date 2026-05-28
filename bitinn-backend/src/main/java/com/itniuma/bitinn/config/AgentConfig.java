package com.itniuma.bitinn.config;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.itniuma.bitinn.tool.EmailTool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * ReAct Agent 配置
 *
 * 专业模式（Professional Mode）的核心配置。
 * 普通模式直接用 ChatClient 调模型，专业模式则通过 ReAct Agent 框架，
 * 让 AI 具备"思考 → 行动 → 观察"的循环推理能力，可以自主调用工具（如发邮件）。
 *
 * ReAct = Reasoning（推理） + Acting（行动）
 * Agent 会在回答前先思考需要什么信息，然后调用工具获取，再基于结果继续推理。
 *
 * @author aceFelix
 *
 * 目前注册的工具：
 *   - EmailTool：发送邮件
 *   后续可扩展：搜索工具、数据库查询工具、代码执行工具等
 */
@Configuration
public class AgentConfig {

    /**
     * 系统提示词文件路径
     * 位于 resources/systemprompt/LilB.txt，定义了 AI 助手的角色、行为规范和回复风格
     */
    @Value("classpath:systemprompt/LilB.txt")
    private Resource systemPrompt;

    /**
     * 加载系统提示词内容
     *
     * @return 提示词文本字符串，用于注入到 Agent 的 systemPrompt
     *         如果文件读取失败则使用默认提示词作为降级方案
     */
    @Bean
    public String systemPromptContent() {
        try {
            return StreamUtils.copyToString(systemPrompt.getInputStream(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            return "你是 BitInn 开发者社区的 AI 助手，名叫小B。请热心专业地回答用户的技术问题。";
        }
    }

    /**
     * 创建 ReAct Agent 实例
     *
     * Agent 使用的模型是 DashScopeChatModel（阿里云百炼），
     * 模型名由 spring.ai.dashscope.chat.options.model 配置项决定（默认 qwen3.6-plus）。
     * 专业模式目前不支持切换模型，始终使用配置的默认模型。
     *
     * @param dashScopeChatModel    DashScope 聊天模型（阿里云百炼）
     * @param systemPromptContent   系统提示词内容
     * @param emailTool             邮件发送工具
     * @return ReAct Agent 实例，供 ChatAsyncService 在专业模式下调用
     */
    @Bean
    public ReactAgent agent(DashScopeChatModel dashScopeChatModel,
                            String systemPromptContent,
                            EmailTool emailTool) {
        return ReactAgent.builder()
                .name("bitinn_agent")
                .model(dashScopeChatModel)
                .systemPrompt(systemPromptContent)
                .methodTools(emailTool)
                .build();
    }
}
