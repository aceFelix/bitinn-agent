package com.itniuma.bitinn.controller.aichat;

import com.itniuma.bitinn.utils.JwtUtil;
import com.itniuma.bitinn.utils.RedisCacheHelper;
import com.itniuma.bitinn.utils.SSEServer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

/**
 * SSE（Server-Sent Events）连接控制器
 *
 * 前端通过 EventSource API 建立长连接，接收 AI 回复的实时推送。
 * SSE 是单向通信（服务端 → 客户端），适合流式 AI 回复场景。
 *
 * 为什么 Token 走 URL 参数？
 *   EventSource 不支持自定义请求头，无法像 Axios 那样在 Header 里放 Authorization，
 *   因此将 JWT Token 作为 URL 查询参数传递，服务端手动验证。
 *
 * 连接生命周期：
 *   前端 connectSSE() → /api/sse/connect?userId=&token=
 *     → SSEServer.connect() 创建 SseEmitter 并缓存
 *     → ChatAsyncService 通过 SSEServer.sendMessage() 推送 chunk
 *     → 连接超时或前端断开时自动移除
 *
 * @author aceFelix
 */
@Slf4j
@RestController
@RequestMapping("/api/sse")
@RequiredArgsConstructor
public class SSEController {
    private final RedisCacheHelper redisCache;

    /** Token 黑名单 Redis Key 前缀，用于识别已登出的 Token */
    private static final String TOKEN_BLACKLIST_PREFIX = "token:blacklist:";

    /**
     * 建立 SSE 连接
     *
     * 验证 Token 有效性（格式 + 未拉黑）后创建 SseEmitter，
     * 后续 AI 回复的每个 chunk 都通过此连接实时推送给前端。
     *
     * @param userId 用户 ID
     * @param token  JWT Token（从 URL 参数传递，因为 EventSource 不支持自定义请求头）
     * @return SseEmitter 标准 SSE 响应流
     */
    @GetMapping("/connect")
    public SseEmitter connect(@RequestParam String userId, @RequestParam(required = false) String token) {
        if (token == null || token.isEmpty()) {
            throw new RuntimeException("Invalid token");
        }
        try {
            Map<String, Object> claims = JwtUtil.parseToken(token);
            String blacklistKey = TOKEN_BLACKLIST_PREFIX + token;
            String blacklisted = redisCache.get(blacklistKey);
            if (blacklisted != null) {
                throw new RuntimeException("Token has been invalidated");
            }
        } catch (Exception e) {
            log.warn("SSE连接token验证失败: {}", e.getMessage());
            throw new RuntimeException("Invalid token");
        }
        return SSEServer.connect(userId);
    }

}
