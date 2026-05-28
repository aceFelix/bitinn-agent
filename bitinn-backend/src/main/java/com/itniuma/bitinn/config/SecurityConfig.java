package com.itniuma.bitinn.config;

import com.itniuma.bitinn.filter.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 安全配置
 *
 * 认证策略：
 *   - 无状态 JWT 认证（SessionCreationPolicy.STATELESS）
 *   - 禁用 CSRF、FormLogin、HttpBasic、Logout、RememberMe（纯 API 服务）
 *   - JwtAuthenticationFilter 在 UsernamePasswordAuthenticationFilter 之前执行
 *
 * 授权策略：
 *   - 白名单路径（permitAll）：注册、登录、文章查询、搜索、分类/标签、文件上传、OSS、SSE、Swagger、Actuator
 *   - 其余请求需认证（authenticated）
 *
 * @author aceFelix
 */
@Configuration
public class SecurityConfig {

    /**
     * JWT 认证过滤器，拦截所有请求并解析 Token 中的用户身份
     */
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    /**
     * 安全过滤器链
     *
     * 请求匹配顺序：白名单 permitAll → 其余 authenticated
     * 前置过滤：JwtAuthenticationFilter 在认证之前设置 SecurityContext
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .rememberMe(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/user/register",
                                "/api/user/login",
                                "/api/user/info",
                                "/api/article",
                                "/api/article/",
                                "/api/article/my",
                                "/api/article/feed",
                                "/api/article/**",
                                "/api/category/**",
                                "/api/tag/**",
                                "/api/upload",
                                "/api/oss/**",
                                "/api/search/**",
                                "/api/chat/send",
                                "/api/sse/**",
                                "/api/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/v3/api-docs.yaml",
                                "/webjars/**",
                                "/actuator/**",
                                "/error"
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/user/{id}").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/interaction/comment/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/interaction/batch-status").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
                .cors(Customizer.withDefaults());

        return http.build();
    }

    /**
     * 密码编码器
     *
     * 使用 BCrypt 哈希算法，自动加盐。
     * 用户注册/登录时用于密码的加密和比对。
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
