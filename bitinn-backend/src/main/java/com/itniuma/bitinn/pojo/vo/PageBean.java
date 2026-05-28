package com.itniuma.bitinn.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 分页返回结果对象
 *
 * 封装分页查询的通用返回格式，包含总数和当前页数据。
 * 用于所有分页接口（文章列表、Feed 流、搜索等）的统一响应。
 *
 * @author aceFelix
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageBean<T> {
    /** 总条数 */
    private Long total;
    /** 当前页数据集合 */
    private List<T> items;
}
