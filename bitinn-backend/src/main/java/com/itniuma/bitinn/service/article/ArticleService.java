package com.itniuma.bitinn.service.article;

import com.itniuma.bitinn.pojo.entity.Article;
import com.itniuma.bitinn.pojo.vo.PageBean;
import com.itniuma.bitinn.pojo.vo.Result;

/**
 * 文章服务接口
 * 定义文章增删改查、个人文章列表和 Feed 流的操作契约。
 *
 * @author aceFelix
 */
public interface ArticleService {

    /**
     * 发布/保存文章，支持草稿和直接发布
     */
    Result add(Article article);

    /**
     * 更新文章，仅允许作者本人操作
     */
    Result update(Article article);

    /**
     * 删除文章，同时清理标签关联
     */
    Result delete(Integer id);

    /**
     * 查看文章详情，同时更新浏览量
     */
    Result<Article> detail(Integer id);

    /**
     * 分页查询文章列表，支持按分类和状态筛选
     */
    Result<PageBean<Article>> list(Integer categoryId, String state, Integer pageNum, Integer pageSize);

    /**
     * 分页查询当前用户的文章
     */
    Result<PageBean<Article>> myArticles(String state, Integer pageNum, Integer pageSize);

    /**
     * Feed 流分页查询，支持推荐/最新/热门排序，含多级缓存
     */
    Result<PageBean<Article>> feed(String sortType, Integer pageNum, Integer pageSize);
}
