package com.itniuma.bitinn.controller.aichat;

import com.itniuma.bitinn.pojo.mongo.Conversation;
import com.itniuma.bitinn.pojo.mongo.Message;
import com.itniuma.bitinn.pojo.vo.Result;
import com.itniuma.bitinn.service.aichat.ConversationService;
import com.itniuma.bitinn.utils.ThreadLocalUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 会话管理 Controller
 *
 * 管理 AI 对话的会话生命周期：创建、列表、删除、查看消息。
 * 会话存储在 MongoDB，包含模式（普通/专业/识图/生图）和模型选择信息。
 *
 * @author aceFelix
 */
@Slf4j
@RestController
@RequestMapping("/api/conversation")
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationService conversationService;

    /**
     * 获取当前用户的会话列表
     *
     * @return 会话列表，按更新时间倒序
     */
    @GetMapping("/list")
    public Result<List<Conversation>> listConversations() {
        Map<String, Object> claims = ThreadLocalUtil.get();
        Integer userId = (Integer) claims.get("id");
        List<Conversation> list = conversationService.listConversations(userId);
        return Result.success(list);
    }

    /**
     * 创建新会话
     *
     * @param body 请求体，可包含 title（标题）、model（模型名）、mode（对话模式）
     * @return 创建成功的会话对象（含 MongoDB 生成的 _id）
     */
    @PostMapping("/create")
    public Result<Conversation> createConversation(@RequestBody Map<String, String> body) {
        Map<String, Object> claims = ThreadLocalUtil.get();
        Integer userId = (Integer) claims.get("id");
        String title = body.get("title");
        String model = body.get("model");
        String mode = body.get("mode");
        Conversation conv = conversationService.createConversation(userId, title, model, mode);
        return Result.success(conv);
    }

    /**
     * 删除会话及其所有消息
     *
     * @param id 会话 ID（MongoDB ObjectId）
     * @return 空 Result
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteConversation(@PathVariable String id) {
        Map<String, Object> claims = ThreadLocalUtil.get();
        Integer userId = (Integer) claims.get("id");
        conversationService.deleteConversation(id, userId);
        return Result.success();
    }

    /**
     * 获取会话的所有历史消息
     *
     * @param id 会话 ID
     * @return 消息列表，按时间正序
     */
    @GetMapping("/{id}/messages")
    public Result<List<Message>> getMessages(@PathVariable String id) {
        List<Message> messages = conversationService.getMessages(id);
        return Result.success(messages);
    }

    /**
     * 获取单个会话信息（含模式和模型）
     *
     * @param id 会话 ID
     * @return 会话对象，前端用于恢复模式和模型选择
     */
    @GetMapping("/{id}")
    public Result<Conversation> getConversation(@PathVariable String id) {
        Conversation conv = conversationService.getConversation(id);
        return Result.success(conv);
    }
}
