package com.ruoyi.im.common;

import com.ruoyi.im.constant.ImErrorCode;
import java.io.Serializable;

public class Result<T> implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private int code;
    
    private String msg;
    
    private T data;
    
    private long timestamp;
    
    public Result() {
        this.timestamp = System.currentTimeMillis();
    }
    
    public Result(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }
    
    public static <T> Result<T> success() {
        return new Result<>(200, "操作成功", null);
    }
    
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "操作成功", data);
    }
    
    public static <T> Result<T> success(String msg, T data) {
        return new Result<>(200, msg, data);
    }
    
    public static <T> Result<T> error() {
        return new Result<>(500, "操作失败", null);
    }
    
    public static <T> Result<T> error(String msg) {
        return new Result<>(500, msg, null);
    }
    
    public static <T> Result<T> error(int code, String msg) {
        return new Result<>(code, msg, null);
    }
    
    public static <T> Result<T> error(String errorCode, String msg) {
        return new Result<>(500, msg, null);
    }
    
    public static <T> Result<T> unauthorized() {
        return new Result<>(401, "未授权，请先登录", null);
    }
    
    public static <T> Result<T> forbidden() {
        return new Result<>(403, "无权限访问", null);
    }
    
    public static <T> Result<T> notFound() {
        return new Result<>(404, "资源不存在", null);
    }
    
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
    
    @Override
    public String toString() {
        return "Result{" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                ", data=" + data +
                ", timestamp=" + timestamp +
                '}';
    }
}
