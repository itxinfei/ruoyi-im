package com.ruoyi.im.common;

import java.io.Serializable;

/**
 * 统一响应结果类
 *
 * @param <T> 数据类型
 * @author ruoyi
 */
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
        return new Result<>(200, "操作成功");
    }

    /**
     * 成功返回结果
     *
     * @param msg 消息
     */
    public static <T> Result<T> success(String msg) {
        return new Result<>(200, msg);
    }

    /**
     * 成功返回结果
     *
     * @param data 数据
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "操作成功", data);
    }

    /**
     * 成功返回结果
     *
     * @param msg 消息
     * @param data 数据
     */
    public static <T> Result<T> success(String msg, T data) {
        return new Result<>(200, msg, data);
    }

    /**
     * 失败返回结果
     */
    public static <T> Result<T> fail() {
        return new Result<>(500, "操作失败");
    }

    /**
     * 失败返回结果
     *
     * @param msg 消息
     */
    public static <T> Result<T> fail(String msg) {
        return new Result<>(500, msg);
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
     * @param code 状态码
     * @param msg 消息
     * @param data 数据
     */
    public static <T> Result<T> error(int code, String msg, java.util.Map<String, String> data) {
        return new Result<>(code, msg, (T) data);
    }

    /**
     * 判断是否成功
     */
    public boolean isSuccess() {
        return this.code == 200;
    }

    // Getter and Setter
    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
