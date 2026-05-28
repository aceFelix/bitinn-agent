package com.itniuma.bitinn.pojo.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户关注实体类（MySQL）
 *
 * 映射 user_follow 表，记录用户之间的关注关系。
 * followerId 是关注者，followingId 是被关注者。
 * 关注为 Toggle 模式：已关注 → 取消（delete），未关注 → 添加（insert）。
 *
 * @author aceFelix
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserFollow {
    /** 主键 ID */
    private Integer id;
    /** 关注者 ID */
    private Integer followerId;
    /** 被关注者 ID */
    private Integer followingId;
    /** 关注时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
