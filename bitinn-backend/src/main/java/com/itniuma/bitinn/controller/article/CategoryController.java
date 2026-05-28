package com.itniuma.bitinn.controller.article;

import com.itniuma.bitinn.pojo.entity.Category;
import com.itniuma.bitinn.pojo.vo.Result;
import com.itniuma.bitinn.service.article.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 文章分类控制器
 *
 * 提供文章分类的查询接口。分类数据较少、变化不频繁，
 * 前端通过 cachedRequest 缓存 10 分钟，减少重复请求。
 *
 * @author aceFelix
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/category")
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * 获取全部分类列表
     *
     * @return 分类列表（id、分类名、别名），按排序字段升序
     */
    @GetMapping
    public Result<List<Category>> list() {
        return categoryService.list();
    }
}
