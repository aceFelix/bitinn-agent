package com.itniuma.bitinn.pojo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 通知计数实体类（MySQL）
 *
 * 映射 notification_count 表，存储用户各类通知的未读数。
 * 前端通过 GET /api/notifications/count 获取，用于显示红点和数字。
 *
 * @author aceFelix
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationCount {
    /** 用户 ID */
    private Integer userId;
    /** 未读通知总数 */
    private Integer unreadCount;
    /** 点赞未读数 */
    private Integer likeCount;
    /** 收藏未读数 */
    private Integer favoriteCount;
    /** 评论未读数 */
    private Integer commentCount;
    /** 关注未读数 */
    private Integer followCount;
    /** 转发未读数 */
    private Integer repostCount;
}
