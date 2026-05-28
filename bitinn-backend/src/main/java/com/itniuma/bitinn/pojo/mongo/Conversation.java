package com.itniuma.bitinn.pojo.mongo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

/**
 * AI 对话会话实体（MongoDB）
 *
 * 每个会话对应一次连续的 AI 对话，存储对话的元信息。
 * mode 字段记录对话模式（normal / professional / vision / image-gen），
 * model 字段记录用户选择的模型名，用于恢复对话上下文。
 *
 * 复合索引：
 *   - user_updated_idx：按用户 ID + 更新时间倒序，用于侧边栏会话列表
 *   - user_pinned_idx：按用户 ID + 置顶状态，用于置顶会话快速查询
 *
 * @author aceFelix
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "conversations")
@CompoundIndexes({
    @CompoundIndex(name = "user_updated_idx", def = "{'userId': 1, 'updatedAt': -1}"),
    @CompoundIndex(name = "user_pinned_idx", def = "{'userId': 1, 'isPinned': -1}")
})
public class Conversation {

    /** MongoDB 自动生成的文档 ID */
    @Id
    private String id;

    /** 所属用户 ID */
    private Integer userId;

    /** 会话标题（默认"新对话"，首次 AI 回复后自动生成） */
    private String title;

    /** 选用的模型名（如 qwen3.6-plus / deepseek-v4-pro） */
    private String model;

    /** 对话模式：normal / professional / vision / image-gen */
    private String mode;

    /** 消息总数 */
    private Integer messageCount;

    /** 最后一条消息的时间 */
    private LocalDateTime lastMessageAt;

    /** 用户自定义标签 */
    private List<String> tags;

    /** 是否置顶 */
    private Boolean isPinned;

    /** AI 生成的会话摘要 */
    private String summary;

    /** 摘要最后更新时间 */
    private LocalDateTime summaryUpdatedAt;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;
}
