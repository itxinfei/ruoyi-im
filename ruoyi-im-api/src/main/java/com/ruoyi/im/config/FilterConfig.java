package com.ruoyi.im.config;

import com.ruoyi.im.filter.RequestLoggingFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 过滤器配置
 * 配置请求日志、JWT处理等过滤器
 *
 * @author ruoyi
 */
@Configuration
public class FilterConfig {

    @Autowired
    private JwtToUserIdHeaderFilter jwtToUserIdHeaderFilter;

    @Autowired
    private RequestLoggingFilter requestLoggingFilter;

    /**
     * 请求日志过滤器
     * 优先级最高，用于生成追踪ID和记录请求响应日志
     */
    @Bean
    public FilterRegistrationBean<RequestLoggingFilter> requestLoggingFilterRegistration() {
        FilterRegistrationBean<RequestLoggingFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(requestLoggingFilter);
        registration.addUrlPatterns("/api/*"); // 拦截所有API接口
        registration.setName("requestLoggingFilter");
        registration.setOrder(1); // 记录日志时优先保证 userId 已写入请求头
        return registration;
    }

    /**
     * JWT用户ID提取过滤器
     * 从JWT Token中提取用户ID并设置到请求头
     */
    @Bean
    public FilterRegistrationBean<JwtToUserIdHeaderFilter> jwtToUserIdHeaderFilterRegistration() {
        FilterRegistrationBean<JwtToUserIdHeaderFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(jwtToUserIdHeaderFilter);
        registration.addUrlPatterns("/api/im/*"); // 只拦截IM API接口
        registration.setName("jwtToUserIdHeaderFilter");
        registration.setOrder(0); // 设置优先级
        return registration;
    }
}
