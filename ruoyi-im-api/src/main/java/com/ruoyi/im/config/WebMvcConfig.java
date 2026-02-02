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

    @Value("${file.upload.path:src/main/resources/uploads/}")
    private String uploadPath;

    /**
     * 获取上传文件的绝对路径
     */
    private String getAbsoluteUploadPath() {
        String projectRoot = System.getProperty("user.dir");
        String apiModulePath = projectRoot + File.separator + "ruoyi-im-api";
        String fullPath = apiModulePath + File.separator + uploadPath;

        // 确保目录存在
        File dir = new File(fullPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        return fullPath;
    }

    /**
     * 配置静态资源处理
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 获取上传文件的绝对路径
        String absoluteUploadPath = getAbsoluteUploadPath();
        // 确保路径格式正确（Windows使用\，Unix使用/）
        String resourceLocation = "file:" + absoluteUploadPath.replace("\\", "/");

        // 配置头像资源映射
        registry.addResourceHandler("/avatar/**")
                .addResourceLocations("classpath:/static/avatar/");

        // 配置上传文件资源映射 - 支持文件下载
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(resourceLocation);

        // 配置API文件下载路径映射
        registry.addResourceHandler("/api/im/file/download/**")
                .addResourceLocations(resourceLocation);

        // 配置其他静态资源
        registry.addResourceHandler("/profile/**")
                .addResourceLocations("classpath:/static/profile/");

        // 配置 favicon
        registry.addResourceHandler("/favicon.ico")
                .addResourceLocations("classpath:/static/");
    }
}
