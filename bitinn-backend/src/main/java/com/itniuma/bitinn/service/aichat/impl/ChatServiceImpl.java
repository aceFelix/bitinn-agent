package com.itniuma.bitinn.service.aichat.impl;

import com.itniuma.bitinn.enums.SSEMessageType;
import com.itniuma.bitinn.pojo.dto.ChatRequest;
import com.itniuma.bitinn.pojo.mongo.Message;
import com.itniuma.bitinn.repository.MessageRepository;
import com.itniuma.bitinn.service.ai.FileProcessService;
import com.itniuma.bitinn.service.aichat.ChatAsyncService;
import com.itniuma.bitinn.service.aichat.ChatService;
import com.itniuma.bitinn.utils.SSEServer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI 聊天服务实现
 * 负责接收用户消息（纯文本/带文件）、构建 Message 对象并保存到 MongoDB，
 * 然后委托 ChatAsyncService 异步调用 AI 模型。
 *
 * @author aceFelix
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final MessageRepository messageRepository;
    private final ChatAsyncService chatAsyncService;
    private final FileProcessService fileProcessService;

    /**
     * 处理纯文本消息，构建 Message 对象后保存并推送到异步处理队列。
     */
    @Override
    public void sendMessage(ChatRequest request, Integer userId) {
        String content = request.getContent();
        String conversationId = request.getConversationId();

        if (content == null || content.isBlank()) {
            SSEServer.sendMessage(String.valueOf(userId), "消息内容不能为空", SSEMessageType.ERROR);
            return;
        }

        Map<String, Object> metadata = new HashMap<>();
        if (request.getSkill() != null && !request.getSkill().isEmpty()) {
            metadata.put("skill", request.getSkill());
        }

        List<Message.Attachment> attachments = null;
        if (request.getAttachments() != null) {
            attachments = request.getAttachments().stream()
                    .map(a -> Message.Attachment.builder()
                            .type((String) a.get("type"))
                            .url((String) a.get("url"))
                            .name((String) a.get("name"))
                            .size(a.get("size") != null ? ((Number) a.get("size")).longValue() : null)
                            .build())
                    .toList();
        }

        Message message = Message.builder()
                .conversationId(conversationId)
                .userId(String.valueOf(userId))
                .role("user")
                .content(content)
                .attachments(attachments)
                .metadata(metadata.isEmpty() ? null : metadata)
                .build();

        saveAndPush(message);
    }

    /**
     * 处理带文件附件的消息，图片转 Base64 嵌入附件，文档通过 Tika 提取文本拼入消息体。
     */
    @Override
    public void sendFileMessage(String conversationId, String content, List<MultipartFile> files, Integer userId) {
        String finalContent = content;
        List<Message.Attachment> attachments = new ArrayList<>();

        if (files != null && !files.isEmpty()) {
            StringBuilder docContext = new StringBuilder();
            for (MultipartFile file : files) {
                if (file.isEmpty()) continue;
                try {
                    if (fileProcessService.isImage(file)) {
                        String base64Data = fileProcessService.imageToBase64(file);
                        attachments.add(Message.Attachment.builder()
                                .type("image")
                                .name(file.getOriginalFilename())
                                .size(file.getSize())
                                .mimeType(file.getContentType())
                                .data(base64Data)
                                .build());
                        log.info("[文件上传] 图片: {}, 大小: {}KB", file.getOriginalFilename(), file.getSize() / 1024);
                    } else {
                        String parsedContent = fileProcessService.buildFileContext(file);
                        docContext.append(parsedContent).append("\n\n");
                        attachments.add(Message.Attachment.builder()
                                .type("file")
                                .name(file.getOriginalFilename())
                                .size(file.getSize())
                                .build());
                        log.info("[文件上传] 文档: {}, 大小: {}KB", file.getOriginalFilename(), file.getSize() / 1024);
                    }
                } catch (Exception e) {
                    log.error("[文件上传] 处理失败: {}", file.getOriginalFilename(), e);
                    docContext.append("[附件: ").append(file.getOriginalFilename()).append(" 处理失败]\n\n");
                }
            }
            if (docContext.length() > 0) {
                finalContent = content != null && !content.isBlank()
                        ? content + "\n\n" + docContext.toString().trim()
                        : docContext.toString().trim();
            }
        }

        Message message = Message.builder()
                .conversationId(conversationId)
                .userId(String.valueOf(userId))
                .role("user")
                .content(finalContent)
                .attachments(attachments.isEmpty() ? null : attachments)
                .build();

        saveAndPush(message);
    }

    /**
     * 保存消息到 MongoDB 并推送到异步 AI 处理队列。
     */
    private void saveAndPush(Message message) {
        String userId = message.getUserId();
        String conversationId = message.getConversationId();

        message.setCreatedAt(LocalDateTime.now());
        message.setUpdatedAt(LocalDateTime.now());
        messageRepository.save(message);

        String skill = message.getMetadata() != null ? (String) message.getMetadata().get("skill") : null;

        log.info("[sendMessage] userId={}, convId={}, prompt={}, attachments={}, skill={}",
                userId, conversationId,
                message.getContent().substring(0, Math.min(50, message.getContent().length())),
                message.getAttachments() != null ? message.getAttachments().size() : 0, skill);

        chatAsyncService.pushMessage(userId, message.getContent(), conversationId, message.getAttachments(), skill);
    }
}