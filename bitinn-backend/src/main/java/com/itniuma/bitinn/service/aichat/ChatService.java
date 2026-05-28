package com.itniuma.bitinn.service.aichat;

import com.itniuma.bitinn.pojo.dto.ChatRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * AI 聊天服务接口
 * 定义用户消息发送的契约，支持纯文本和带附件的消息。
 *
 * @author aceFelix
 */
public interface ChatService {

    /**
     * 发送文本消息（支持 skill 技能参数和附件元数据）
     */
    void sendMessage(ChatRequest request, Integer userId);

    /**
     * 发送带文件附件的消息，自动识别图片/文档类型并分别处理
     */
    void sendFileMessage(String conversationId, String content, List<MultipartFile> files, Integer userId);
}