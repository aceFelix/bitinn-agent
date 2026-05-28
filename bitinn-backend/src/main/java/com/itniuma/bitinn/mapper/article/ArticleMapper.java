package com.itniuma.bitinn.mapper.article;

import com.itniuma.bitinn.pojo.entity.Article;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 文章 Mapper
 *
 * MyBatis 数据访问接口，所有 SQL 定义在 resources 下的 ArticleMapper.xml 中。
 * 提供文章的 CRUD、Feed 流查询、热度分计算、计数更新等功能。
 *
 * 关键查询：
 *   - listFeed：社区首页 Feed 流，支持 recommend / latest / hot 三种排序
 *   - recalculateHotScores：定时任务调用，批量刷新文章热度分
 *   - updateCounts：RabbitMQ 消费者调用，将 Redis 最新计数回写 MySQL
 *
 * @author aceFelix
 */
@Mapper
public interface ArticleMapper {

    /** 新增文章，返回自增主键 id */
    void insert(Article article);

    /** 更新文章（title、content、coverImg、excerpt、state、categoryId） */
    void update(Article article);

    /** 根据主键删除文章 */
    void deleteById(Integer id);

    /** 根据主键查询文章（不含标签） */
    Article findById(Integer id);

    /**
     * 分页查询文章列表
     *
     * @param categoryId 分类 ID（可选）
     * @param state      状态（"草稿"、"已发布"等）
     * @param userId     作者用户 ID（可选），用于"我的文章"
     */
    List<Article> list(@Param("categoryId") Integer categoryId,
                       @Param("state") String state,
                       @Param("userId") Integer userId);

    /**
     * 分页查询文章列表（含作者信息），用于 ES 全量同步和滚动查询
     */
    List<Article> listWithAuthor(@Param("categoryId") Integer categoryId,
                                 @Param("state") String state,
                                 @Param("userId") Integer userId,
                                 @Param("offset") int offset,
                                 @Param("pageSize") int pageSize);

    /**
     * 社区 Feed 流查询（含作者、分类、标签联表）
     *
     * @param sortType recommend（按 hot_score）/ latest（按时间）/ hot（按互动量加权÷时间衰减）
     */
    List<Article> listFeed(@Param("sortType") String sortType,
                           @Param("state") String state,
                           @Param("offset") int offset,
                           @Param("pageSize") int pageSize);

    /** Feed 流总数 */
    Long countFeed(@Param("state") String state);

    /** 通用列表总数 */
    Long countList(@Param("categoryId") Integer categoryId,
                   @Param("state") String state,
                   @Param("userId") Integer userId);

    /** 文章阅读量 +1 */
    void incrementViewCount(@Param("id") Integer id);

    /** 为文章添加标签关联 */
    void insertArticleTag(@Param("articleId") Integer articleId, @Param("tagId") Integer tagId);

    /** 删除文章的所有标签关联（更新前先删再插） */
    void deleteArticleTagsByArticleId(Integer articleId);

    /**
     * 批量重算热度分
     *
     * 公式：hot_score = (like*2 + favorite*3 + comment*1.5) / POWER(GREATEST(hours_since_create, 1) + 1, 1.5)
     * 由 HotScoreScheduler 每 10 分钟调用一次，结果用于 Feed 推荐排序。
     */
    void recalculateHotScores();

    /** 批量查询文章（用于 Redis/ES 同步时获取最新数据） */
    List<Article> findByIds(@Param("ids") List<Integer> ids);

    /**
     * 更新文章计数（Redis → MySQL 回写）
     *
     * 由 DataSyncConsumer 消费 RabbitMQ 消息后调用，
     * 将 Redis 中累积的互动计数持久化到 MySQL。
     */
    void updateCounts(@Param("id") Integer id,
                      @Param("likeCount") int likeCount,
                      @Param("favoriteCount") int favoriteCount,
                      @Param("commentCount") int commentCount,
                      @Param("shareCount") int shareCount);
}
