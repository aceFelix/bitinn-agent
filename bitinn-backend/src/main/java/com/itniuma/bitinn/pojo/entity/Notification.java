package com.itniuma.bitinn.pojo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 通知实体类（MySQL）
 *
 * 映射 notification 表，存储用户收到的各类通知。
 * 通知类型：like / favorite / comment / follow / repost
 *
 * sourceUsername 和 sourceUserAvatar 由联表查询填充，用于前端展示。
 *
 * @author aceFelix
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Notification {
    /** 主键 ID */
    private Integer id;
    /** 接收通知的用户 ID */
    private Integer userId;
    /** 通知类型: like / favorite / comment / follow / repost */
    private String type;
    /** 通知标题 */
    private String title;
    /** 通知内容摘要 */
    private String content;
    /** 触发通知的用户 ID（谁干的） */
    private Integer sourceUserId;
    /** 关联资源 ID（文章 ID / 评论 ID 等） */
    private Integer sourceId;
    /** 资源类型: article / comment */
    private String sourceType;
    /** 是否已读: 0-未读 1-已读 */
    private Integer isRead;
    /** 创建时间 */
    private LocalDateTime createTime;

    /** 触发者用户名（联表查询填充） */
    private String sourceUsername;
    /** 触发者头像（联表查询填充） */
    private String sourceUserAvatar;
}
