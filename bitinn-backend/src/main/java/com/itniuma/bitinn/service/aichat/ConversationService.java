package com.itniuma.bitinn.service.aichat;

import com.itniuma.bitinn.pojo.mongo.Conversation;
import com.itniuma.bitinn.pojo.mongo.Message;

import java.util.List;

/**
 * 对话管理服务接口
 * 定义对话的增删查操作契约。
 *
 * @author aceFelix
 */
public interface ConversationService {

    /**
     * 创建新对话，支持指定标题、模型和模式
     */
    Conversation createConversation(Integer userId, String title, String model, String mode);

    /**
     * 获取用户的对话列表（最多50条，按更新时间倒序）
     */
    List<Conversation> listConversations(Integer userId);

    /**
     * 删除指定对话，同时级联删除对话下的所有消息
     */
    void deleteConversation(String id, Integer userId);

    /**
     * 保存单条消息
     */
    void saveMessage(Message message);

    /**
     * 获取对话的消息列表（最多100条，按时间正序）
     */
    List<Message> getMessages(String conversationId);

    /**
     * 根据 ID 获取对话，不存在返回 null
     */
    Conversation getConversation(String id);
}
