package com.itniuma.bitinn.pojo.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.itniuma.bitinn.annotation.State;
import jakarta.validation.constraints.Size;
import jakarta.validation.groups.Default;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 文章实体类（MySQL）
 *
 * 文章的核心数据模型，映射 article 表。
 * 标签通过 article_tag 中间表多对多关联，查询时由 ArticleMapper.xml 联表填充。
 * 计数（like/favorite/comment/share/view）由 Redis 实时维护，定时回写 MySQL。
 *
 * @author aceFelix
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Article {
    /** 主键 ID */
    private Integer id;

    /** 标题，最大 100 字符 */
    @Size(max = 100)
    private String title;

    /** 正文（Markdown / HTML） */
    private String content;

    /** 封面图 URL */
    private String coverImg;

    /** 摘要（100 字内） */
    private String excerpt;

    /** 状态：草稿 / 已发布 */
    @State
    private String state;

    /** 分类 ID */
    private Integer categoryId;

    /** 创建人用户 ID */
    private Integer createUser;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /** 前端传的标签 ID 列表，发布/更新时使用 */
    private List<Integer> tagIds;

    /** 查询时填充的标签对象列表，展示用 */
    private List<Tag> tags;

    /** 阅读数 */
    private Integer viewCount;
    /** 点赞数 */
    private Integer likeCount;
    /** 评论数 */
    private Integer commentCount;
    /** 收藏数 */
    private Integer favoriteCount;
    /** 分享数 */
    private Integer shareCount;

    /** 作者昵称（联表查询填充） */
    private String authorName;
    /** 作者头像（联表查询填充） */
    private String authorAvatar;

    /** 分类名（联表查询填充） */
    private String categoryName;

    /** 热度分，由 HotScoreScheduler 定时计算 */
    private Double hotScore;

    /** 添加时的校验分组 */
    public interface Add extends Default {
    }

    /** 更新时的校验分组 */
    public interface Update extends Default {
    }
}
