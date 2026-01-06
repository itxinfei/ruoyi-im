package com.ruoyi.im.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/** 
 * IM API版本控制配置
 * 
 * 提供API版本控制的拦截器配置，支持通过Header、URL路径等方式进行版本控制。
 * 默认版本为v1，支持版本回退和版本兼容性检查。
 * 
 * @author ruoyi
 */
@Configuration
public class ImApiVersionConfig implements WebMvcConfigurer {

    /**
     * API版本控制拦截器
     */
    @Bean
    public ApiVersionInterceptor apiVersionInterceptor() {
        return new ApiVersionInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(apiVersionInterceptor())
                .addPathPatterns("/api/**");
    }

    /**
     * API版本控制拦截器
     */
    public static class ApiVersionInterceptor extends HandlerInterceptorAdapter {

        private static final String DEFAULT_VERSION = "v1";
        private static final String VERSION_HEADER = "X-API-Version";
        private static final String VERSION_ATTRIBUTE = "apiVersion";

        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
            String version = getVersionFromRequest(request);
            request.setAttribute(VERSION_ATTRIBUTE, version);
            return true;
        }

        /**
         * 从请求中获取API版本
         * 支持多种方式：
         * 1. X-API-Version 请求头
         * 2. URL路径中的版本号（如 /api/v1/xxx）
         * 3. 默认版本 v1
         */
        private String getVersionFromRequest(HttpServletRequest request) {
            // 1. 从请求头获取版本
            String headerVersion = request.getHeader(VERSION_HEADER);
            if (isValidVersion(headerVersion)) {
                return headerVersion;
            }

            // 2. 从URL路径获取版本
            String requestUri = request.getRequestURI();
            String pathVersion = extractVersionFromPath(requestUri);
            if (isValidVersion(pathVersion)) {
                return pathVersion;
            }

            // 3. 返回默认版本
            return DEFAULT_VERSION;
        }

        /**
         * 从URL路径中提取版本号
         */
        private String extractVersionFromPath(String uri) {
            // 匹配 /api/v1/xxx 或 /api/v2/xxx 等模式
            String[] parts = uri.split("/");
            for (int i = 0; i < parts.length - 1; i++) {
                if ("api".equals(parts[i]) && isVersion(parts[i + 1])) {
                    return parts[i + 1];
                }
            }
            return null;
        }

        /**
         * 检查字符串是否为有效的版本号
         */
        private boolean isVersion(String str) {
            return str != null && str.matches("v\\d+");
        }

        /**
         * 检查版本号是否有效
         */
        private boolean isValidVersion(String version) {
            return version != null && isVersion(version);
        }
    }
}