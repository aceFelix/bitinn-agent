package com.itniuma.bitinn.filter;

import com.itniuma.bitinn.utils.JwtUtil;
import com.itniuma.bitinn.utils.RedisCacheHelper;
import com.itniuma.bitinn.utils.ThreadLocalUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

/**
 * JWT 认证过滤器
 *
 * 拦截所有 HTTP 请求，从 Authorization Header 中提取 JWT Token，
 * 解析用户身份并存入 SecurityContextHolder，供后续 Controller 通过 ThreadLocalUtil 获取。
 *
 * 优化策略：
 *   1. 先本地解析 JWT（零网络开销，JWT 自带签名校验）
 *   2. 再检查 Redis 黑名单（仅在 Token 被主动失效时才命中，如修改密码、登出）
 *   3. 正常请求只需 1 次 JWT 解析，无需每次都查 Redis
 *
 * 为什么继承 OncePerRequestFilter？
 *   保证每个请求只过滤一次，避免在请求转发（如 forward、include）时重复执行。
 *
 * @author aceFelix
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    /**
     * Token 黑名单 Redis Key 前缀
     * 用户登出或修改密码时，将旧 Token 加入黑名单使其立即失效
     */
    private static final String TOKEN_BLACKLIST_PREFIX = "token:blacklist:";

    /**
     * Redis 缓存工具，用于检查 Token 是否在黑名单中
     */
    private final RedisCacheHelper redisCache;

    /**
     * 核心过滤逻辑
     *
     * 执行流程：
     *   1. 提取 Authorization Header 中的 Token
     *   2. Token 为空 → 直接放行（由 Spring Security 授权机制处理白名单/认证要求）
     *   3. 解析 JWT → 检查黑名单 → 存入 ThreadLocal + SecurityContext
     *   4. 解析失败 → 记录日志后放行（不阻断未认证请求）
     *   5. finally 中清理 ThreadLocal（防止内存泄漏）
     *
     * @param request     HTTP 请求
     * @param response    HTTP 响应
     * @param filterChain 过滤器链
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        String token = request.getHeader("Authorization");

        if (token == null || token.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 第一步：本地解析 JWT（零网络开销）
            Map<String, Object> claims = JwtUtil.parseToken(token);

            // 第二步：检查黑名单（仅在 token 被主动失效时才命中，如修改密码、登出）
            String blacklistKey = TOKEN_BLACKLIST_PREFIX + token;
            String blacklisted = redisCache.get(blacklistKey);
            if (blacklisted != null) {
                log.debug("Token 已被加入黑名单: {}", token.substring(0, Math.min(token.length(), 20)) + "...");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            ThreadLocalUtil.set(claims);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(claims, null, new ArrayList<>());
            SecurityContextHolder.getContext().setAuthentication(authentication);

            filterChain.doFilter(request, response);
        } catch (Exception e) {
            // JWT 过期、签名错误等情况，不阻断请求，交给 Spring Security 授权机制处理
            log.debug("JWT 解析失败: {}, 放行请求由Spring Security授权机制处理", e.getMessage());
            filterChain.doFilter(request, response);
        } finally {
            // 清理 ThreadLocal，防止线程池复用时造成内存泄漏和数据串扰
            ThreadLocalUtil.remove();
        }
    }
}
