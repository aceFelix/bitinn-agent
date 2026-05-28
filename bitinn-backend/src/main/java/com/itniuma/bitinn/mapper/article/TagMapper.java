package com.itniuma.bitinn.mapper.article;

import com.itniuma.bitinn.pojo.entity.Tag;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 文章标签 Mapper
 *
 * MyBatis 数据访问接口，SQL 定义在 TagMapper.xml 中。
 * 标签与文章通过 article_tag 中间表实现多对多关联。
 *
 * @author aceFelix
 */
@Mapper
public interface TagMapper {

    /**
     * 查询某篇文章的所有标签
     *
     * @param articleId 文章 ID
     * @return 标签列表，用于详情页和列表展示
     */
    List<Tag> findByArticleId(Integer articleId);

    /**
     * 查询某用户使用过的标签
     *
     * @param userId 用户 ID
     * @return 标签列表
     */
    List<Tag> listByUser(Integer userId);

    /** 获取全部标签列表，按排序字段升序 */
    List<Tag> list();
}
