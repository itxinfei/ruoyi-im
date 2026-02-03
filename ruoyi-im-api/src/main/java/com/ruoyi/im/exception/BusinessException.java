package com.ruoyi.im.exception;

import com.ruoyi.im.constant.ErrorCode;
import lombok.Data;

/**
 * 业务异常类
 *
 * 用于处理业务逻辑错误，支持使用ErrorCode枚举和传统int错误码
 *
 * @author ruoyi
 */
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 错误码枚举
     */
    private ErrorCode errorCodeEnum;

    /**
     * 错误码（兼容旧代码）
     */
    private int code;

    /**
     * 错误码字符串（兼容旧代码）
     */
    private String errorCode;

    /**
     * 构造一个带错误码枚举的异常
     *
     * @param errorCode 错误码枚举
     */
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCodeEnum = errorCode;
        this.code = errorCode.getCode();
        this.errorCode = errorCode.name();
    }

    /**
     * 构造一个带错误码枚举和自定义消息的异常
     *
     * @param errorCode     错误码枚举
     * @param customMessage 自定义错误消息
     */
    public BusinessException(ErrorCode errorCode, String customMessage) {
        super(customMessage);
        this.errorCodeEnum = errorCode;
        this.code = errorCode.getCode();
        this.errorCode = errorCode.name();
    }

    /**
     * 构造一个带错误码枚举、自定义消息和原因的异常
     *
     * @param errorCode     错误码枚举
     * @param customMessage 自定义错误消息
     * @param cause         原始异常
     */
    public BusinessException(ErrorCode errorCode, String customMessage, Throwable cause) {
        super(customMessage, cause);
        this.errorCodeEnum = errorCode;
        this.code = errorCode.getCode();
        this.errorCode = errorCode.name();
    }

    /**
     * 构造一个带错误码的异常（兼容旧代码）
     *
     * @param code 错误码
     */
    public BusinessException(int code) {
        this(ErrorCode.fromCode(code));
    }

    /**
     * 构造一个带错误码和额外消息的异常（兼容旧代码）
     *
     * @param code          错误码
     * @param customMessage 自定义错误消息
     */
    public BusinessException(int code, String customMessage) {
        this(ErrorCode.fromCode(code), customMessage);
    }

    /**
     * 构造一个带HTTP状态码和错误码的异常（兼容旧代码）
     */
    public BusinessException(int code, String errorCode, String message) {
        super(message);
        this.code = code;
        this.errorCode = errorCode;
    }

    /**
     * 构造一个只带错误码字符串的异常（兼容旧代码）
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

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.code = 500;
    }

    public BusinessException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    /**
     * 获取错误码
     */
    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    /**
     * 获取错误码字符串
     */
    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    /**
     * 获取错误码枚举
     */
    public ErrorCode getErrorCodeEnum() {
        return errorCodeEnum;
    }

    /**
     * 便捷静态方法：抛出用户不存在异常
     */
    public static BusinessException userNotFound() {
        return new BusinessException(ErrorCode.USER_NOT_EXIST);
    }

    /**
     * 便捷静态方法：抛出数据不存在异常
     */
    public static BusinessException dataNotFound() {
        return new BusinessException(ErrorCode.DATA_NOT_FOUND);
    }

    /**
     * 便捷静态方法：抛出参数错误异常
     */
    public static BusinessException paramError(String message) {
        return new BusinessException(ErrorCode.PARAM_ERROR, message);
    }

    /**
     * 便捷静态方法：抛出无权限异常
     */
    public static BusinessException noPermission() {
        return new BusinessException(ErrorCode.FORBIDDEN);
    }
}
