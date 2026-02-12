package com.ruoyi.im.exception;

/**
 * 邮件相关异常
 * 
 * @author ruoyi
 */
public class EmailException extends BusinessException {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 邮件不存在异常
     */
    public static class EmailNotFoundException extends EmailException {
        public EmailNotFoundException(Long emailId) {
            super("邮件不存在: " + emailId);
        }
    }
    
    /**
     * 邮件权限异常
     */
    public static class EmailPermissionException extends EmailException {
        public EmailPermissionException(String message) {
            super(message);
        }
        
        public EmailPermissionException(Long userId, Long emailId) {
            super("用户 " + userId + " 没有权限访问邮件 " + emailId);
        }
    }
    
    /**
     * 邮件参数异常
     */
    public static class EmailParameterException extends EmailException {
        public EmailParameterException(String paramName, String message) {
            super("邮件参数错误 [" + paramName + "]: " + message);
        }
    }
    
    /**
     * 附件异常
     */
    public static class AttachmentException extends EmailException {
        public AttachmentException(String message) {
            super("附件错误: " + message);
        }
    }
    
    /**
     * 附件类型异常
     */
    public static class InvalidAttachmentTypeException extends AttachmentException {
        public InvalidAttachmentTypeException(String fileName, String fileType) {
            super("不支持的文件类型: " + fileType + " (文件: " + fileName + ")");
        }
    }
    
    /**
     * 附件大小异常
     */
    public static class AttachmentSizeException extends AttachmentException {
        public AttachmentSizeException(long fileSize, long maxSize) {
            super("文件大小超过限制: " + fileSize + " > " + maxSize);
        }
    }
    
    /**
     * 草稿异常
     */
    public static class DraftException extends EmailException {
        public DraftException(String message) {
            super("草稿错误: " + message);
        }
    }
    
    /**
     * 邮件发送异常
     */
    public static class EmailSendException extends EmailException {
        public EmailSendException(String message) {
            super("邮件发送失败: " + message);
        }
    }
    
    /**
     * 邮件搜索异常
     */
    public static class EmailSearchException extends EmailException {
        public EmailSearchException(String message) {
            super("邮件搜索失败: " + message);
        }
    }
    
    public EmailException(String message) {
        super(message);
    }
    
    public EmailException(String message, Throwable cause) {
        super(message, cause);
    }
}
