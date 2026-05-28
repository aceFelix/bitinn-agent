package com.itniuma.bitinn.mapper.article;

import com.itniuma.bitinn.pojo.entity.Category;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 文章分类 Mapper
 *
 * MyBatis 数据访问接口，SQL 定义在 CategoryMapper.xml 中。
 * 分类数据较少（一般不超过 20 条），前端通过 cachedRequest 缓存 10 分钟。
 *
 * @author aceFelix
 */
@Mapper
public interface CategoryMapper {

    /** 获取全部分类列表，按排序字段升序 */
    List<Category> list();

    /** 根据主键查询分类 */
    Category findById(Integer id);
}
