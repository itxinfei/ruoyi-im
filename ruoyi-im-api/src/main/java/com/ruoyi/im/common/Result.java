package com.ruoyi.im.common;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

/**
 * 响应信息主体
 *
 * @author ruoyi
 */
@ApiModel(description = "响应结果封装")
public class Result<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("成功标志")
    private boolean success = true;

    @ApiModelProperty("消息")
    private String message;

    @ApiModelProperty("返回代码")
    private Integer code = 200;

    @ApiModelProperty("数据对象")
    private T data;

    /**
     * 成功返回结果
     */
    public static <T> Result<T> success() {
        Result<T> result = new Result<>();
        result.setSuccess(true);
        result.setCode(200);
        result.setMessage("操作成功");
        return result;
    }

    /**
     * 成功返回结果
     *
     * @param data 数据对象
     */
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setSuccess(true);
        result.setCode(200);
        result.setData(data);
        result.setMessage("操作成功");
        return result;
    }

    /**
     * 成功返回结果
     *
     * @param message 消息
     * @param data    数据对象
     */
    public static <T> Result<T> success(String message, T data) {
        Result<T> result = new Result<>();
        result.setSuccess(true);
        result.setCode(200);
        result.setMessage(message);
        result.setData(data);
        return result;
    }

    /**
     * 成功返回结果
     *
     * @param code    返回码
     * @param message 消息
     * @param data    数据对象
     */
    public static <T> Result<T> success(int code, String message, T data) {
        Result<T> result = new Result<>();
        result.setSuccess(true);
        result.setCode(code);
        result.setMessage(message);
        result.setData(data);
        return result;
    }

    /**
     * 成功返回结果
     *
     * @param code    返回码
     * @param message 消息
     */
    public static <T> Result<T> success(int code, String message) {
        Result<T> result = new Result<>();
        result.setSuccess(true);
        result.setCode(code);
        result.setMessage(message);
        return result;
    }

    /**
     * 失败返回结果
     */
    public static <T> Result<T> error() {
        Result<T> result = new Result<>();
        result.setSuccess(false);
        result.setCode(500);
        result.setMessage("操作失败");
        return result;
    }

    /**
     * 失败返回结果
     *
     * @param message 消息
     */
    public static <T> Result<T> error(String message) {
        Result<T> result = new Result<>();
        result.setSuccess(false);
        result.setCode(500);
        result.setMessage(message);
        return result;
    }

    /**
     * 失败返回结果
     *
     * @param code    返回码
     * @param message 消息
     */
    public static <T> Result<T> error(int code, String message) {
        Result<T> result = new Result<>();
        result.setSuccess(false);
        result.setCode(code);
        result.setMessage(message);
        return result;
    }

    /**
     * 失败返回结果
     *
     * @param code 返回码
     * @param data 数据对象
     */
    public static <T> Result<T> error(int code, T data) {
        Result<T> result = new Result<>();
        result.setSuccess(false);
        result.setCode(code);
        result.setData(data);
        result.setMessage("操作失败");
        return result;
    }

    /**
     * 失败返回结果
     *
     * @param code    返回码
     * @param message 消息
     * @param data    数据对象
     */
    public static <T> Result<T> error(int code, String message, T data) {
        Result<T> result = new Result<>();
        result.setSuccess(false);
        result.setCode(code);
        result.setMessage(message);
        result.setData(data);
        return result;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}