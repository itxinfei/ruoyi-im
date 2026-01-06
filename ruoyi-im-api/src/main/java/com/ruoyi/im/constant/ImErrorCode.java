package com.ruoyi.im.constant;

/**
 * IM系统错误码枚举
 * 
 * <p>该枚举类定义了IM系统中所有可能的错误码，并提供了获取错误码和错误信息的方法。</p>
 * 
 * @author ruoyi
 * @date 2024-01-01
 */
public enum ImErrorCode {

    /** 未认证或令牌失效 */
    UNAUTHORIZED("IM-401-01", "未认证或令牌失效", 401),
    
    /** 无权限访问 */
    FORBIDDEN("IM-403-01", "无权限访问", 403),
    
    /** 非法文件类型或大小超限 */
    INVALID_FILE("IM-400-01", "非法文件类型或大小超限", 400),
    
    /** 敏感词拦截 */
    SENSITIVE_BLOCK("IM-409-01", "敏感词拦截", 409),
    
    /** 资源不存在 */
    NOT_FOUND("IM-404-01", "资源不存在", 404),
    
    /** 消息撤回超时 */
    RECALL_TIMEOUT("IM-400-02", "消息撤回超时", 400),
    
    /** 会话不存在 */
    CONVERSATION_NOT_FOUND("IM-404-02", "会话不存在", 404),
    
    /** 用户不在线 */
    USER_OFFLINE("IM-400-03", "用户不在线", 400),
    
    /** 导出申请待审核 */
    EXPORT_PENDING("IM-400-04", "导出申请待审核", 400),
    
    /** 导出链接已过期 */
    EXPORT_EXPIRED("IM-400-05", "导出链接已过期", 400),
    
    /** 文件下载次数超限 */
    DOWNLOAD_LIMIT("IM-400-06", "文件下载次数超限", 400),
    
    /** 群组已满员 */
    GROUP_FULL("IM-400-07", "群组已满员", 400),
    
    /** 用户已被禁言 */
    USER_MUTED("IM-400-08", "用户已被禁言", 400),
    
    /** 消息内容过长 */
    MESSAGE_TOO_LONG("IM-400-09", "消息内容过长", 400),
    
    /** 系统内部错误 */
    INTERNAL_ERROR("IM-500-01", "系统内部错误", 500),
    
    /** WebSocket连接失败 */
    WEBSOCKET_ERROR("IM-500-02", "WebSocket连接失败", 500),
    
    /** 数据库操作失败 */
    DATABASE_ERROR("IM-500-03", "数据库操作失败", 500),
    
    /** 文件上传失败 */
    UPLOAD_ERROR("IM-500-04", "文件上传失败", 500),
    
    /** 敏感词检测失败 */
    SENSITIVE_DETECT_ERROR("IM-500-05", "敏感词检测失败", 500);
    
    /** 错误码 */
    private final String code;
    
    /** 错误消息 */
    private final String message;
    
    /** HTTP状态码 */
    private final int httpStatus;
    
    /**
     * 构造函数
     * 
     * @param code 错误码
     * @param message 错误消息
     * @param httpStatus HTTP状态码
     */
    ImErrorCode(String code, String message, int httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
    
    /**
     * 获取错误码
     * 
     * @return 错误码
     */
    public String getCode() {
        return code;
    }
    
    /**
     * 获取错误消息
     * 
     * @return 错误消息
     */
    public String getMessage() {
        return message;
    }
    
    /**
     * 获取HTTP状态码
     * 
     * @return HTTP状态码
     */
    public int getHttpStatus() {
        return httpStatus;
    }
    
    /**
     * 根据错误码字符串获取枚举值
     * 
     * @param code 错误码字符串
     * @return 对应的枚举值，如果未找到则返回null
     */
    public static ImErrorCode fromCode(String code) {
        for (ImErrorCode errorCode : ImErrorCode.values()) {
            if (errorCode.getCode().equals(code)) {
                return errorCode;
            }
        }
        return null;
    }
    
    /**
     * 根据HTTP状态码获取第一个匹配的错误码
     * 
     * @param httpStatus HTTP状态码
     * @return 对应的枚举值，如果未找到则返回null
     */
    public static ImErrorCode fromHttpStatus(int httpStatus) {
        for (ImErrorCode errorCode : ImErrorCode.values()) {
            if (errorCode.getHttpStatus() == httpStatus) {
                return errorCode;
            }
        }
        return null;
    }
    
    /**
     * 获取所有错误码列表
     * 
     * @return 错误码列表
     */
    public static String[] getAllCodes() {
        ImErrorCode[] values = ImErrorCode.values();
        String[] codes = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            codes[i] = values[i].getCode();
        }
        return codes;
    }
}