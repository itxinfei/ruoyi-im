package com.ruoyi.im.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 文件上传配置测试类
 *
 * @author ruoyi
 */
@SpringBootTest
public class FileUploadConfigTest {

    @Autowired
    private FileUploadConfig fileUploadConfig;

    /**
     * 测试获取上传路径
     */
    @Test
    public void testGetUploadPath() {
        String uploadPath = fileUploadConfig.getUploadPath();
        assertNotNull(uploadPath);
        assertTrue(uploadPath.contains("uploads"));
        System.out.println("上传路径: " + uploadPath);
    }

    /**
     * 测试获取绝对路径
     */
    @Test
    public void testGetAbsoluteUploadPath() {
        String absolutePath = fileUploadConfig.getAbsoluteUploadPath();
        assertNotNull(absolutePath);
        assertTrue(absolutePath.contains("uploads"));

        // 验证目录是否存在
        File dir = new File(absolutePath);
        assertTrue(dir.exists(), "上传目录应该存在");
        assertTrue(dir.isDirectory(), "上传路径应该是目录");

        System.out.println("绝对路径: " + absolutePath);
    }

    /**
     * 测试获取URL前缀
     */
    @Test
    public void testGetUrlPrefix() {
        String urlPrefix = fileUploadConfig.getUrlPrefix();
        assertNotNull(urlPrefix);
        assertTrue(urlPrefix.startsWith("/"));
        System.out.println("URL前缀: " + urlPrefix);
    }

    /**
     * 测试构建文件URL
     */
    @Test
    public void testBuildFileUrl() {
        // 测试相对路径
        String url1 = fileUploadConfig.buildFileUrl("avatar/test.jpg");
        assertNotNull(url1);
        assertTrue(url1.contains("/uploads/"));
        System.out.println("文件URL 1: " + url1);

        // 测试带斜杠的路径
        String url2 = fileUploadConfig.buildFileUrl("/avatar/test.jpg");
        assertNotNull(url2);
        System.out.println("文件URL 2: " + url2);

        // 测试完整URL（应该直接返回）
        String fullUrl = "http://example.com/test.jpg";
        String url3 = fileUploadConfig.buildFileUrl(fullUrl);
        assertEquals(fullUrl, url3);
        System.out.println("文件URL 3: " + url3);
    }

    /**
     * 测试子目录是否存在
     */
    @Test
    public void testSubDirectories() {
        String basePath = fileUploadConfig.getAbsoluteUploadPath();

        String[] subDirs = {"avatar", "file", "chunks", "cloud", "emoji"};
        for (String subDir : subDirs) {
            File dir = new File(basePath + File.separator + subDir);
            assertTrue(dir.exists(), subDir + " 目录应该存在");
            assertTrue(dir.isDirectory(), subDir + " 应该是目录");
            System.out.println("子目录 " + subDir + " 存在: " + dir.getAbsolutePath());
        }
    }
}
