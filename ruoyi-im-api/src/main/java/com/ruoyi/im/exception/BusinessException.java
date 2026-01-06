package com.ruoyi.im.exception;

import com.ruoyi.im.constant.ImErrorCode;

/**
 * 涓氬姟寮傚父绫? * 
 * 鐢ㄤ簬澶勭悊涓氬姟閫昏緫閿欒
 * 
 * @author ruoyi
 */
public class BusinessException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    private int code;
    
    private String errorCode;
    
    /**
     * 鏋勯€犱竴涓甫閿欒鐮佺殑寮傚父
     * 
     * @param errorCode 閿欒鐮佹灇涓?     */
    public BusinessException(ImErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getHttpStatus();
        this.errorCode = errorCode.getCode();
    }
    
    /**
     * 鏋勯€犱竴涓甫閿欒鐮佸拰棰濆娑堟伅鐨勫紓甯?     * 
     * @param errorCode 閿欒鐮佹灇涓?     * @param customMessage 鑷畾涔夐敊璇秷鎭?     */
    public BusinessException(ImErrorCode errorCode, String customMessage) {
        super(customMessage);
        this.code = errorCode.getHttpStatus();
        this.errorCode = errorCode.getCode();
    }
    
    /**
     * 鏋勯€犱竴涓甫HTTP鐘舵€佺爜鍜岄敊璇爜鐨勫紓甯?     */
    public BusinessException(int code, String errorCode, String message) {
        super(message);
        this.code = code;
        this.errorCode = errorCode;
    }
    
    /**
     * 鏋勯€犱竴涓彧甯﹂敊璇爜瀛楃涓茬殑寮傚父
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
