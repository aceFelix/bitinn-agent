package com.itniuma.bitinn.service.ai;

import com.itniuma.bitinn.enums.SSEMessageType;
import com.itniuma.bitinn.pojo.mongo.Conversation;
import com.itniuma.bitinn.pojo.mongo.Message;
import com.itniuma.bitinn.repository.ConversationRepository;
import com.itniuma.bitinn.repository.MessageRepository;
import com.itniuma.bitinn.utils.SSEServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 对话标题服务
 *
 * 初始标题直接从用户首条消息截取前 15 字；
 * 第 2、第 5 轮 AI 回复后通过 DeepSeek-V4-Pro 基于上下文重新生成标题。
 *
 * @author aceFelix
 */
@Slf4j
@Service
public class TitleService {

    private static final String TITLE_PROMPT = "根据对话内容生成一个不超过15字的简洁标题。只输出标题，不要引号或任何其他内容。";

    private final ChatClient chatClient;
    private final ChatClient deepSeekClient;
    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;

    public TitleService(ChatClient chatClient,
                        @Qualifier("deepSeekChatModel") ChatModel deepSeekChatModel,
                        MessageRepository messageRepository,
                        ConversationRepository conversationRepository) {
        this.chatClient = chatClient;
        this.deepSeekClient = ChatClient.builder(deepSeekChatModel).build();
        this.messageRepository = messageRepository;
        this.conversationRepository = conversationRepository;
    }

    /**
     * 根据用户首条消息为对话设置初始标题，仅当标题为默认值"新对话"时触发。
     */
    public void setInitialTitle(String conversationId, String userId, String prompt) {
        try {
            Conversation conversation = conversationRepository.findById(conversationId).orElse(null);
            if (conversation == null) return;
            if (!"新对话".equals(conversation.getTitle())) return;

            String title = buildTitleFromPrompt(prompt);

            conversation.setTitle(title);
            conversation.setUpdatedAt(LocalDateTime.now());
            conversationRepository.save(conversation);

            SSEServer.sendMessageWithResult(userId, title, SSEMessageType.TITLE_UPDATE);
            log.info("[Title] 初始: conv={}, title={}", conversationId, title);

        } catch (Exception e) {
            log.error("[Title] 初始失败, conv={}, error={}", conversationId, e.getMessage(), e);
        }
    }

    /**
     * 异步刷新对话标题，在第2、第5轮AI回复后，取最近4条消息通过AI重新生成标题。
     */
    @Async
    public void refreshTitle(String conversationId, String userId) {
        try {
            long assistantCount = messageRepository.countByConversationIdAndRole(conversationId, "assistant");
            if (assistantCount != 2 && assistantCount != 5) return;

            List<Message> recent = messageRepository.findByConversationIdOrderByCreatedAtAsc(
                    conversationId, PageRequest.of(0, 4));

            StringBuilder ctx = new StringBuilder();
            for (Message msg : recent) {
                String role = "user".equals(msg.getRole()) ? "用户" : "AI";
                String c = msg.getContent();
                if (c != null && !c.isEmpty()) {
                    ctx.append(role).append(": ")
                       .append(c.length() > 150 ? c.substring(0, 150) + "..." : c)
                       .append("\n");
                }
            }
            if (ctx.isEmpty()) return;

            String title = deepSeekClient.prompt()
                    .system(TITLE_PROMPT)
                    .user("对话内容：\n" + ctx)
                    .call()
                    .content();

            if (title != null) {
                title = title.trim();
                if (title.length() > 20) title = title.substring(0, 20);
            }

            if (title != null && !title.isEmpty()) {
                Conversation conv = conversationRepository.findById(conversationId).orElse(null);
                if (conv != null) {
                    conv.setTitle(title);
                    conv.setUpdatedAt(LocalDateTime.now());
                    conversationRepository.save(conv);
                    SSEServer.sendMessageWithResult(userId, title, SSEMessageType.TITLE_UPDATE);
                    log.info("[Title] 更新: conv={}, title={}", conversationId, title);
                }
            }
        } catch (Exception e) {
            log.error("[Title] 更新失败, conv={}, error={}", conversationId, e.getMessage());
        }
    }

    /**
     * 从用户首条消息中截取不超过15字作为初始标题。
     */
    private String buildTitleFromPrompt(String prompt) {
        if (prompt == null || prompt.isBlank()) return "新对话";
        String cleaned = prompt.replaceAll("\\s+", " ").replaceAll("[\\r\\n]", "").trim();
        if (cleaned.isEmpty()) return "新对话";
        return cleaned.length() <= 15 ? cleaned : cleaned.substring(0, 15);
    }
}
