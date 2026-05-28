package com.itniuma.bitinn.pojo.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 文章收藏实体类（MySQL）
 *
 * 映射 article_favorite 表，记录用户-文章的收藏关系。
 * 收藏为 Toggle 模式：存在记录 → 取消（delete），不存在 → 添加（insert）。
 *
 * @author aceFelix
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ArticleFavorite {
    /** 主键 ID */
    private Integer id;
    /** 文章 ID */
    private Integer articleId;
    /** 收藏用户 ID */
    private Integer userId;
    /** 收藏时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
