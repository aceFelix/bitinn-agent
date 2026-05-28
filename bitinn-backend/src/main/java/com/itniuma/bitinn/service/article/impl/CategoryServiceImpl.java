package com.itniuma.bitinn.service.article.impl;

import com.itniuma.bitinn.mapper.article.CategoryMapper;
import com.itniuma.bitinn.pojo.entity.Category;
import com.itniuma.bitinn.pojo.vo.Result;
import com.itniuma.bitinn.service.article.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 分类服务实现
 * 提供全部分类的查询能力，基于 MySQL 查询。
 *
 * @author aceFelix
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;

    @Override
    public Result<List<Category>> list() {
        List<Category> categories = categoryMapper.list();
        return Result.success(categories);
    }
}
