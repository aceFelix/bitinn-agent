package com.itniuma.bitinn.pojo.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * AI 聊天请求 DTO
 *
 * POST /api/chat/send 接口的 JSON 请求体。
 * Controller 反序列化后传递给 ChatService 处理。
 *
 * @author aceFelix
 */
@Data
public class ChatRequest {
    /** 会话 ID，新建会话时为空 */
    private String conversationId;
    /** 用户消息内容 */
    private String content;
    /** 附件列表，每项含 type/url/name/size */
    private List<Map<String, Object>> attachments;
    /** AI 技能类型（article / code / data / translate / summary / resume），非技能请求为空 */
    private String skill;
}
