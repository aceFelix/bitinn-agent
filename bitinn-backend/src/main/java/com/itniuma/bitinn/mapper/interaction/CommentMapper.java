package com.itniuma.bitinn.mapper.interaction;

import com.itniuma.bitinn.pojo.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 评论 Mapper
 *
 * MyBatis 数据访问接口，SQL 定义在 CommentMapper.xml 中。
 * 提供评论的增删查和计数功能，评论按时间正序排列。
 *
 * @author aceFelix
 */
@Mapper
public interface CommentMapper {

    /** 新增评论 */
    void insert(Comment comment);

    /** 删除评论（软删除或硬删除） */
    void delete(@Param("id") Integer id);

    /** 查询文章的所有评论 */
    List<Comment> findByArticleId(@Param("articleId") Integer articleId);

    /** 统计文章的评论总数 */
    Long countByArticleId(@Param("articleId") Integer articleId);

    /** 根据主键查询评论 */
    Comment findById(@Param("id") Integer id);
}
