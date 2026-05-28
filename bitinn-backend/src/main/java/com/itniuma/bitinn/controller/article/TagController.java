package com.itniuma.bitinn.controller.article;

import com.itniuma.bitinn.mapper.article.TagMapper;
import com.itniuma.bitinn.pojo.vo.Result;
import com.itniuma.bitinn.pojo.entity.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 文章标签控制器
 *
 * 提供标签的查询接口。标签用于文章的分类标记（如 Java、Vue、SpringBoot 等），
 * 多对多关联，支持前端发布文章时选择标签。
 *
 * @author aceFelix
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/tag")
public class TagController {

    /**
     * 标签 Mapper，直接查询 MySQL 标签表
     */
    private final TagMapper tagMapper;

    /**
     * 获取全部标签列表
     *
     * @return 标签列表（id、标签名、颜色），按排序字段升序
     */
    @GetMapping
    public Result<List<Tag>> list() {
        return Result.success(tagMapper.list());
    }
}
