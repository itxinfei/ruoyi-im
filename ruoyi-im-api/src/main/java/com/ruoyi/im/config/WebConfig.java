package com.ruoyi.im.config;

import com.ruoyi.im.web.PerformanceLoggingInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web配置类
 * 
 * 配置拦截器和Web相关的设置
 * 
 * @author ruoyi
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Autowired
    private PerformanceLoggingInterceptor performanceLoggingInterceptor;
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册性能日志拦截器
        registry.addInterceptor(performanceLoggingInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/im/user/login"); // 排除登录接口，减少日志噪音
    }
}