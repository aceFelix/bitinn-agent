package com.itniuma.bitinn.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * API 全局限流拦截器
 *
 * 基于滑动窗口的简单限流，按 IP 维度控制请求频率，
 * 防止恶意爬虫或单点过载打垮服务。
 *
 * 限流策略：
 *   全局：每 IP 每分钟最多 120 次请求（平均 2 req/s）
 *   登录/注册：每 IP 每分钟最多 20 次（防止暴力破解）
 *
 * 注意：此实现为单机内存限流，多实例部署时需替换为 Redis 分布式限流。
 *
 * @author aceFelix
 */
@Slf4j
@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    /** 全局接口：每 IP 每分钟最大请求数 */
    private static final int GLOBAL_LIMIT = 120;

    /** 敏感接口（登录/注册）：每 IP 每分钟最大请求数 */
    private static final int SENSITIVE_LIMIT = 20;

    /** 时间窗口（毫秒） */
    private static final long WINDOW_MS = TimeUnit.MINUTES.toMillis(1);

    /**
     * 滑动窗口计数器
     * key: ip::path, value: { windowStart, count }
     */
    private final Map<String, WindowCounter> counters = new ConcurrentHashMap<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String ip = getClientIp(request);
        String path = request.getRequestURI();

        int limit = isSensitivePath(path) ? SENSITIVE_LIMIT : GLOBAL_LIMIT;

        String key = ip + "::" + (isSensitivePath(path) ? "sensitive" : "global");
        WindowCounter counter = counters.computeIfAbsent(key, k -> new WindowCounter());

        long now = System.currentTimeMillis();

        synchronized (counter) {
            // 窗口过期则重置
            if (now - counter.windowStart > WINDOW_MS) {
                counter.windowStart = now;
                counter.count = 0;
            }

            counter.count++;

            if (counter.count > limit) {
                log.warn("[限流] IP: {}, 路径: {}, 计数: {}/{}", ip, path, counter.count, limit);
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":429,\"message\":\"请求过于频繁，请稍后再试\"}");
                return false;
            }
        }

        return true;
    }

    /**
     * 判断是否为敏感路径（需要更严格的限流）
     */
    private boolean isSensitivePath(String path) {
        return path.contains("/login") || path.contains("/register");
    }

    /**
     * 获取客户端真实 IP，优先从 Nginx 反向代理头获取
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // X-Forwarded-For 可能包含多级代理 IP，取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    /**
     * 滑动窗口计数器
     */
    private static class WindowCounter {
        long windowStart = System.currentTimeMillis();
        int count = 0;
    }
}
