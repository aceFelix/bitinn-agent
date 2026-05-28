package com.itniuma.bitinn.mapper.interaction;

import com.itniuma.bitinn.pojo.entity.ArticleLike;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 文章点赞 Mapper
 *
 * MyBatis 数据访问接口，SQL 定义在 ArticleLikeMapper.xml 中。
 * 点赞为 Toggle 模式：已点赞则取消（delete），未点赞则添加（insert）。
 *
 * @author aceFelix
 */
@Mapper
public interface ArticleLikeMapper {

    /** 点赞 */
    void insert(@Param("articleId") Integer articleId, @Param("userId") Integer userId);

    /** 取消点赞 */
    void delete(@Param("articleId") Integer articleId, @Param("userId") Integer userId);

    /** 查询用户是否已点赞该文章 */
    ArticleLike findByArticleAndUser(@Param("articleId") Integer articleId, @Param("userId") Integer userId);

    /** 查询用户点赞的所有文章 ID 列表 */
    List<Integer> findLikedArticleIdsByUserId(@Param("userId") Integer userId);

    /** 统计文章的点赞总数 */
    Long countByArticleId(@Param("articleId") Integer articleId);
}
