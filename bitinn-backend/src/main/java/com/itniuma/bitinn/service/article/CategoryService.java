package com.itniuma.bitinn.service.article;

import com.itniuma.bitinn.pojo.entity.Category;
import com.itniuma.bitinn.pojo.vo.Result;

import java.util.List;

/**
 * 分类服务接口
 * 定义文章分类的查询契约。
 *
 * @author aceFelix
 */
public interface CategoryService {

    /**
     * 获取全部分类列表
     */
    Result<List<Category>> list();
}
