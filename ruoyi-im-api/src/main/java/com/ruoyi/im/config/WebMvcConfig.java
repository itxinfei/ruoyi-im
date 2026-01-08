package com.ruoyi.im.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置
 * 配置静态资源访问路径
 *
 * @author ruoyi
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * 配置静态资源处理
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 配置头像资源映射
        registry.addResourceHandler("/avatar/**")
                .addResourceLocations("classpath:/static/avatar/");

        // 配置上传文件资源映射
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");

        // 配置其他静态资源
        registry.addResourceHandler("/profile/**")
                .addResourceLocations("classpath:/static/profile/");

        // 配置 favicon
        registry.addResourceHandler("/favicon.ico")
                .addResourceLocations("classpath:/static/");
    }
}
