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
import java.util.Map;

/**
 * AI 聊天消息实体（MongoDB）
 *
 * 每条消息属于一个会话（conversationId），记录用户或 AI 的发言。
 * content 以 Markdown 格式存储，rawContent 保存 AI 返回的原始内容。
 *
 * metadata 扩展字段：
 *   - skill：AI 技能类型（article / code 等），非技能请求不设置
 *   - 可用于存储未来的自定义参数
 *
 * 复合索引：
 *   - conv_time_idx：按会话 ID + 时间正序，用于加载历史消息
 *   - user_time_idx：按用户 ID + 时间倒序，用于统计和审计
 *
 * @author aceFelix
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "messages")
@CompoundIndexes({
    @CompoundIndex(name = "conv_time_idx", def = "{'conversationId': 1, 'createdAt': 1}"),
    @CompoundIndex(name = "user_time_idx", def = "{'userId': 1, 'createdAt': -1}")
})
public class Message {

    /** MongoDB 自动生成的文档 ID */
    @Id
    private String id;

    /** 所属对话 ID */
    private String conversationId;

    /** 所属用户 ID */
    private String userId;

    /** 角色: "user" / "assistant" / "system" */
    private String role;

    /** 消息内容（Markdown 格式） */
    private String content;

    /** 原始内容（AI 返回的完整内容，用于调试） */
    private String rawContent;

    /** 使用的模型名 */
    private String model;

    /** Token 消耗量 */
    private Integer tokens;

    /** 附件列表（图片 Base64 / 文档 Base64 等） */
    private List<Attachment> attachments;

    /** 扩展元数据（skill 等自定义字段） */
    private Map<String, Object> metadata;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;

    /**
     * 附件内嵌类
     *
     * type 区分图片（image）、文档（file）等类型，
     * data 存储 Base64 编码的文件内容，name/size/mimeType 用于前端展示。
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Attachment {
        /** 附件类型：image / file */
        private String type;
        /** 文件 URL（上传到 OSS 后的地址） */
        private String url;
        /** 文件名 */
        private String name;
        /** 文件大小（字节） */
        private Long size;
        /** MIME 类型（如 image/png） */
        private String mimeType;
        /** Base64 编码的文件数据 */
        private String data;
    }
}
