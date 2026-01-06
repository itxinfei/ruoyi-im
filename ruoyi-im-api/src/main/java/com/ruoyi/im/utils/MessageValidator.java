package com.ruoyi.im.utils;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 娑堟伅楠岃瘉鍣?
 * 
 * @author ruoyi
 */
@Component
public class MessageValidator {
    
    private static final int MAX_TEXT_LENGTH = 5000;
    private static final int MAX_FILE_SIZE = 50 * 1024 * 1024; // 50MB
    
    /**
     * 楠岃瘉鏂囨湰娑堟伅
     */
    public void validateTextMessage(String content) {
        if (!StringUtils.hasText(content)) {
            throw new RuntimeException("娑堟伅鍐呭涓嶈兘涓虹┖");
        }
        
        if (content.length() > MAX_TEXT_LENGTH) {
            throw new RuntimeException("娑堟伅鍐呭杩囬暱锛屾渶澶ф敮鎸? + MAX_TEXT_LENGTH + "涓瓧绗?);
        }
        
        // 妫€鏌ユ槸鍚﹀寘鍚晱鎰熻瘝
        if (containsSensitiveWords(content)) {
            throw new RuntimeException("娑堟伅鍖呭惈鏁忔劅璇嶆眹");
        }
    }
    
    /**
     * 楠岃瘉鏂囦欢娑堟伅
     */
    public void validateFileMessage(String fileName, long fileSize, String fileType) {
        if (!StringUtils.hasText(fileName)) {
            throw new RuntimeException("鏂囦欢鍚嶄笉鑳戒负绌?);
        }
        
        if (fileSize <= 0) {
            throw new RuntimeException("鏂囦欢澶у皬鏃犳晥");
        }
        
        if (fileSize > MAX_FILE_SIZE) {
            throw new RuntimeException("鏂囦欢澶у皬瓒呰繃闄愬埗锛屾渶澶ф敮鎸?0MB");
        }
        
        if (!isAllowedFileType(fileType)) {
            throw new RuntimeException("涓嶆敮鎸佺殑鏂囦欢绫诲瀷");
        }
    }
    
    /**
     * 楠岃瘉鍥剧墖娑堟伅
     */
    public void validateImageMessage(String fileName, long fileSize) {
        validateFileMessage(fileName, fileSize, getFileExtension(fileName));
        
        String extension = getFileExtension(fileName).toLowerCase();
        if (!isImageType(extension)) {
            throw new RuntimeException("涓嶆敮鎸佺殑鍥剧墖鏍煎紡");
        }
    }
    
    /**
     * 妫€鏌ユ槸鍚﹀寘鍚晱鎰熻瘝
     */
    private boolean containsSensitiveWords(String content) {
        // TODO: 瀹炵幇鏁忔劅璇嶆娴嬮€昏緫
        // 杩欓噷鍙互闆嗘垚绗笁鏂规晱鎰熻瘝妫€娴嬫湇鍔℃垨浣跨敤鏈湴鏁忔劅璇嶅簱
        return false;
    }
    
    /**
     * 妫€鏌ユ槸鍚︿负鍏佽鐨勬枃浠剁被鍨?
     */
    private boolean isAllowedFileType(String fileType) {
        if (!StringUtils.hasText(fileType)) {
            return false;
        }
        
        String[] allowedTypes = {
            "jpg", "jpeg", "png", "gif", "bmp", "webp", // 鍥剧墖
            "mp4", "avi", "mov", "wmv", "flv", "mkv",   // 瑙嗛
            "mp3", "wav", "aac", "flac", "ogg",         // 闊抽
            "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", // 鏂囨。
            "txt", "zip", "rar", "7z"                   // 鍏朵粬
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
     * 妫€鏌ユ槸鍚︿负鍥剧墖绫诲瀷
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
     * 鑾峰彇鏂囦欢鎵╁睍鍚?
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
