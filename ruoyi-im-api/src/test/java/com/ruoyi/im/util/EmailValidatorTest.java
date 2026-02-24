package com.ruoyi.im.util;

import com.ruoyi.im.exception.EmailException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 邮件参数验证工具测试类
 *
 * @author ruoyi
 */
class EmailValidatorTest {

    // ==================== 邮件ID验证测试 ====================

    @Test
    void testValidateEmailId_Valid() {
        assertDoesNotThrow(() -> EmailValidator.validateEmailId(1L));
        assertDoesNotThrow(() -> EmailValidator.validateEmailId(100L));
    }

    @Test
    void testValidateEmailId_Null() {
        EmailException exception = assertThrows(EmailException.class, 
            () -> EmailValidator.validateEmailId(null));
        assertTrue(exception.getMessage().contains("邮件ID不能为空"));
    }

    @Test
    void testValidateEmailId_Zero() {
        EmailException exception = assertThrows(EmailException.class, 
            () -> EmailValidator.validateEmailId(0L));
        assertTrue(exception.getMessage().contains("邮件ID不能为空"));
    }

    @Test
    void testValidateEmailId_Negative() {
        EmailException exception = assertThrows(EmailException.class, 
            () -> EmailValidator.validateEmailId(-1L));
        assertTrue(exception.getMessage().contains("邮件ID不能为空"));
    }

    // ==================== 用户ID验证测试 ====================

    @Test
    void testValidateUserId_Valid() {
        assertDoesNotThrow(() -> EmailValidator.validateUserId(1L));
        assertDoesNotThrow(() -> EmailValidator.validateUserId(100L));
    }

    @Test
    void testValidateUserId_Null() {
        EmailException exception = assertThrows(EmailException.class, 
            () -> EmailValidator.validateUserId(null));
        assertTrue(exception.getMessage().contains("用户ID不能为空"));
    }

    @Test
    void testValidateUserId_Zero() {
        EmailException exception = assertThrows(EmailException.class, 
            () -> EmailValidator.validateUserId(0L));
        assertTrue(exception.getMessage().contains("用户ID不能为空"));
    }

    // ==================== 邮件主题验证测试 ====================

    @Test
    void testValidateSubject_Valid() {
        assertDoesNotThrow(() -> EmailValidator.validateSubject("测试邮件"));
        assertDoesNotThrow(() -> EmailValidator.validateSubject("Hello World"));
    }

    @Test
    void testValidateSubject_Null() {
        EmailException exception = assertThrows(EmailException.class, 
            () -> EmailValidator.validateSubject(null));
        assertTrue(exception.getMessage().contains("邮件主题不能为空"));
    }

    @Test
    void testValidateSubject_Empty() {
        EmailException exception = assertThrows(EmailException.class, 
            () -> EmailValidator.validateSubject(""));
        assertTrue(exception.getMessage().contains("邮件主题不能为空"));
    }

    @Test
    void testValidateSubject_Blank() {
        EmailException exception = assertThrows(EmailException.class, 
            () -> EmailValidator.validateSubject("   "));
        assertTrue(exception.getMessage().contains("邮件主题不能为空"));
    }

    @Test
    void testValidateSubject_TooLong() {
        String longSubject = repeatString("a", 256);
        EmailException exception = assertThrows(EmailException.class, 
            () -> EmailValidator.validateSubject(longSubject));
        assertTrue(exception.getMessage().contains("邮件主题长度不能超过"));
    }

    @Test
    void testValidateSubject_MaxLength() {
        String maxSubject = repeatString("a", 255);
        assertDoesNotThrow(() -> EmailValidator.validateSubject(maxSubject));
    }

    // ==================== 邮件内容验证测试 ====================

    @Test
    void testValidateContent_Valid() {
        assertDoesNotThrow(() -> EmailValidator.validateContent("这是一封测试邮件"));
    }

    @Test
    void testValidateContent_Null() {
        EmailException exception = assertThrows(EmailException.class, 
            () -> EmailValidator.validateContent(null));
        assertTrue(exception.getMessage().contains("邮件内容不能为空"));
    }

    @Test
    void testValidateContent_Empty() {
        EmailException exception = assertThrows(EmailException.class, 
            () -> EmailValidator.validateContent(""));
        assertTrue(exception.getMessage().contains("邮件内容不能为空"));
    }

    @Test
    void testValidateContent_TooLong() {
        String longContent = repeatString("a", 10001);
        EmailException exception = assertThrows(EmailException.class, 
            () -> EmailValidator.validateContent(longContent));
        assertTrue(exception.getMessage().contains("邮件内容长度不能超过"));
    }

    @Test
    void testValidateContent_MaxLength() {
        String maxContent = repeatString("a", 10000);
        assertDoesNotThrow(() -> EmailValidator.validateContent(maxContent));
    }

