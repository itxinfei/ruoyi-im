package com.ruoyi.im.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.File;

/**
 * 文件上传配置类
 * 统一管理文件上传路径和初始化上传目录
 * <p>
 * 路径说明：
 * - 开发环境：文件上传到项目根目录下的 uploads/ 目录
 * - 生产环境：文件上传到 JAR 包所在目录下的 uploads/ 目录
 *
 * @author ruoyi
 */
@Configuration
public class FileUploadConfig {

    private static final Logger logger = LoggerFactory.getLogger(FileUploadConfig.class);

    /** 静态实例，用于静态方法调用 */
    private static FileUploadConfig instance;

    /**
     * 上传文件的基础路径（相对于项目根目录或 JAR 包所在目录）
     */
    @Value("${file.upload.path:uploads/}")
    private String uploadPath;

    /**
     * 文件访问的URL前缀
     */
    @Value("${file.upload.url-prefix:/uploads/}")
    private String urlPrefix;

    /**
     * 初始化上传目录
     */
    @PostConstruct
    public void init() {
        instance = this;
        String absolutePath = getAbsoluteUploadPath();
        File uploadDir = new File(absolutePath);

        if (!uploadDir.exists()) {
            boolean created = uploadDir.mkdirs();
            if (created) {
                logger.info("创建上传目录成功: {}", absolutePath);
            } else {
                logger.error("创建上传目录失败: {}", absolutePath);
            }
        } else {
            logger.info("上传目录已存在: {}", absolutePath);
        }

        // 创建子目录
        createSubDirectory(absolutePath, "avatar");
        createSubDirectory(absolutePath, "file");
        createSubDirectory(absolutePath, "chunks");
        createSubDirectory(absolutePath, "cloud");
        createSubDirectory(absolutePath, "emoji");
        createSubDirectory(absolutePath, "email");
    }

    /**
     * 创建子目录
     *
     * @param parentPath 父目录路径
     * @param subDir     子目录名称
     */
    private void createSubDirectory(String parentPath, String subDir) {
        File dir = new File(parentPath + File.separator + subDir);
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            if (created) {
                logger.info("创建子目录成功: {}/{}", parentPath, subDir);
            }
        }
    }

    /**
     * 获取上传文件的绝对路径
     * <p>
     * 规则：
     * - 如果配置的路径是绝对路径（以 / 或 Windows 盘符开头），直接使用
     * - 否则，相对于项目/JAR 包所在目录
     *
     * @return 绝对路径
     */
    public String getAbsoluteUploadPath() {
        File pathFile = new File(uploadPath);

        // 如果已经是绝对路径，直接使用
        if (pathFile.isAbsolute()) {
            return normalizePath(uploadPath);
        }

        // 相对路径：基于当前工作目录（JAR 包所在目录或项目根目录）
        String workingDir = System.getProperty("user.dir");
        String fullPath = workingDir + File.separator + uploadPath;

        return normalizePath(fullPath);
    }

    /**
     * 标准化路径，处理路径分隔符
     *
     * @param path 原始路径
     * @return 标准化后的路径
     */
    private String normalizePath(String path) {
        File file = new File(path);
        return file.getAbsolutePath();
    }

    /**
     * 获取上传路径（相对路径）
     *
     * @return 相对路径
     */
    public String getUploadPath() {
        return uploadPath;
    }

    /**
     * 获取URL前缀
     *
     * @return URL前缀
     */
    public String getUrlPrefix() {
        return urlPrefix;
    }

    /**
     * 构建完整的文件访问URL
     *
     * @param relativePath 相对路径
     * @return 完整URL
     */
    public String buildFileUrl(String relativePath) {
        if (relativePath == null || relativePath.isEmpty()) {
            return "";
        }
        // 如果已经是完整URL，直接返回
        if (relativePath.startsWith("http://") || relativePath.startsWith("https://")) {
            return relativePath;
        }
        // 移除开头的斜杠避免重复
        String path = relativePath.startsWith("/") ? relativePath.substring(1) : relativePath;
        String prefix = urlPrefix.endsWith("/") ? urlPrefix : urlPrefix + "/";
        return prefix + path;
    }

    /**
     * 静态方法：获取上传文件的绝对路径
     * 用于非 Spring 管理的类中调用
     *
     * @return 绝对路径
     */
    public static String getAbsoluteUploadPathStatic() {
        if (instance == null) {
            return "uploads/";
        }
        return instance.getAbsoluteUploadPath();
    }
}
