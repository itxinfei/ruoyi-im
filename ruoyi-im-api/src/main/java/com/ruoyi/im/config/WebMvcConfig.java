package com.ruoyi.im.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

/**
 * Web MVC 配置
 * 配置静态资源访问路径
 *
 * @author ruoyi
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private static final String PROJECT_MODULE_NAME = "ruoyi-im-api";
    private static final String FILE_PROTOCOL_PREFIX = "file:";

    @Value("${file.upload.path:src/main/resources/uploads/}")
    private String uploadPath;

    /**
     * 配置静态资源处理
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 获取项目根目录的绝对路径
        String projectRoot = System.getProperty("user.dir");
        String absoluteUploadPath = projectRoot + File.separator + PROJECT_MODULE_NAME + File.separator + uploadPath;
        // 确保路径格式正确（Windows使用\，Unix使用/）
        if (!uploadPath.startsWith("/") && !uploadPath.contains(":")) {
            absoluteUploadPath = FILE_PROTOCOL_PREFIX + absoluteUploadPath.replace("\\", "/");
        } else {
            absoluteUploadPath = FILE_PROTOCOL_PREFIX + uploadPath;
        }

        // 配置头像资源映射
        registry.addResourceHandler("/avatar/**")
                .addResourceLocations("classpath:/static/avatar/");

        // 配置上传文件资源映射 - 支持文件下载
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(absoluteUploadPath);

        // 配置API文件下载路径映射
        registry.addResourceHandler("/api/im/file/download/**")
                .addResourceLocations(absoluteUploadPath);

        // 配置其他静态资源
        registry.addResourceHandler("/profile/**")
                .addResourceLocations("classpath:/static/profile/");

        // 配置 favicon
        registry.addResourceHandler("/favicon.ico")
                .addResourceLocations("classpath:/static/");
    }
}