    // ==================== 收件人列表验证测试 ====================

    @Test
    void testValidateRecipients_Valid() {
        List<Long> toIds = Arrays.asList(1L, 2L, 3L);
        assertDoesNotThrow(() -> EmailValidator.validateRecipients(toIds));
    }

    @Test
    void testValidateRecipients_Null() {
        EmailException exception = assertThrows(EmailException.class, 
            () -> EmailValidator.validateRecipients(null));
        assertTrue(exception.getMessage().contains("收件人不能为空"));
    }

    @Test
    void testValidateRecipients_Empty() {
        EmailException exception = assertThrows(EmailException.class, 
            () -> EmailValidator.validateRecipients(Collections.emptyList()));
        assertTrue(exception.getMessage().contains("收件人不能为空"));
    }

    @Test
    void testValidateRecipients_TooMany() {
        // 创建101个收件人
        List<Long> toIds = Arrays.asList(new Long[101]);
        for (int i = 0; i < 101; i++) {
            toIds.set(i, (long) (i + 1));
        }
        EmailException exception = assertThrows(EmailException.class, 
            () -> EmailValidator.validateRecipients(toIds));
        assertTrue(exception.getMessage().contains("收件人数量不能超过"));
    }

    @Test
    void testValidateRecipients_MaxCount() {
        // 创建100个收件人
        List<Long> toIds = Arrays.asList(new Long[100]);
        for (int i = 0; i < 100; i++) {
            toIds.set(i, (long) (i + 1));
        }
        assertDoesNotThrow(() -> EmailValidator.validateRecipients(toIds));
    }

    @Test
    void testValidateRecipients_InvalidId() {
        List<Long> toIds = Arrays.asList(1L, null, 3L);
        EmailException exception = assertThrows(EmailException.class, 
            () -> EmailValidator.validateRecipients(toIds));
        assertTrue(exception.getMessage().contains("收件人ID无效"));
    }

    @Test
    void testValidateRecipients_ZeroId() {
        List<Long> toIds = Arrays.asList(1L, 0L, 3L);
        EmailException exception = assertThrows(EmailException.class, 
            () -> EmailValidator.validateRecipients(toIds));
        assertTrue(exception.getMessage().contains("收件人ID无效"));
    }

    // ==================== 文件类型验证测试 ====================

