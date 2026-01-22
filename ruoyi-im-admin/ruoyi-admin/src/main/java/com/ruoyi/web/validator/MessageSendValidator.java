package com.ruoyi.web.validator;

import com.ruoyi.web.domain.dto.MessageSendDTO;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

/**
 * 消息发送参数验证器
 * 
 * @author ruoyi
 * @version 1.0
 * @since 2025-01-22
 */
@Component
public class MessageSendValidator {

    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final int MAX_CONTENT_LENGTH = 10000;
    private static final int MIN_CONTENT_LENGTH = 1;
    
    /**
     * 验证消息发送参数
     */
    public ValidationResult validate(MessageSendDTO dto) {
        if (dto == null) {
            return ValidationResult.error("消息参数不能为空");
        }
        
        return validateConversationId(dto.getConversationId())
                .andThen(() -> validateMessageType(dto.getMessageType()))
                .andThen(() -> validateContent(dto.getContent()))
                .andThen(() -> validateExtraData(dto.getExtraData()))
                .andThen(() -> validateReplyTo(dto.getReplyToMessageId()))
                .getResult();
    }
    
    private ValidationResult validateConversationId(Long conversationId) {
        if (conversationId == null || conversationId <= 0) {
            return ValidationResult.error("会话ID不能为空且必须大于0");
        }
        return ValidationResult.success();
    }
    
    private ValidationResult validateMessageType(String messageType) {
        if (!StringUtils.hasText(messageType)) {
            return ValidationResult.error("消息类型不能为空");
        }
        
        String[] allowedTypes = {"TEXT", "IMAGE", "FILE", "VOICE", "VIDEO", "SYSTEM"};
        boolean isValidType = false;
        for (String allowedType : allowedTypes) {
            if (allowedType.equals(messageType)) {
                isValidType = true;
                break;
            }
        }
        
        if (!isValidType) {
            return ValidationResult.error("消息类型必须是TEXT、IMAGE、FILE、VOICE、VIDEO、SYSTEM之一");
        }
        
        return ValidationResult.success();
    }
    
    private ValidationResult validateContent(String content) {
        if (!StringUtils.hasText(content)) {
            return ValidationResult.error("消息内容不能为空");
        }
        
        if (content.length() < MIN_CONTENT_LENGTH) {
            return ValidationResult.error("消息内容长度不能少于" + MIN_CONTENT_LENGTH + "个字符");
        }
        
        if (content.length() > MAX_CONTENT_LENGTH) {
            return ValidationResult.error("消息内容长度不能超过" + MAX_CONTENT_LENGTH + "个字符");
        }
        
        return ValidationResult.success();
    }
    
    private ValidationResult validateExtraData(String extraData) {
        if (StringUtils.hasText(extraData)) {
            try {
                // 验证是否为有效JSON
                if (extraData.startsWith("{") && extraData.endsWith("}")) {
                    // 简单的JSON格式验证
                    return ValidationResult.success();
                }
                
                // 验证是否为有效的URL
                if (extraData.startsWith("http://") || extraData.startsWith("https://")) {
                    return ValidationResult.success();
                }
                
                return ValidationResult.error("额外数据格式不正确，应为JSON格式或URL");
            } catch (Exception e) {
                return ValidationResult.error("额外数据解析失败");
            }
        }
        
        return ValidationResult.success();
    }
    
    private ValidationResult validateReplyTo(Long replyToMessageId) {
        if (replyToMessageId != null && replyToMessageId <= 0) {
            return ValidationResult.error("回复消息ID必须大于0");
        }
        return ValidationResult.success();
    }
    
    /**
     * 验证结果类
     */
    public static class ValidationResult {
        private boolean valid;
        private String message;
        
        private ValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }
        
        public static ValidationResult success() {
            return new ValidationResult(true, null);
        }
        
        public static ValidationResult error(String message) {
            return new ValidationResult(false, message);
        }
        
        public boolean isValid() {
            return valid;
        }
        
        public String getMessage() {
            return message;
        }
        
        public ValidationResult andThen(java.util.function.Supplier<ValidationResult> other) {
            if (!valid) {
                return this;
            }
            return other.get();
        }
    }
}