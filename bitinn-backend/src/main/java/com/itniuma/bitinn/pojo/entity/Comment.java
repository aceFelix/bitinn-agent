package com.itniuma.bitinn.pojo.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 评论实体类（MySQL）
 *
 * 映射 comment 表，支持二级回复（通过 parentId 字段）。
 * username/nickname/userPic 由联表查询填充，不存库。
 *
 * @author aceFelix
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Comment {
    /** 主键 ID */
    private Integer id;
    /** 所属文章 ID */
    private Integer articleId;
    /** 评论用户 ID */
    private Integer userId;
    /** 评论内容 */
    private String content;
    /** 父评论 ID，用于二级回复 */
    private Integer parentId;
    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /** 评论者用户名（联表查询填充，不存库） */
    private String username;
    /** 评论者头像（联表查询填充，不存库） */
    private String userPic;
    /** 评论者昵称（联表查询填充，不存库） */
    private String nickname;
}
