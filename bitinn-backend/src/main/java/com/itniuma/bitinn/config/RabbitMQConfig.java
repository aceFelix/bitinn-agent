package com.itniuma.bitinn.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 配置 - 跨存储数据同步
 *
 * 定义用于文章数据同步的 Exchange、Queue 和 Binding，
 * 实现 MySQL → Redis → Elasticsearch 之间的异步数据一致性。
 *
 * 架构：
 *   DataSyncProducer（生产者）
 *     → Exchange: bitinn.data.sync (DirectExchange)
 *       ├─ Queue: article.count.sync  (routingKey: article.count)
 *       └─ Queue: article.es.sync     (routingKey: article.es)
 *           → DataSyncConsumer（消费者）
 *
 * 消息格式：JSON（由 Jackson2JsonMessageConverter 序列化）
 * 所有队列声明为持久化（durable=true），服务重启消息不丢失。
 *
 * @author aceFelix
 */
@Configuration
public class RabbitMQConfig {

    /**
     * 交换机名称，来自配置文件 rabbitmq.exchanges.data-sync
     */
    @Value("${rabbitmq.exchanges.data-sync}")
    private String dataSyncExchange;

    /**
     * 文章计数同步队列名，来自配置文件 rabbitmq.queues.article-count-sync
     */
    @Value("${rabbitmq.queues.article-count-sync}")
    private String articleCountSyncQueue;

    /**
     * ES 文档同步队列名，来自配置文件 rabbitmq.queues.es-sync
     */
    @Value("${rabbitmq.queues.es-sync}")
    private String esSyncQueue;

    /**
     * JSON 消息转换器
     *
     * 替代 Spring AMQP 默认的 SimpleMessageConverter（仅支持 byte[]/String），
     * 使 RabbitTemplate 可以自动将 Map<String, Object> 序列化为 JSON 发送。
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * 直连交换机（Direct Exchange）
     *
     * 根据 routing key 精确匹配队列：
     *   "article.count" → 文章计数同步队列
     *   "article.es"    → ES 文档同步队列
     */
    @Bean
    public DirectExchange dataSyncExchange() {
        return new DirectExchange(dataSyncExchange, true, false);
    }

    /**
     * 文章计数同步队列（持久化）
     *
     * 消费 MySQL 中 like_count / comment_count / favorite_count 变更，
     * 同步到 Redis 和 ES。
     */
    @Bean
    public Queue articleCountSyncQueue() {
        return new Queue(articleCountSyncQueue, true);
    }

    /**
     * ES 文档同步队列（持久化）
     *
     * 消费文章的创建、更新、删除事件，同步或移除 ES 索引。
     */
    @Bean
    public Queue esSyncQueue() {
        return new Queue(esSyncQueue, true);
    }

    /**
     * 绑定：article.count → 文章计数同步队列
     */
    @Bean
    public Binding articleCountSyncBinding() {
        return BindingBuilder.bind(articleCountSyncQueue())
                .to(dataSyncExchange())
                .with("article.count");
    }

    /**
     * 绑定：article.es → ES 文档同步队列
     */
    @Bean
    public Binding esSyncBinding() {
        return BindingBuilder.bind(esSyncQueue())
                .to(dataSyncExchange())
                .with("article.es");
    }
}
