package com.ruoyi.web.exception;

/**
 * 自定义IM业务异常基类
 * 
 * @author ruoyi
 * @version 1.0
 * @since 2025-01-22
 */
public class ImException extends RuntimeException {
    
    private Integer code;
    private String message;
    
    public ImException(String message) {
        super(message);
        this.message = message;
        this.code = 500;
    }
    
    public ImException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }
    
    public ImException(String message, Throwable cause) {
        super(message, cause);
        this.message = message;
        this.code = 500;
    }
    
    public ImException(Integer code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.message = message;
    }
    
    public Integer getCode() {
        return code;
    }
    
    @Override
    public String getMessage() {
        return message;
    }
}