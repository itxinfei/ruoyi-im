package com.ruoyi.im.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 过滤器配置
 * 
 * @author ruoyi
 */
@Configuration
public class FilterConfig {

    @Autowired
    private JwtToUserIdHeaderFilter jwtToUserIdHeaderFilter;

    @Bean
    public FilterRegistrationBean<JwtToUserIdHeaderFilter> jwtToUserIdHeaderFilterRegistration() {
        FilterRegistrationBean<JwtToUserIdHeaderFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(jwtToUserIdHeaderFilter);
        registration.addUrlPatterns("/api/im/*"); // 只拦截IM API接口
        registration.setName("jwtToUserIdHeaderFilter");
        registration.setOrder(1); // 设置优先级
        return registration;
    }
}