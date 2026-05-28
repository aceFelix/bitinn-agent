package com.itniuma.bitinn.service.ai;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.itniuma.bitinn.pojo.mongo.Conversation;
import com.itniuma.bitinn.pojo.mongo.Message;
import com.itniuma.bitinn.repository.ConversationRepository;
import com.itniuma.bitinn.repository.MessageRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 对话摘要服务
 * 当对话消息超过10条时，异步取前一半消息通过 AI 生成上下文摘要，供后续对话上下文压缩使用。
 *
 * @author aceFelix
 */
@Slf4j
@Service
public class SummaryService {

    private static final String SUMMARY_SYSTEM_PROMPT = """
            你是一个对话摘要助手。请将以下对话历史压缩成一段简洁的摘要，保留以下关键信息：
            1. 用户提出的主要问题和需求
            2. AI给出的关键回答和解决方案
            3. 重要的技术细节、代码片段名称、配置项等
            4. 用户表达的偏好和约束条件
            
            要求：
            - 摘要应该简洁但完整，不超过800字
            - 保留具体的技术术语和名称
            - 按时间顺序组织信息
            - 不要遗漏用户的核心诉求
            """;

    private final DashScopeChatModel dashScopeChatModel;
    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;

    public SummaryService(DashScopeChatModel dashScopeChatModel,
                          MessageRepository messageRepository,
                          ConversationRepository conversationRepository) {
        this.dashScopeChatModel = dashScopeChatModel;
        this.messageRepository = messageRepository;
        this.conversationRepository = conversationRepository;
    }

    /**
     * 异步生成对话摘要，消息数不足10条时跳过，取前一半消息压缩为摘要存入会话记录。
     */
    @Async
    public void generateSummaryAsync(String conversationId) {
        try {
            log.info("[Summary] 开始为会话 {} 生成摘要", conversationId);

            List<Message> messages = messageRepository.findByConversationIdOrderByCreatedAtAsc(
                    conversationId, PageRequest.of(0, 100));

            if (messages.size() < 10) {
                log.info("[Summary] 消息数不足10条, 跳过摘要生成");
                return;
            }

            int halfPoint = messages.size() / 2;
            List<Message> olderMessages = messages.subList(0, halfPoint);

            StringBuilder conversationText = new StringBuilder();
            for (Message msg : olderMessages) {
                String role = "user".equals(msg.getRole()) ? "用户" : "AI";
                String content = msg.getContent();
                if (content != null && !content.isEmpty()) {
                    if (content.length() > 500) {
                        content = content.substring(0, 500) + "...[截断]";
                    }
                    conversationText.append(role).append(": ").append(content).append("\n\n");
                }
            }

            if (conversationText.isEmpty()) {
                log.info("[Summary] 无有效内容可摘要");
                return;
            }

            org.springframework.ai.chat.messages.Message systemMsg =
                    new org.springframework.ai.chat.messages.SystemMessage(SUMMARY_SYSTEM_PROMPT);
            org.springframework.ai.chat.messages.Message userMsg =
                    new org.springframework.ai.chat.messages.UserMessage(
                            "请为以下对话生成摘要：\n\n" + conversationText);

            Prompt prompt = new Prompt(List.of(systemMsg, userMsg));
            ChatResponse response = dashScopeChatModel.call(prompt);

            String summary = null;
            if (response.getResult() != null
                    && response.getResult().getOutput() != null
                    && response.getResult().getOutput().getText() != null) {
                summary = response.getResult().getOutput().getText();
            }

            if (summary != null && !summary.isEmpty()) {
                Conversation conversation = conversationRepository.findById(conversationId).orElse(null);
                if (conversation != null) {
                    conversation.setSummary(summary);
                    conversation.setSummaryUpdatedAt(LocalDateTime.now());
                    conversationRepository.save(conversation);
                    log.info("[Summary] 摘要生成完成, 会话={}, 摘要长度={}", conversationId, summary.length());
                }
            } else {
                log.warn("[Summary] AI返回空摘要, 会话={}", conversationId);
            }

        } catch (Exception e) {
            log.error("[Summary] 摘要生成失败, 会话={}, error={}", conversationId, e.getMessage(), e);
        }
    }
}
