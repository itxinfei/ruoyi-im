package com.ruoyi.im.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;

/**
 * 跨域配置
 *
 * @author ruoyi
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Value("#{'${cors.allowed-origins:http://localhost:3000,http://127.0.0.1:3000}'.split(',')}")
    private List<String> allowedOrigins;

    private static final String[] ALLOWED_HEADERS = {
            "Authorization", "Content-Type", "X-Requested-With",
            "X-CSRF-Token", "Accept", "Origin", "Cache-Control", "UserId"
    };

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(allowedOrigins.toArray(new String[0]))
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD")
                .allowedHeaders(ALLOWED_HEADERS)
                .exposedHeaders(ALLOWED_HEADERS)
                .allowCredentials(true)
                .maxAge(3600);
    }
}