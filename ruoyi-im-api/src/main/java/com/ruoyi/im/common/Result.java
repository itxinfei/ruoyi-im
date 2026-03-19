package com.ruoyi.im.common;

import com.ruoyi.im.constant.ApiErrorCode;
import lombok.Data;

import java.io.Serializable;

/**
 * 统一响应结果类
 *
 * @param <T> 数据类型
 * @author ruoyi
 */
@Data
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 状态码 */
    private int code;

    /** 返回消息 */
    private String msg;

    /** 返回数据 */
    private T data;

    /** 时间戳 */
    private long timestamp;

    public Result() {
        this.timestamp = System.currentTimeMillis();
    }

    public Result(int code, String msg) {
        this.code = code;
        this.msg = msg;
        this.timestamp = System.currentTimeMillis();
    }

    public Result(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * 成功返回结果
     */
    public static <T> Result<T> success() {
        return new Result<>(ApiErrorCode.SUCCESS.getCode(), ApiErrorCode.SUCCESS.getMessage());
    }

    /**
     * 成功返回结果
     *
     * @param msg 消息
     */
    public static <T> Result<T> success(String msg) {
        return new Result<>(ApiErrorCode.SUCCESS.getCode(), msg);
    }

    /**
     * 成功返回结果
     *
     * @param data 数据
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(ApiErrorCode.SUCCESS.getCode(), ApiErrorCode.SUCCESS.getMessage(), data);
    }

    /**
     * 成功返回结果
     *
     * @param msg 消息
     * @param data 数据
     */
    public static <T> Result<T> success(String msg, T data) {
        return new Result<>(ApiErrorCode.SUCCESS.getCode(), msg, data);
    }

    /**
     * 失败返回结果
     */
    public static <T> Result<T> fail() {
        return new Result<>(ApiErrorCode.ERROR.getCode(), ApiErrorCode.ERROR.getMessage());
    }

    /**
     * 失败返回结果
     *
     * @param msg 消息
     */
    public static <T> Result<T> fail(String msg) {
        return new Result<>(ApiErrorCode.ERROR.getCode(), msg);
    }

    /**
     * 失败返回结果
     *
     * @param code 状态码
     * @param msg 消息
     */
    public static <T> Result<T> fail(int code, String msg) {
        return new Result<>(code, msg);
    }

    /**
     * 失败返回结果
     *
     * @param errorCode 错误码枚举
     */
    public static <T> Result<T> fail(ApiErrorCode errorCode) {
        return new Result<>(errorCode.getCode(), errorCode.getMessage());
    }

    /**
     * 错误返回结果（便捷方法，默认500状态码）
     *
     * @param msg 消息
     */
    public static <T> Result<T> error(String msg) {
        return new Result<>(ApiErrorCode.ERROR.getCode(), msg);
    }

    /**
     * 错误返回结果
     *
     * @param code 状态码
     * @param msg 消息
     */
    public static <T> Result<T> error(int code, String msg) {
        return new Result<>(code, msg);
    }

    /**
     * 错误返回结果
     *
     * @param errorCode 错误码枚举
     */
    public static <T> Result<T> error(ApiErrorCode errorCode) {
        return new Result<>(errorCode.getCode(), errorCode.getMessage());
    }

    /**
     * 错误返回结果
     *
     * @param code 状态码
     * @param msg 消息
     * @param data 数据
     */
    public static <T> Result<T> error(int code, String msg, T data) {
        return new Result<>(code, msg, data);
    }

    /**
     * 错误返回结果
     *
     * @param errorCode 错误码枚举
     * @param data 数据
     */
    public static <T> Result<T> error(ApiErrorCode errorCode, T data) {
        return new Result<>(errorCode.getCode(), errorCode.getMessage(), data);
    }

    /**
     * 错误返回结果
     *
     * @param code 状态码
     * @param msg 消息
     * @param data 数据
     */
    @SuppressWarnings("unchecked")
    public static <T> Result<T> error(int code, String msg, java.util.Map<String, String> data) {
        return new Result<>(code, msg, (T) data);
    }

    /**
     * 判断是否成功
     */
    public boolean isSuccess() {
        return this.code == ApiErrorCode.SUCCESS.getCode();
    }

}
