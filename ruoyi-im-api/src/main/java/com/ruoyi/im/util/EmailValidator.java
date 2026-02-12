package com.ruoyi.im.util;

import com.ruoyi.im.exception.EmailException;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.regex.Pattern;

/**
 * 邮件参数验证工具
 * 
 * @author ruoyi
 */
public class EmailValidator {
    
    // 邮件主题最大长度
    private static final int MAX_SUBJECT_LENGTH = 255;
    
    // 邮件内容最大长度
    private static final int MAX_CONTENT_LENGTH = 10000;
    
    // 收件人最大数量
    private static final int MAX_RECIPIENTS = 100;
    
    // 附件最大大小（100MB）
    private static final long MAX_ATTACHMENT_SIZE = 100 * 1024 * 1024;
    
    // 允许的文件类型
    private static final String[] ALLOWED_FILE_TYPES = {
        "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx",
        "jpg", "jpeg", "png", "gif", "bmp",
        "zip", "rar", "7z",
        "txt", "csv"
    };
    
    // 禁止的文件类型
    private static final String[] FORBIDDEN_FILE_TYPES = {
        "exe", "bat", "cmd", "sh", "com",
        "js", "vbs", "ps1",
        "dll", "sys", "drv"
    };
    
    // 邮件地址正则表达式
    private static final Pattern EMAIL_PATTERN = 
        Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    
    /**
     * 验证邮件ID
     * 
     * @param emailId 邮件ID
     * @throws EmailException 如果邮件ID无效
     */
    public static void validateEmailId(Long emailId) {
        if (emailId == null || emailId <= 0) {
            throw new EmailException.EmailParameterException("emailId", "邮件ID不能为空或小于等于0");
        }
    }
    
    /**
     * 验证用户ID
     * 
     * @param userId 用户ID
     * @throws EmailException 如果用户ID无效
     */
    public static void validateUserId(Long userId) {
        if (userId == null || userId <= 0) {
            throw new EmailException.EmailParameterException("userId", "用户ID不能为空或小于等于0");
        }
    }
    
    /**
     * 验证邮件主题
     * 
     * @param subject 邮件主题
     * @throws EmailException 如果主题无效
     */
    public static void validateSubject(String subject) {
        if (StringUtils.isBlank(subject)) {
            throw new EmailException.EmailParameterException("subject", "邮件主题不能为空");
        }
        if (subject.length() > MAX_SUBJECT_LENGTH) {
            throw new EmailException.EmailParameterException("subject", 
                "邮件主题长度不能超过 " + MAX_SUBJECT_LENGTH + " 个字符");
        }
    }
    
    /**
     * 验证邮件内容
     * 
     * @param content 邮件内容
     * @throws EmailException 如果内容无效
     */
    public static void validateContent(String content) {
        if (StringUtils.isBlank(content)) {
            throw new EmailException.EmailParameterException("content", "邮件内容不能为空");
        }
        if (content.length() > MAX_CONTENT_LENGTH) {
            throw new EmailException.EmailParameterException("content", 
                "邮件内容长度不能超过 " + MAX_CONTENT_LENGTH + " 个字符");
        }
    }
    
    /**
     * 验证收件人ID列表
     * 
     * @param toIds 收件人ID列表
     * @throws EmailException 如果收件人列表无效
     */
    public static void validateRecipients(List<Long> toIds) {
        if (toIds == null || toIds.isEmpty()) {
            throw new EmailException.EmailParameterException("toIds", "收件人不能为空");
        }
        if (toIds.size() > MAX_RECIPIENTS) {
            throw new EmailException.EmailParameterException("toIds", 
                "收件人数量不能超过 " + MAX_RECIPIENTS);
        }
        for (Long id : toIds) {
            if (id == null || id <= 0) {
                throw new EmailException.EmailParameterException("toIds", "收件人ID无效");
            }
        }
    }
    
    /**
     * 验证文件类型
     * 
     * @param fileName 文件名
     * @throws EmailException 如果文件类型不允许
     */
    public static void validateFileType(String fileName) {
        if (StringUtils.isBlank(fileName)) {
            throw new EmailException.EmailParameterException("fileName", "文件名不能为空");
        }
        
        String fileExtension = getFileExtension(fileName).toLowerCase();
        
        // 检查禁止的文件类型
        for (String forbiddenType : FORBIDDEN_FILE_TYPES) {
            if (forbiddenType.equals(fileExtension)) {
                throw new EmailException.InvalidAttachmentTypeException(fileName, fileExtension);
            }
        }
        
        // 检查允许的文件类型
        boolean isAllowed = false;
        for (String allowedType : ALLOWED_FILE_TYPES) {
            if (allowedType.equals(fileExtension)) {
                isAllowed = true;
                break;
            }
        }
        
        if (!isAllowed) {
            throw new EmailException.InvalidAttachmentTypeException(fileName, fileExtension);
        }
    }
    
    /**
     * 验证文件大小
     * 
     * @param fileSize 文件大小（字节）
     * @throws EmailException 如果文件大小超过限制
     */
    public static void validateFileSize(long fileSize) {
        if (fileSize <= 0) {
            throw new EmailException.AttachmentException("文件大小无效");
        }
        if (fileSize > MAX_ATTACHMENT_SIZE) {
            throw new EmailException.AttachmentSizeException(fileSize, MAX_ATTACHMENT_SIZE);
        }
    }
    
    /**
     * 验证文件夹名称
     * 
     * @param folder 文件夹名称
     * @throws EmailException 如果文件夹名称无效
     */
    public static void validateFolder(String folder) {
        if (StringUtils.isBlank(folder)) {
            throw new EmailException.EmailParameterException("folder", "文件夹名称不能为空");
        }
        
        String folderUpper = folder.toUpperCase();
        if (!folderUpper.matches("^(INBOX|SENT|DRAFT|STARRED|TRASH|SPAM)$")) {
            throw new EmailException.EmailParameterException("folder", 
                "无效的文件夹名称: " + folder);
        }
    }
    
    /**
     * 验证邮件地址
     * 
     * @param email 邮件地址
     * @throws EmailException 如果邮件地址无效
     */
    public static void validateEmailAddress(String email) {
        if (StringUtils.isBlank(email)) {
            throw new EmailException.EmailParameterException("email", "邮件地址不能为空");
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new EmailException.EmailParameterException("email", 
                "邮件地址格式无效: " + email);
        }
    }
    
    /**
     * 获取文件扩展名
     * 
     * @param fileName 文件名
     * @return 文件扩展名
     */
    private static String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
            return fileName.substring(lastDotIndex + 1);
        }
        return "";
    }
    
    /**
     * 获取允许的文件类型列表
     * 
     * @return 允许的文件类型数组
     */
    public static String[] getAllowedFileTypes() {
        return ALLOWED_FILE_TYPES;
    }
    
    /**
     * 获取最大附件大小
     * 
     * @return 最大附件大小（字节）
     */
    public static long getMaxAttachmentSize() {
        return MAX_ATTACHMENT_SIZE;
    }
}
