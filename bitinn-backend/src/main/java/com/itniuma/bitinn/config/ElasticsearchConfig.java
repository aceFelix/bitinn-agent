package com.itniuma.bitinn.config;

import com.itniuma.bitinn.pojo.document.ArticleDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

import jakarta.annotation.PostConstruct;

/**
 * Elasticsearch 配置
 *
 * 负责文章搜索索引（bitinn-article）的自动创建和映射管理。
 * 项目启动时自动检查索引是否存在：
 *   - 不存在 → 根据 ArticleDocument 注解创建索引（含 IK 中文分词器映射）
 *   - 已存在 → 更新映射，适应字段变更
 *
 * Spring Data Elasticsearch 仓库扫描路径：
 *   com.itniuma.bitinn.repository（ArticleSearchRepository）
 *
 * @author aceFelix
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableElasticsearchRepositories(basePackages = "com.itniuma.bitinn.repository")
public class ElasticsearchConfig {

    /**
     * Elasticsearch 底层操作模板，提供索引管理和文档操作能力
     */
    private final ElasticsearchOperations esOps;

    /**
     * 启动时初始化文章搜索索引
     *
     * 使用 ArticleDocument 上的 @Document、@Field 注解自动生成映射，
     * 标题和内容字段应用 IK 分词器（ik_max_word 索引时 / ik_smart 搜索时）。
     */
    @PostConstruct
    public void initIndex() {
        try {
            IndexOperations indexOps = esOps.indexOps(ArticleDocument.class);
            if (!indexOps.exists()) {
                indexOps.createWithMapping();
                log.info("[ES] 索引 bitinn-article 创建成功，已应用 IK 分词器映射");
            } else {
                indexOps.putMapping();
                log.info("[ES] 索引 bitinn-article 已存在，映射已更新");
            }
        } catch (Exception e) {
            log.error("[ES] 索引初始化失败", e);
        }
    }
}
