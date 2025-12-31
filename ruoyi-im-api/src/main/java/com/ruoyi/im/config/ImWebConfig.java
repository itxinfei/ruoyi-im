package com.ruoyi.im.config;

import com.ruoyi.im.interceptor.ImAuthInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * IM模块Web配置
 * 
 * 配置IM模块相关的拦截器和路径映射
 * 
 * @author ruoyi
 * @date 2024-08-09
 */
@Configuration
public class ImWebConfig implements WebMvcConfigurer
{
    @Autowired
    private ImAuthInterceptor imAuthInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry)
    {
        // 添加IM模块的认证拦截器，处理 /im/** 路径的请求
        registry.addInterceptor(imAuthInterceptor)
                .addPathPatterns("/im/**")
                .excludePathPatterns(
                    "/im/login",           // 登录接口不需要拦截
                    "/im/register",        // 注册接口不需要拦截
                    "/im/public/**",       // 公共接口不需要拦截
                    "/im/websocket/**",    // WebSocket接口不需要拦截
                    "/swagger-ui/**",      // Swagger UI不拦截
                    "/swagger-resources/**", // Swagger资源不拦截
                    "/v2/api-docs",        // Swagger API文档不拦截
                    "/error"               // 错误页面不拦截
                );
    }
}