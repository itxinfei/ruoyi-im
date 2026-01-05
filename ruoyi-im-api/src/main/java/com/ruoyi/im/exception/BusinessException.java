package com.ruoyi.im.exception;

import com.ruoyi.im.constant.ImErrorCode;

/**
 * 业务异常类
 * 
 * 用于处理业务逻辑错误
 * 
 * @author ruoyi
 */
public class BusinessException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    private int code;
    
    private String errorCode;
    
    /**
     * 构造一个带错误码的异常
     * 
     * @param errorCode 错误码枚举
     */
    public BusinessException(ImErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getHttpStatus();
        this.errorCode = errorCode.getCode();
    }
    
    /**
     * 构造一个带错误码和额外消息的异常
     * 
     * @param errorCode 错误码枚举
     * @param customMessage 自定义错误消息
     */
    public BusinessException(ImErrorCode errorCode, String customMessage) {
        super(customMessage);
        this.code = errorCode.getHttpStatus();
        this.errorCode = errorCode.getCode();
    }
    
    /**
     * 构造一个带HTTP状态码和错误码的异常
     */
    public BusinessException(int code, String errorCode, String message) {
        super(message);
        this.code = code;
        this.errorCode = errorCode;
    }
    
    /**
     * 构造一个只带错误码字符串的异常
     */
    public BusinessException(String errorCode, String message) {
        super(message);
        this.code = 500;
        this.errorCode = errorCode;
    }
    
    public BusinessException(String message) {
        super(message);
        this.code = 500;
    }
    
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }
    
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.code = 500;
    }
    
    public BusinessException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }
    
    public int getCode() {
        return code;
    }
    
    public void setCode(int code) {
        this.code = code;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }
}
