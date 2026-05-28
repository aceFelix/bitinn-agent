package com.itniuma.bitinn.mapper.interaction;

import com.itniuma.bitinn.pojo.entity.ArticleFavorite;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 文章收藏 Mapper
 *
 * MyBatis 数据访问接口，SQL 定义在 ArticleFavoriteMapper.xml 中。
 * 收藏为 Toggle 模式：已收藏则取消（delete），未收藏则添加（insert）。
 *
 * @author aceFelix
 */
@Mapper
public interface ArticleFavoriteMapper {

    /** 收藏文章 */
    void insert(@Param("articleId") Integer articleId, @Param("userId") Integer userId);

    /** 取消收藏 */
    void delete(@Param("articleId") Integer articleId, @Param("userId") Integer userId);

    /** 查询用户是否已收藏该文章 */
    ArticleFavorite findByArticleAndUser(@Param("articleId") Integer articleId, @Param("userId") Integer userId);

    /** 查询用户收藏的所有文章 ID 列表 */
    List<Integer> findFavoritedArticleIdsByUserId(@Param("userId") Integer userId);

    /** 统计文章的收藏总数 */
    Long countByArticleId(@Param("articleId") Integer articleId);
}
