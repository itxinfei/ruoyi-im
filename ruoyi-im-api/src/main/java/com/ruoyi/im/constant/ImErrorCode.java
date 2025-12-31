package com.ruoyi.im.constant;

/**
 * IM系统错误码常量
 * 
 * @author ruoyi
 * @date 2024-01-01
 */
public class ImErrorCode {

    /** 错误码前缀 */
    public static final String PREFIX = "IM-";

    /** 未认证或令牌失效 */
    public static final String UNAUTHORIZED = PREFIX + "401-01";
    
    /** 无权限访问 */
    public static final String FORBIDDEN = PREFIX + "403-01";
    
    /** 非法文件类型或大小超限 */
    public static final String INVALID_FILE = PREFIX + "400-01";
    
    /** 敏感词拦截 */
    public static final String SENSITIVE_BLOCK = PREFIX + "409-01";
    
    /** 资源不存在 */
    public static final String NOT_FOUND = PREFIX + "404-01";
    
    /** 消息撤回超时 */
    public static final String RECALL_TIMEOUT = PREFIX + "400-02";
    
    /** 会话不存在 */
    public static final String CONVERSATION_NOT_FOUND = PREFIX + "404-02";
    
    /** 用户不在线 */
    public static final String USER_OFFLINE = PREFIX + "400-03";
    
    /** 导出申请待审批 */
    public static final String EXPORT_PENDING = PREFIX + "400-04";
    
    /** 导出链接已过期 */
    public static final String EXPORT_EXPIRED = PREFIX + "400-05";
    
    /** 文件下载次数超限 */
    public static final String DOWNLOAD_LIMIT = PREFIX + "400-06";
    
    /** 群组已满员 */
    public static final String GROUP_FULL = PREFIX + "400-07";
    
    /** 用户已被禁言 */
    public static final String USER_MUTED = PREFIX + "400-08";
    
    /** 消息内容过长 */
    public static final String MESSAGE_TOO_LONG = PREFIX + "400-09";
    
    /** 系统内部错误 */
    public static final String INTERNAL_ERROR = PREFIX + "500-01";
    
    /** WebSocket连接失败 */
    public static final String WEBSOCKET_ERROR = PREFIX + "500-02";
    
    /** 数据库操作失败 */
    public static final String DATABASE_ERROR = PREFIX + "500-03";
    
    /** 文件上传失败 */
    public static final String UPLOAD_ERROR = PREFIX + "500-04";
    
    /** 敏感词检测失败 */
    public static final String SENSITIVE_DETECT_ERROR = PREFIX + "500-05";
} 