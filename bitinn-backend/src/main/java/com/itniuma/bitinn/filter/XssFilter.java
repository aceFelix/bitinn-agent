package com.itniuma.bitinn.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * XSS 防护过滤器
 *
 * 对请求参数进行 HTML 转义，防止反射型 XSS 攻击。
 * 仅处理 GET/POST 的查询参数和表单参数，不处理 JSON Body
 * （JSON Body 由 Jackson 反序列化为对象，前端 Vditor 编辑器自带 XSS 防护）。
 *
 * 转义策略：
 *   < → &lt;    > → &gt;    " → &quot;    ' → &#39;    & → &amp;
 *
 * @author aceFelix
 */
@Slf4j
@Component
@Order(1)
public class XssFilter implements Filter {

    /**
     * 拦截所有请求，将原始 HttpServletRequest 包装为 XssRequestWrapper，
     * 后续 Controller 通过 getParameter() 获取参数时自动完成 HTML 转义
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        chain.doFilter(new XssRequestWrapper((HttpServletRequest) request), response);
    }

    /**
     * 包装 HttpServletRequest，对参数值进行 HTML 转义
     */
    private static class XssRequestWrapper extends HttpServletRequestWrapper {

        public XssRequestWrapper(HttpServletRequest request) {
            super(request);
        }

        /**
         * 获取单个参数值，自动转义 HTML 特殊字符
         */
        @Override
        public String getParameter(String name) {
            String value = super.getParameter(name);
            return sanitize(value);
        }

        /**
         * 获取同名参数数组，逐项转义 HTML 特殊字符
         */
        @Override
        public String[] getParameterValues(String name) {
            String[] values = super.getParameterValues(name);
            if (values == null) return null;
            String[] sanitized = new String[values.length];
            for (int i = 0; i < values.length; i++) {
                sanitized[i] = sanitize(values[i]);
            }
            return sanitized;
        }

        /**
         * 获取全部参数映射，逐项转义 HTML 特殊字符
         */
        @Override
        public Map<String, String[]> getParameterMap() {
            Map<String, String[]> map = super.getParameterMap();
            return map.entrySet().stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            entry -> {
                                String[] values = entry.getValue();
                                String[] sanitized = new String[values.length];
                                for (int i = 0; i < values.length; i++) {
                                    sanitized[i] = sanitize(values[i]);
                                }
                                return sanitized;
                            }
                    ));
        }

        /**
         * 对参数值进行 HTML 特殊字符转义（< > " ' &），防止反射型 XSS 攻击
         * @param value 原始参数值
         * @return 转义后的安全字符串
         */
        private String sanitize(String value) {
            if (value == null || value.isEmpty()) return value;
            return HtmlUtils.htmlEscape(value);
        }
    }
}
