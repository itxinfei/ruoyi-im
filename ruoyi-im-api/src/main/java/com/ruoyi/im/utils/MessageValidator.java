package com.ruoyi.im.utils;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 消息验证器
 * 
 * @author ruoyi
 */
@Component
public class MessageValidator {
    
    private static final int MAX_TEXT_LENGTH = 5000;
    private static final int MAX_FILE_SIZE = 50 * 1024 * 1024; // 50MB
    
    /**
     * 验证文本消息
     */
    public void validateTextMessage(String content) {
        if (!StringUtils.hasText(content)) {
            throw new RuntimeException("消息内容不能为空");
        }
        
        if (content.length() > MAX_TEXT_LENGTH) {
            throw new RuntimeException("消息内容过长，最大支持" + MAX_TEXT_LENGTH + "个字符");
        }
        
        // 检查是否包含敏感词
        if (containsSensitiveWords(content)) {
            throw new RuntimeException("消息包含敏感词汇");
        }
    }
    
    /**
     * 验证文件消息
     */
    public void validateFileMessage(String fileName, long fileSize, String fileType) {
        if (!StringUtils.hasText(fileName)) {
            throw new RuntimeException("文件名不能为空");
        }
        
        if (fileSize <= 0) {
            throw new RuntimeException("文件大小无效");
        }
        
        if (fileSize > MAX_FILE_SIZE) {
            throw new RuntimeException("文件大小超过限制，最大支持50MB");
        }
        
        if (!isAllowedFileType(fileType)) {
            throw new RuntimeException("不支持的文件类型");
        }
    }
    
    /**
     * 验证图片消息
     */
    public void validateImageMessage(String fileName, long fileSize) {
        validateFileMessage(fileName, fileSize, getFileExtension(fileName));
        
        String extension = getFileExtension(fileName).toLowerCase();
        if (!isImageType(extension)) {
            throw new RuntimeException("不支持的图片格式");
        }
    }
    
    /**
     * 检查是否包含敏感词
     */
    private boolean containsSensitiveWords(String content) {
        // TODO: 实现敏感词检测逻辑
        // 这里可以集成第三方敏感词检测服务或使用本地敏感词库
        return false;
    }
    
    /**
     * 检查是否为允许的文件类型
     */
    private boolean isAllowedFileType(String fileType) {
        if (!StringUtils.hasText(fileType)) {
            return false;
        }
        
        String[] allowedTypes = {
            "jpg", "jpeg", "png", "gif", "bmp", "webp", // 图片
            "mp4", "avi", "mov", "wmv", "flv", "mkv",   // 视频
            "mp3", "wav", "aac", "flac", "ogg",         // 音频
            "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", // 文档
            "txt", "zip", "rar", "7z"                   // 其他
        };
        
        String lowerType = fileType.toLowerCase();
        for (String type : allowedTypes) {
            if (type.equals(lowerType)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 检查是否为图片类型
     */
    private boolean isImageType(String extension) {
        String[] imageTypes = {"jpg", "jpeg", "png", "gif", "bmp", "webp"};
        for (String type : imageTypes) {
            if (type.equals(extension)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String fileName) {
        if (!StringUtils.hasText(fileName)) {
            return "";
        }
        
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == fileName.length() - 1) {
            return "";
        }
        
        return fileName.substring(lastDotIndex + 1);
    }
}