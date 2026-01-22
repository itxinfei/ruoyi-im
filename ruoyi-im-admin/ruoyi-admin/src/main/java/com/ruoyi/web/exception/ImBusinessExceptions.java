package com.ruoyi.web.exception;

/**
 * 消息发送异常
 * 
 * @author ruoyi
 * @version 1.0
 * @since 2025-01-22
 */
public class MessageSendException extends ImException {
    
    public MessageSendException(String message) {
        super(400, message);
    }
    
    public MessageSendException(String message, Throwable cause) {
        super(400, message, cause);
    }
    
    /**
     * 网络异常
     */
    public static class NetworkException extends MessageSendException {
        public NetworkException() {
            super("网络连接异常，请检查网络设置");
        }
    }
    
    /**
     * 消息内容异常
     */
    public static class ContentException extends MessageSendException {
        public ContentException(String message) {
            super("消息内容异常：" + message);
        }
    }
    
    /**
     * 发送频率限制异常
     */
    public static class RateLimitException extends MessageSendException {
        public RateLimitException() {
            super("发送频率过高，请稍后重试");
        }
    }
}

/**
 * 敏感词过滤异常
 * 
 * @author ruoyi
 * @version 1.0
 * @since 2025-01-22
 */
class SensitiveWordException extends ImException {
    
    private String sensitiveWord;
    
    public SensitiveWordException(String message, String sensitiveWord) {
        super(400, message);
        this.sensitiveWord = sensitiveWord;
    }
    
    public String getSensitiveWord() {
        return sensitiveWord;
    }
}

/**
 * 用户状态异常
 * 
 * @author ruoyi
 * @version 1.0
 * @since 2025-01-22
 */
class UserStatusException extends ImException {
    
    public UserStatusException(String message) {
        super(400, message);
    }
    
    /**
     * 用户已被禁用异常
     */
    public static class UserDisabledException extends UserStatusException {
        public UserDisabledException() {
            super("用户已被禁用，无法执行此操作");
        }
    }
    
    /**
     * 用户不在线异常
     */
    public static class UserNotOnlineException extends UserStatusException {
        public UserNotOnlineException() {
            super("用户当前不在线");
        }
    }
}

/**
 * 认证异常
 * 
 * @author ruoyi
 * @version 1.0
 * @since 2025-01-22
 */
class AuthenticationException extends ImException {
    
    public AuthenticationException(String message) {
        super(401, message);
    }
    
    /**
     * Token过期异常
     */
    public static class TokenExpiredException extends AuthenticationException {
        public TokenExpiredException() {
            super("登录已过期，请重新登录");
        }
    }
    
    /**
     * Token无效异常
     */
    public static class TokenInvalidException extends AuthenticationException {
        public TokenInvalidException() {
            super("无效的登录凭证");
        }
    }
}

/**
 * 授权异常
 * 
 * @author ruoyi
 * @version 1.0
 * @since 2025-01-22
 */
class AuthorizationException extends ImException {
    
    public AuthorizationException(String message) {
        super(403, message);
    }
    
    /**
     * 权限不足异常
     */
    public static class PermissionDeniedException extends AuthorizationException {
        public PermissionDeniedException() {
            super("权限不足，无法执行此操作");
        }
    }
    
    /**
     * 角色限制异常
     */
    public static class RoleLimitException extends AuthorizationException {
        public RoleLimitException(String role) {
            super("角色[" + role + "]权限不足");
        }
    }
}