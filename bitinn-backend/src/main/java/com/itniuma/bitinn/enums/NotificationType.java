package com.itniuma.bitinn.enums;

/**
 * 通知类型枚举
 *
 * 定义系统通知的分类，每种类型对应一种用户互动行为。
 * 通知触发后会写入 MySQL 通知表，前端通过轮询/WebSocket 获取未读数。
 *
 * @author aceFelix
 */
public enum NotificationType {
    /** 点赞通知 */
    LIKE("like", "赞了你的文章"),
    /** 收藏通知 */
    FAVORITE("favorite", "收藏了你的文章"),
    /** 评论通知 */
    COMMENT("comment", "评论了你的文章"),
    /** 关注通知 */
    FOLLOW("follow", "关注了你"),
    /** 转发通知 */
    REPOST("repost", "转发了你的文章");

    private final String code;
    private final String description;

    NotificationType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 获取类型编码，用于数据库存储和 API 传输
     */
    public String getCode() {
        return code;
    }

    /**
     * 获取类型中文描述，用于前端展示
     */
    public String getDescription() {
        return description;
    }

    /**
     * 根据编码查找枚举值
     *
     * @param code 类型编码（如 "like"、"comment"）
     * @return 对应的枚举值，未匹配时返回 null
     */
    public static NotificationType fromCode(String code) {
        for (NotificationType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return null;
    }
}
