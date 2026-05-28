package com.itniuma.bitinn.controller.article;

import com.itniuma.bitinn.pojo.entity.Article;
import com.itniuma.bitinn.pojo.vo.PageBean;
import com.itniuma.bitinn.pojo.vo.Result;
import com.itniuma.bitinn.service.article.ArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 文章管理控制器
 *
 * 提供文章的 CRUD、Feed 流、我的文章等接口。
 * 文章发布/更新会通过 RabbitMQ 异步同步到 Elasticsearch 搜索索引。
 *
 * @author aceFelix
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/article")
public class ArticleController {

    private final ArticleService articleService;

    /**
     * 发布新文章
     *
     * @param article 文章实体，至少包含 title 和 content
     * @return 操作结果
     */
    @PostMapping
    public Result add(@RequestBody Article article) {
        if (article.getTitle() == null || article.getTitle().trim().isEmpty()) {
            return Result.error("文章标题不能为空");
        }
        if (article.getContent() == null || article.getContent().trim().isEmpty()) {
            return Result.error("文章内容不能为空");
        }
        return articleService.add(article);
    }

    /**
     * 更新文章
     *
     * @param article 文章实体，必须包含 id
     * @return 操作结果
     */
    @PutMapping
    public Result update(@RequestBody Article article) {
        if (article.getId() == null) {
            return Result.error("文章ID不能为空");
        }
        return articleService.update(article);
    }

    /**
     * 删除文章
     *
     * @param id 文章 ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Integer id) {
        return articleService.delete(id);
    }

    /**
     * 查看文章详情
     *
     * @param id 文章 ID
     * @return 文章完整信息（含作者、分类、标签）
     */
    @GetMapping("/{id}")
    public Result<Article> detail(@PathVariable Integer id) {
        return articleService.detail(id);
    }

    /**
     * 文章列表（按分类 + 状态分页查询）
     *
     * @param categoryId 分类 ID（可选）
     * @param state      文章状态，默认 "已发布"
     * @param pageNum    页码，默认 1
     * @param pageSize   每页条数，默认 10
     * @return 分页结果
     */
    @GetMapping
    public Result<PageBean<Article>> list(
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false, defaultValue = "已发布") String state,
            @RequestParam(required = false, defaultValue = "1") Integer pageNum,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize) {
        return articleService.list(categoryId, state, pageNum, pageSize);
    }

    /**
     * 获取当前用户的文章列表
     *
     * @param state    文章状态（可选，如 "草稿"、"已发布"）
     * @param pageNum  页码，默认 1
     * @param pageSize 每页条数，默认 10
     * @return 分页结果
     */
    @GetMapping("/my")
    public Result<PageBean<Article>> myArticles(
            @RequestParam(required = false) String state,
            @RequestParam(required = false, defaultValue = "1") Integer pageNum,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize) {
        return articleService.myArticles(state, pageNum, pageSize);
    }

    /**
     * 文章 Feed 流（社区首页）
     *
     * 支持三种排序：
     *   - recommend：按热度分（hot_score）倒序
     *   - latest：按发布时间倒序
     *   - hot：按互动量加权倒序
     *
     * @param sortType 排序类型，默认 recommend
     * @param pageNum  页码，默认 1
     * @param pageSize 每页条数，默认 10
     * @return 分页结果（含红点缓存优化）
     */
    @GetMapping("/feed")
    public Result<PageBean<Article>> feed(
            @RequestParam(required = false, defaultValue = "recommend") String sortType,
            @RequestParam(required = false, defaultValue = "1") Integer pageNum,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize) {
        return articleService.feed(sortType, pageNum, pageSize);
    }
}
