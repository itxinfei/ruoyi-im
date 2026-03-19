package com.ruoyi.im.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * API版本控制配置
 * 支持通过URL路径或请求头进行API版本控制
 *
 * @author ruoyi
 */
@Configuration
public class ApiVersionConfig implements WebMvcConfigurer {

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        // 启用矩阵变量支持
        configurer.setUseTrailingSlashMatch(true);
    }
}