package com.itniuma.bitinn.pojo.document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Elasticsearch 文章文档实体
 *
 * 映射到 ES 索引 bitinn-article，用于全文搜索。
 * 字段使用 IK 中文分词器（ik_max_word 写 / ik_smart 查），
 * 标题和标签权重高于正文，通过 ArticleSearchService 的 boost 控制。
 *
 * 注意：这是 ES 的冗余副本，主数据在 MySQL article 表，
 * 通过 RabbitMQ 消息驱动的 ArticleDataSyncService 保持同步。
 *
 * @author aceFelix
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(indexName = "bitinn-article")
@Setting(shards = 1, replicas = 0)
public class ArticleDocument {

    /** 文章 ID，与 MySQL 主键一致 */
    @Id
    private Integer id;

    /** 标题（IK 分词） */
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String title;

    /** 正文（IK 分词） */
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String content;

    /** 摘要（IK 分词） */
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String excerpt;

    /** 封面图 URL（精确匹配） */
    @Field(type = FieldType.Keyword)
    private String coverImg;

    /** 分类名（精确匹配，冗余自 category 表） */
    @Field(type = FieldType.Keyword)
    private String categoryName;

    /** 标签列表（精确匹配，冗余自 tag 表） */
    @Field(type = FieldType.Keyword)
    private List<String> tags;

    /** 作者昵称（精确匹配，冗余自 user 表） */
    @Field(type = FieldType.Keyword)
    private String authorName;

    /** 作者头像（精确匹配） */
    @Field(type = FieldType.Keyword)
    private String authorAvatar;

    /** 点赞数 */
    @Field(type = FieldType.Integer)
    private Integer likeCount;

    /** 评论数 */
    @Field(type = FieldType.Integer)
    private Integer commentCount;

    /** 收藏数 */
    @Field(type = FieldType.Integer)
    private Integer favoriteCount;

    /** 阅读数 */
    @Field(type = FieldType.Integer)
    private Integer viewCount;

    /** 热度分，用于排序 */
    @Field(type = FieldType.Double)
    private Double hotScore;

    /** 创建时间 */
    @Field(type = FieldType.Date, format = {}, pattern = "yyyy-MM-dd HH:mm:ss||epoch_millis")
    private LocalDateTime createTime;

    /** 更新时间 */
    @Field(type = FieldType.Date, format = {}, pattern = "yyyy-MM-dd HH:mm:ss||epoch_millis")
    private LocalDateTime updateTime;
}
