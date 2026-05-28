package com.itniuma.bitinn.service.aichat.impl;

import com.itniuma.bitinn.pojo.mongo.Conversation;
import com.itniuma.bitinn.pojo.mongo.Message;
import com.itniuma.bitinn.repository.ConversationRepository;
import com.itniuma.bitinn.repository.MessageRepository;
import com.itniuma.bitinn.service.aichat.ConversationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 对话管理服务实现
 * 负责对话和消息的 CRUD 操作，基于 MongoDB 存储。
 *
 * @author aceFelix
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConversationServiceImpl implements ConversationService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;

    @Override
    public Conversation createConversation(Integer userId, String title, String model, String mode) {
        Conversation conv = Conversation.builder()
                .userId(userId)
                .title(title != null ? title : "新对话")
                .model(model != null ? model : "qwen3.6-plus")
                .mode(mode != null ? mode : "normal")
                .messageCount(0)
                .isPinned(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        return conversationRepository.save(conv);
    }

    @Override
    public List<Conversation> listConversations(Integer userId) {
        return conversationRepository.findByUserIdOrderByUpdatedAtDesc(userId, PageRequest.of(0, 50));
    }

    @Override
    public void deleteConversation(String id, Integer userId) {
        conversationRepository.findByIdAndUserId(id, userId).ifPresent(conv -> {
            messageRepository.deleteByConversationId(id);
            conversationRepository.delete(conv);
        });
    }

    @Override
    public void saveMessage(Message message) {
        messageRepository.save(message);
    }

    @Override
    public List<Message> getMessages(String conversationId) {
        return messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId, PageRequest.of(0, 100));
    }

    @Override
    public Conversation getConversation(String id) {
        return conversationRepository.findById(id).orElse(null);
    }
}
