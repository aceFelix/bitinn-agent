package com.itniuma.bitinn.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 异步任务线程池配置
 *
 * 替代 Spring Boot 默认的 SimpleAsyncTaskExecutor（每次新建线程、无上限、不回收），
 * 使用有界线程池 + 拒绝策略，避免高并发时 OOM。
 *
 * 线程池参数说明：
 *   corePoolSize = 4：常驻线程数，匹配 AI 调用这类 IO 密集型任务的并发能力
 *   maxPoolSize = 16：峰值线程数，突发流量时最多扩容到 16 个线程
 *   queueCapacity = 100：等待队列容量，超出时触发拒绝策略
 *   rejectedExecutionHandler = CallerRunsPolicy：拒绝后由调用线程（Tomcat HTTP 线程）执行，
 *     避免消息丢失，同时给上游施加背压
 *
 * @author aceFelix
 */
@Slf4j
@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

    @Bean("aiChatExecutor")
    public Executor aiChatExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(16);
        executor.setQueueCapacity(100);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("ai-chat-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        log.info("AI聊天线程池初始化完成: core={}, max={}, queue={}",
                executor.getCorePoolSize(), executor.getMaxPoolSize(), executor.getQueueCapacity());
        return executor;
    }

    /**
     * 默认 @Async 执行器，供 TitleService、SummaryService 等轻量异步任务使用
     */
    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(100);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("async-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(15);
        executor.initialize();
        return executor;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (ex, method, params) -> {
            log.error("异步任务执行异常, 方法: {}, 参数: {}", method.getName(), params, ex);
        };
    }
}
