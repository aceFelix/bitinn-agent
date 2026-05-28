package com.itniuma.bitinn.controller.search;

import com.itniuma.bitinn.pojo.vo.Result;
import com.itniuma.bitinn.service.search.ArticleSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 文章搜索控制器
 *
 * 提供基于 Elasticsearch 的全文搜索、搜索建议和热搜词功能。
 * 依赖 Elasticsearch 服务，如果 ES 未安装则所有接口返回友好提示。
 *
 * 搜索流程：
 *   用户输入关键词 → ES MultiMatch 多字段匹配 + 短语精确匹配
 *     → IK 中文分词 + 高亮命中词 → 按相关性/热度/时间排序返回
 *
 * @author aceFelix
 */
@RestController
@RequestMapping("/api/search")
public class ArticleSearchController {

    /**
     * 搜索服务，依赖 Elasticsearch，未安装时为 null
     */
    @Autowired(required = false)
    private ArticleSearchService searchService;

    /**
     * 全文搜索
     *
     * 在文章标题、摘要、正文、标签、分类、作者中搜索关键词，
     * 返回高亮结果和分页数据。
     *
     * @param keyword  搜索关键词（必填）
     * @param pageNum  页码，默认 1
     * @param pageSize 每页条数，默认 10
     * @param sortType 排序类型：relevant（相关性）/ hot（热度）/ latest（最新）
     * @return 搜索结果（total、items、keyword、pageNum、pageSize）
     */
    @GetMapping
    public Result search(
            @RequestParam String keyword,
            @RequestParam(required = false, defaultValue = "1") Integer pageNum,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(required = false, defaultValue = "relevant") String sortType
    ) {
        if (searchService == null) {
            return Result.error("搜索服务暂未启用，请先安装并配置 Elasticsearch");
        }
        return searchService.search(keyword, pageNum, pageSize, sortType);
    }

    /**
     * 搜索建议（自动补全）
     *
     * 根据用户输入的前缀，在文章标题中做 MatchPhrasePrefix 匹配，
     * 返回最多 5 条相关文章标题供下拉选择。
     *
     * @param prefix 输入前缀
     * @return 建议标题列表
     */
    @GetMapping("/suggest")
    public Result suggest(@RequestParam String prefix) {
        if (searchService == null) {
            return Result.error("搜索服务暂未启用，请先安装并配置 Elasticsearch");
        }
        return searchService.suggest(prefix);
    }

    /**
     * 获取热门搜索关键词
     *
     * 从 Redis SortedSet 中按搜索频次倒序取 Top N，
     * 数据由每次搜索操作自动记录。
     *
     * @param topN 取前 N 个，默认 10
     * @return 热搜词列表（keyword + count）
     */
    @GetMapping("/hot-keywords")
    public Result hotKeywords(@RequestParam(required = false, defaultValue = "10") Integer topN) {
        if (searchService == null) {
            return Result.error("搜索服务暂未启用，请先安装并配置 Elasticsearch");
        }
        return searchService.getHotKeywords(topN);
    }
}