    @ParameterizedTest
    @ValueSource(strings = {
        "document.pdf", "report.docx", "data.xlsx", "presentation.pptx",
        "image.jpg", "photo.png", "animation.gif",
        "archive.zip", "backup.rar", "compressed.7z",
        "notes.txt", "data.csv"
    })
    void testValidateFileType_Valid(String fileName) {
        assertDoesNotThrow(() -> EmailValidator.validateFileType(fileName));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "virus.exe", "script.bat", "command.cmd", "shell.sh",
        "program.js", "macro.vbs", "powershell.ps1",
        "library.dll", "driver.sys"
    })
    void testValidateFileType_Forbidden(String fileName) {
        EmailException exception = assertThrows(EmailException.class, 
            () -> EmailValidator.validateFileType(fileName));
        assertTrue(exception.getMessage().contains("不支持的文件类型"));
    }

    @Test
    void testValidateFileType_Null() {
        EmailException exception = assertThrows(EmailException.class, 
            () -> EmailValidator.validateFileType(null));
        assertTrue(exception.getMessage().contains("文件名不能为空"));
    }

    @Test
    void testValidateFileType_Empty() {
        EmailException exception = assertThrows(EmailException.class, 
            () -> EmailValidator.validateFileType(""));
        assertTrue(exception.getMessage().contains("文件名不能为空"));
    }

    @Test
    void testValidateFileType_UnknownExtension() {
        EmailException exception = assertThrows(EmailException.class, 
            () -> EmailValidator.validateFileType("file.xyz"));
        assertTrue(exception.getMessage().contains("不支持的文件类型"));
    }

    @Test
    void testValidateFileType_NoExtension() {
        EmailException exception = assertThrows(EmailException.class, 
            () -> EmailValidator.validateFileType("filename"));
        assertTrue(exception.getMessage().contains("不支持的文件类型"));
    }

    // ==================== 文件大小验证测试 ====================

    @Test
    void testValidateFileSize_Valid() {
        assertDoesNotThrow(() -> EmailValidator.validateFileSize(1L));
        assertDoesNotThrow(() -> EmailValidator.validateFileSize(1024L * 1024L)); // 1MB
        assertDoesNotThrow(() -> EmailValidator.validateFileSize(100L * 1024L * 1024L)); // 100MB
    }

    @Test
    void testValidateFileSize_Zero() {
        EmailException exception = assertThrows(EmailException.class, 
            () -> EmailValidator.validateFileSize(0L));
        assertTrue(exception.getMessage().contains("文件大小无效"));
    }

    @Test
    void testValidateFileSize_Negative() {
        EmailException exception = assertThrows(EmailException.class, 
            () -> EmailValidator.validateFileSize(-1L));
        assertTrue(exception.getMessage().contains("文件大小无效"));
    }

    @Test
    void testValidateFileSize_TooLarge() {
        long maxSize = 100L * 1024L * 1024L; // 100MB
        EmailException exception = assertThrows(EmailException.class, 
            () -> EmailValidator.validateFileSize(maxSize + 1));
        assertTrue(exception.getMessage().contains("文件大小超过限制"));
    }

    // ==================== 文件夹名称验证测试 ====================

    @ParameterizedTest
    @ValueSource(strings = {
        "INBOX", "SENT", "DRAFT", "STARRED", "TRASH", "SPAM",
        "inbox", "sent", "draft", "starred", "trash", "spam",
        "Inbox", "Sent", "Draft"
    })
    void testValidateFolder_Valid(String folder) {
        assertDoesNotThrow(() -> EmailValidator.validateFolder(folder));
    }

    @Test
    void testValidateFolder_Null() {
        EmailException exception = assertThrows(EmailException.class, 
            () -> EmailValidator.validateFolder(null));
        assertTrue(exception.getMessage().contains("文件夹名称不能为空"));
    }

    @Test
    void testValidateFolder_Empty() {
        EmailException exception = assertThrows(EmailException.class, 
            () -> EmailValidator.validateFolder(""));
        assertTrue(exception.getMessage().contains("文件夹名称不能为空"));
    }

    @Test
    void testValidateFolder_Invalid() {
        EmailException exception = assertThrows(EmailException.class, 
            () -> EmailValidator.validateFolder("INVALID_FOLDER"));
        assertTrue(exception.getMessage().contains("无效的文件夹名称"));
    }

    @Test
    void testValidateFolder_CustomFolder() {
        EmailException exception = assertThrows(EmailException.class, 
            () -> EmailValidator.validateFolder("MyFolder"));
        assertTrue(exception.getMessage().contains("无效的文件夹名称"));
    }

    // ==================== 邮件地址验证测试 ====================

    @ParameterizedTest
    @CsvSource({
        "user@example.com",
        "test.user@domain.org",
        "user+tag@example.co.uk",
        "firstname.lastname@company.com",
        "123@numeric.com"
    })
    void testValidateEmailAddress_Valid(String email) {
        assertDoesNotThrow(() -> EmailValidator.validateEmailAddress(email));
    }

    @Test
    void testValidateEmailAddress_Null() {
        EmailException exception = assertThrows(EmailException.class, 
            () -> EmailValidator.validateEmailAddress(null));
        assertTrue(exception.getMessage().contains("邮件地址不能为空"));
    }

    @Test
    void testValidateEmailAddress_Empty() {
        EmailException exception = assertThrows(EmailException.class, 
            () -> EmailValidator.validateEmailAddress(""));
        assertTrue(exception.getMessage().contains("邮件地址不能为空"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "invalid",
        "@example.com",
        "user@",
        "user@.com",
        "user name@example.com",
        "user@example",
        "user..name@example.com"
    })
    void testValidateEmailAddress_Invalid(String email) {
        EmailException exception = assertThrows(EmailException.class, 
            () -> EmailValidator.validateEmailAddress(email));
        assertTrue(exception.getMessage().contains("邮件地址格式无效"));
    }

    // ==================== 工具方法测试 ====================

    @Test
    void testGetAllowedFileTypes() {
        String[] allowedTypes = EmailValidator.getAllowedFileTypes();
        assertNotNull(allowedTypes);
        assertTrue(allowedTypes.length > 0);
        
        // 验证包含常见的允许类型
        List<String> typesList = Arrays.asList(allowedTypes);
        assertTrue(typesList.contains("pdf"));
        assertTrue(typesList.contains("docx"));
        assertTrue(typesList.contains("jpg"));
        assertTrue(typesList.contains("zip"));
    }

    @Test
    void testGetMaxAttachmentSize() {
        long maxSize = EmailValidator.getMaxAttachmentSize();
        assertEquals(100L * 1024L * 1024L, maxSize); // 100MB
    }

    // ==================== 辅助方法 ====================

    /**
     * Java 8 兼容的字符串重复方法
     * 替代 Java 11+ 的 String.repeat()
     */
    private String repeatString(String str, int count) {
        if (count <= 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder(str.length() * count);
        for (int i = 0; i < count; i++) {
            sb.append(str);
        }
        return sb.toString();
    }
}
