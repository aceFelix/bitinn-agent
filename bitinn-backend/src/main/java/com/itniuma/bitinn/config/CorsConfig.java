package com.itniuma.bitinn.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

/**
 * 跨域配置
 *
 * 显式收紧 CORS 策略，替代 SecurityConfig 中默认的 Customizer.withDefaults()。
 * 生产环境通过环境变量 CORS_ALLOWED_ORIGINS 指定允许的前端域名。
 *
 * @author aceFelix
 */
@Configuration
public class CorsConfig {

    /**
     * 允许的跨域来源，开发环境默认 localhost:5173（Vite dev server），
     * 生产环境通过环境变量注入正式域名
     */
    @org.springframework.beans.factory.annotation.Value("${cors.allowed-origins:http://localhost:5173}")
    private String allowedOrigins;

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        String[] origins = allowedOrigins.split(",");
        for (String origin : origins) {
            config.addAllowedOrigin(origin.trim());
        }

        config.setAllowCredentials(true);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("Authorization", "Content-Type"));
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
