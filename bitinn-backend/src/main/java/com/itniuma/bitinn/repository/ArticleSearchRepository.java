package com.itniuma.bitinn.repository;

import com.itniuma.bitinn.pojo.document.ArticleDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * 文章搜索 Repository
 * 基于 Elasticsearch 实现文章全文检索，继承 ElasticsearchRepository 提供基础 CRUD 及分页查询能力。
 * 索引实体为 ArticleDocument，主键类型为 Integer（对应文章ID）。
 *
 * @author aceFelix
 */
public interface ArticleSearchRepository extends ElasticsearchRepository<ArticleDocument, Integer> {
}
