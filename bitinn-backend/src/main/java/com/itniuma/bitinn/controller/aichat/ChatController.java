package com.itniuma.bitinn.controller.aichat;

import com.itniuma.bitinn.pojo.dto.ChatRequest;
import com.itniuma.bitinn.pojo.vo.Result;
import com.itniuma.bitinn.service.aichat.ChatService;
import com.itniuma.bitinn.utils.ThreadLocalUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * AI 聊天控制器
 *
 * 接收前端发送的聊天消息，包括纯文本和带附件的消息。
 * Controller 层只负责参数接收和调用 Service，不处理业务逻辑。
 *
 * 消息处理链路：
 *   ChatController → ChatService.sendMessage / sendFileMessage
 *     → 保存用户消息到 MongoDB → ChatAsyncService.pushMessage()（异步）
 *       → AI 模型流式调用 → SSE 实时推送回复给前端
 *
 * @author aceFelix
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    /**
     * 发送纯文本消息（JSON 请求体）
     *
     * @param request 聊天请求体，包含 conversationId（会话ID）、content（消息内容）、skill（技能类型）等
     * @return Result 立即返回成功，实际 AI 回复通过 SSE 异步推送
     */
    @PostMapping(value = "/send", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Result<Void> chat(@RequestBody ChatRequest request) {
        Map<String, Object> claims = ThreadLocalUtil.get();
        Integer userId = (Integer) claims.get("id");
        chatService.sendMessage(request, userId);
        return Result.success();
    }

    /**
     * 发送带附件的消息（Multipart Form Data）
     *
     * 支持上传图片（识图、生图编辑）和文档（PDF、TXT、Markdown）。
     * 图片会被转为 Base64，文档会被解析为文本上下文拼接到消息中。
     *
     * @param conversationId 会话 ID
     * @param content        消息文本内容
     * @param files          附件文件列表（可选）
     * @return Result 立即返回成功，实际 AI 回复通过 SSE 异步推送
     */
    @PostMapping(value = "/send", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<Void> chatWithFiles(
            @RequestParam String conversationId,
            @RequestParam String content,
            @RequestParam(value = "files", required = false) List<MultipartFile> files) {

        Map<String, Object> claims = ThreadLocalUtil.get();
        Integer userId = (Integer) claims.get("id");
        chatService.sendFileMessage(conversationId, content, files, userId);
        return Result.success();
    }
}