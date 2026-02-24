package com.ruoyi.im.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 邮件异常类测试
 *
 * @author ruoyi
 */
class EmailExceptionTest {

    @Test
    void testEmailException_MessageOnly() {
        String message = "测试邮件异常";
        EmailException exception = new EmailException(message);
        
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void testEmailException_MessageAndCause() {
        String message = "测试邮件异常";
        Throwable cause = new RuntimeException("原始异常");
        EmailException exception = new EmailException(message, cause);
        
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    // ==================== EmailNotFoundException 测试 ====================

    @Test
    void testEmailNotFoundException() {
        Long emailId = 12345L;
        EmailException.EmailNotFoundException exception = 
            new EmailException.EmailNotFoundException(emailId);
        
        assertTrue(exception.getMessage().contains("邮件不存在"));
        assertTrue(exception.getMessage().contains(emailId.toString()));
    }

    @Test
    void testEmailNotFoundException_LargeId() {
        Long emailId = 999999999L;
        EmailException.EmailNotFoundException exception = 
            new EmailException.EmailNotFoundException(emailId);
        
        assertTrue(exception.getMessage().contains(emailId.toString()));
    }

    // ==================== EmailPermissionException 测试 ====================

    @Test
    void testEmailPermissionException_MessageOnly() {
        String message = "自定义权限错误消息";
        EmailException.EmailPermissionException exception = 
            new EmailException.EmailPermissionException(message);
        
        assertEquals(message, exception.getMessage());
    }

    @Test
    void testEmailPermissionException_WithUserAndEmail() {
        Long userId = 100L;
        Long emailId = 200L;
        EmailException.EmailPermissionException exception = 
            new EmailException.EmailPermissionException(userId, emailId);
        
        assertTrue(exception.getMessage().contains("没有权限访问邮件"));
        assertTrue(exception.getMessage().contains(userId.toString()));
        assertTrue(exception.getMessage().contains(emailId.toString()));
    }

    // ==================== EmailParameterException 测试 ====================

    @Test
    void testEmailParameterException() {
        String paramName = "subject";
        String message = "邮件主题不能为空";
        EmailException.EmailParameterException exception = 
            new EmailException.EmailParameterException(paramName, message);
        
        assertTrue(exception.getMessage().contains("邮件参数错误"));
        assertTrue(exception.getMessage().contains(paramName));
        assertTrue(exception.getMessage().contains(message));
    }

    @Test
    void testEmailParameterException_DifferentParams() {
        // 测试不同的参数名
        EmailException.EmailParameterException exception1 = 
            new EmailException.EmailParameterException("emailId", "无效的邮件ID");
        assertTrue(exception1.getMessage().contains("emailId"));
        
        EmailException.EmailParameterException exception2 = 
            new EmailException.EmailParameterException("content", "内容过长");
        assertTrue(exception2.getMessage().contains("content"));
    }

    // ==================== AttachmentException 测试 ====================

    @Test
    void testAttachmentException() {
        String message = "附件上传失败";
        EmailException.AttachmentException exception = 
            new EmailException.AttachmentException(message);
        
        assertTrue(exception.getMessage().contains("附件错误"));
        assertTrue(exception.getMessage().contains(message));
    }

    // ==================== InvalidAttachmentTypeException 测试 ====================

    @Test
    void testInvalidAttachmentTypeException() {
        String fileName = "virus.exe";
        String fileType = "exe";
        EmailException.InvalidAttachmentTypeException exception = 
            new EmailException.InvalidAttachmentTypeException(fileName, fileType);
        
        assertTrue(exception.getMessage().contains("不支持的文件类型"));
        assertTrue(exception.getMessage().contains(fileType));
        assertTrue(exception.getMessage().contains(fileName));
    }

    @Test
    void testInvalidAttachmentTypeException_DifferentTypes() {
        // 测试不同的文件类型
        EmailException.InvalidAttachmentTypeException exception1 = 
            new EmailException.InvalidAttachmentTypeException("script.bat", "bat");
        assertTrue(exception1.getMessage().contains("bat"));
        
        EmailException.InvalidAttachmentTypeException exception2 = 
            new EmailException.InvalidAttachmentTypeException("unknown.xyz", "xyz");
        assertTrue(exception2.getMessage().contains("xyz"));
    }

    // ==================== AttachmentSizeException 测试 ====================

    @Test
    void testAttachmentSizeException() {
        long fileSize = 150 * 1024 * 1024; // 150MB
        long maxSize = 100 * 1024 * 1024;  // 100MB
        EmailException.AttachmentSizeException exception = 
            new EmailException.AttachmentSizeException(fileSize, maxSize);
        
        assertTrue(exception.getMessage().contains("文件大小超过限制"));
        assertTrue(exception.getMessage().contains(String.valueOf(fileSize)));
        assertTrue(exception.getMessage().contains(String.valueOf(maxSize)));
    }

    @Test
    void testAttachmentSizeException_SmallSizes() {
        long fileSize = 1024; // 1KB
        long maxSize = 512;   // 512B
        EmailException.AttachmentSizeException exception = 
            new EmailException.AttachmentSizeException(fileSize, maxSize);
        
        assertTrue(exception.getMessage().contains("1024"));
        assertTrue(exception.getMessage().contains("512"));
    }

    // ==================== DraftException 测试 ====================

    @Test
    void testDraftException() {
        String message = "草稿保存失败";
        EmailException.DraftException exception = 
            new EmailException.DraftException(message);
        
        assertTrue(exception.getMessage().contains("草稿错误"));
        assertTrue(exception.getMessage().contains(message));
    }

    @Test
    void testDraftException_DifferentMessages() {
        EmailException.DraftException exception1 = 
            new EmailException.DraftException("草稿已过期");
        assertTrue(exception1.getMessage().contains("草稿已过期"));
        
        EmailException.DraftException exception2 = 
            new EmailException.DraftException("草稿加载失败");
        assertTrue(exception2.getMessage().contains("草稿加载失败"));
    }

    // ==================== EmailSendException 测试 ====================

    @Test
    void testEmailSendException() {
        String message = "SMTP服务器连接失败";
        EmailException.EmailSendException exception = 
            new EmailException.EmailSendException(message);
        
        assertTrue(exception.getMessage().contains("邮件发送失败"));
        assertTrue(exception.getMessage().contains(message));
    }

    @Test
    void testEmailSendException_DifferentReasons() {
        EmailException.EmailSendException exception1 = 
            new EmailException.EmailSendException("收件人地址无效");
        assertTrue(exception1.getMessage().contains("收件人地址无效"));
        
        EmailException.EmailSendException exception2 = 
            new EmailException.EmailSendException("邮件内容过大");
        assertTrue(exception2.getMessage().contains("邮件内容过大"));
    }

    // ==================== EmailSearchException 测试 ====================

    @Test
    void testEmailSearchException() {
        String message = "搜索索引不可用";
        EmailException.EmailSearchException exception = 
            new EmailException.EmailSearchException(message);
        
        assertTrue(exception.getMessage().contains("邮件搜索失败"));
        assertTrue(exception.getMessage().contains(message));
    }

    @Test
    void testEmailSearchException_DifferentReasons() {
        EmailException.EmailSearchException exception1 = 
            new EmailException.EmailSearchException("搜索关键词过长");
        assertTrue(exception1.getMessage().contains("搜索关键词过长"));
        
        EmailException.EmailSearchException exception2 = 
            new EmailException.EmailSearchException("数据库连接失败");
        assertTrue(exception2.getMessage().contains("数据库连接失败"));
    }

    // ==================== 继承关系测试 ====================

    @Test
    void testInheritance() {
        // 验证所有子类都是EmailException的子类
        assertTrue(new EmailException.EmailNotFoundException(1L) instanceof EmailException);
        assertTrue(new EmailException.EmailPermissionException("test") instanceof EmailException);
        assertTrue(new EmailException.EmailParameterException("p", "m") instanceof EmailException);
        assertTrue(new EmailException.AttachmentException("test") instanceof EmailException);
        assertTrue(new EmailException.InvalidAttachmentTypeException("f", "t") instanceof EmailException.AttachmentException);
        assertTrue(new EmailException.AttachmentSizeException(1, 2) instanceof EmailException.AttachmentException);
        assertTrue(new EmailException.DraftException("test") instanceof EmailException);
        assertTrue(new EmailException.EmailSendException("test") instanceof EmailException);
        assertTrue(new EmailException.EmailSearchException("test") instanceof EmailException);
        
        // 验证EmailException是BusinessException的子类
        assertTrue(new EmailException("test") instanceof BusinessException);
    }

    // ==================== 序列化测试 ====================

    @Test
    void testSerialVersionUID() {
        // 验证serialVersionUID存在且为1L
        assertDoesNotThrow(() -> {
            java.lang.reflect.Field field = EmailException.class.getDeclaredField("serialVersionUID");
            field.setAccessible(true);
            long uid = (long) field.get(null);
            assertEquals(1L, uid);
        });
    }
}
