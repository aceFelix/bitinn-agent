package com.itniuma.bitinn.listener;

import com.itniuma.bitinn.utils.RedisCacheHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * 连接池预热监听器
 *
 * 监听 ApplicationReadyEvent（Spring 容器完全就绪后触发），
 * 在应用启动完成后主动触发数据库和 Redis 的连接初始化，
 * 避免第一个用户请求因懒加载连接池而造成延迟。
 *
 * 预热的顺序和目的：
 *   1. 数据库连接池（HikariCP）：执行 5 次 SELECT 1，建立最小连接数
 *   2. Redis 连接池（Lettuce）：执行 3 次 GET，预热 TCP 连接
 *   3. 热点表查询：提前加载 MySQL 的查询计划到缓存
 *
 * @author aceFelix
 */
@Slf4j
@Component
public class WarmupListener implements ApplicationListener<ApplicationReadyEvent> {

    /**
     * JDBC 操作模板，用于执行数据库预热查询
     */
    private final JdbcTemplate jdbcTemplate;

    /**
     * Redis 缓存工具，用于执行 Redis 预热查询
     */
    private final RedisCacheHelper redisCache;

    public WarmupListener(JdbcTemplate jdbcTemplate, RedisCacheHelper redisCache) {
        this.jdbcTemplate = jdbcTemplate;
        this.redisCache = redisCache;
    }

    /**
     * 应用就绪后执行预热
     *
     * ApplicationReadyEvent 在 CommandLineRunner 和 ApplicationRunner 之后触发，
     * 此时所有 Bean 已初始化完毕，是最安全的预热时机。
     */
    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        log.info("开始预热数据库和Redis连接...");

        warmupDatabase();
        warmupRedis();
        warmupTables();

        log.info("所有连接池预热完成，服务已就绪！");
    }

    /**
     * 数据库连接池预热
     *
     * 连续执行 5 次 SELECT 1，触发 HikariCP 建立 minimum-idle 数量的连接。
     * 每次执行间隔 100ms，给连接创建留出时间。
     */
    private void warmupDatabase() {
        for (int i = 0; i < 5; i++) {
            try {
                jdbcTemplate.queryForObject("SELECT 1", Integer.class);
                Thread.sleep(100);
            } catch (Exception e) {
                log.warn("数据库连接预热第{}次失败: {}", i + 1, e.getMessage());
            }
        }
        log.info("数据库连接预热完成（5次）");
    }

    /**
     * Redis 连接池预热
     *
     * 连续执行 3 次 GET，触发 Lettuce 建立 Redis TCP 连接。
     * 使用不存在的 warmup:test key，避免读业务数据。
     */
    private void warmupRedis() {
        for (int i = 0; i < 3; i++) {
            try {
                redisCache.get("warmup:test");
                Thread.sleep(50);
            } catch (Exception e) {
                log.warn("Redis连接预热第{}次失败: {}", i + 1, e.getMessage());
            }
        }
        log.info("Redis连接预热完成（3次）");
    }

    /**
     * 热点表预热
     *
     * 执行 article 和 user 表的 COUNT 查询，让 MySQL 将索引和数据页加载到 Buffer Pool，
     * 同时让 MyBatis 的 MappedStatement 完成解析和缓存。
     */
    private void warmupTables() {
        try {
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM article", Long.class);
            log.info("文章表查询预热完成");
        } catch (Exception e) {
            log.warn("文章表查询预热失败: {}", e.getMessage());
        }

        try {
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM user", Long.class);
            log.info("用户表查询预热完成");
        } catch (Exception e) {
            log.warn("用户表查询预热失败: {}", e.getMessage());
        }
    }
}
