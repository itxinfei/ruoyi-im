package com.ruoyi.im.common;

import com.ruoyi.im.constant.ImErrorCode;
import java.io.Serializable;
import java.util.Map;

/**
 * 统一API响应结果类
 * 
 * <p>该类用于封装API的响应结果，提供统一的响应格式，包含状态码、消息、数据等字段。</p>
 * 
 * @param <T> 响应数据类型
 * @author ruoyi
 */
public class Result<T> implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 响应状态码
     */
    private int code;
    
    /**
     * 响应消息
     */
    private String message;
    
    /**
     * 响应数据
     */
    private T data;
    
    /**
     * 响应时间戳
     */
    private long timestamp;
    
    /**
     * 错误详情，用于参数校验等场景
     */
    private Map<String, String> errors;
    
    /**
     * 请求追踪ID，用于日志追踪
     */
    private String traceId;
    
    /**
     * 默认构造函数
     */
    public Result() {
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * 构造函数
     * 
     * @param code 状态码
     * @param message 响应消息
     * @param data 响应数据
     */
    public Result(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * 返回成功结果（无数据）
     * 
     * @param <T> 泛型类型
     * @return 成功响应结果
     */
    public static <T> Result<T> success() {
        return new Result<>(200, "操作成功", null);
    }
    
    /**
     * 返回成功结果（带数据）
     * 
     * @param <T> 泛型类型
     * @param data 响应数据
     * @return 成功响应结果
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "操作成功", data);
    }
    
    /**
     * 返回成功结果（带消息和数据）
     * 
     * @param <T> 泛型类型
     * @param message 响应消息
     * @param data 响应数据
     * @return 成功响应结果
     */
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(200, message, data);
    }
    
    /**
     * 返回成功结果（带自定义状态码和消息）
     * 
     * @param <T> 泛型类型
     * @param code 状态码
     * @param message 响应消息
     * @return 成功响应结果
     */
    public static <T> Result<T> success(int code, String message) {
        return new Result<>(code, message, null);
    }
    
    /**
     * 返回成功结果（带自定义状态码、消息和数据）
     * 
     * @param <T> 泛型类型
     * @param code 状态码
     * @param message 响应消息
     * @param data 响应数据
     * @return 成功响应结果
     */
    public static <T> Result<T> success(int code, String message, T data) {
        return new Result<>(code, message, data);
    }
    
    /**
     * 返回成功结果（带消息和数据，泛型类型推断）
     * 
     * @param <T> 泛型类型
     * @param data 响应数据
     * @param message 响应消息
     * @return 成功响应结果
     */
    public static <T> Result<T> successData(T data, String message) {
        return new Result<>(200, message, data);
    }
    
    /**
     * 返回成功结果（带错误码和数据的简化版本）
     * 
     * @param <T> 泛型类型
     * @param data 响应数据
     * @return 成功响应结果
     */
    public static <T> Result<T> ok(T data) {
        return new Result<>(200, "操作成功", data);
    }
    
    /**
     * 返回成功结果（带消息和数据，简化版本）
     * 
     * @param <T> 泛型类型
     * @param data 响应数据
     * @param message 响应消息
     * @return 成功响应结果
     */
    public static <T> Result<T> ok(T data, String message) {
        return new Result<>(200, message, data);
    }
    
    /**
     * 判断响应是否成功
     * 
     * @return 是否成功
     */
    public boolean isSuccess() {
        return this.code == 200;
    }
    
    /**
     * 判断响应是否为错误
     * 
     * @return 是否为错误
     */
    public boolean isError() {
        return this.code != 200;
    }
    
    /**
     * 获取响应状态描述
     * 
     * @return 响应状态描述
     */
    public String getStatusText() {
        if (isSuccess()) {
            return "成功";
        } else if (code == 401) {
            return "未授权";
        } else if (code == 403) {
            return "禁止访问";
        } else if (code == 404) {
            return "资源不存在";
        } else if (code >= 400 && code < 500) {
            return "客户端错误";
        } else if (code >= 500) {
            return "服务器错误";
        } else {
            return "未知状态";
        }
    }
    
    /**
     * 返回错误结果
     * 
     * @param <T> 泛型类型
     * @return 错误响应结果
     */
    public static <T> Result<T> error() {
        return new Result<>(500, "操作失败", null);
    }
    
    /**
     * 返回错误结果（带消息）
     * 
     * @param <T> 泛型类型
     * @param message 错误消息
     * @return 错误响应结果
     */
    public static <T> Result<T> error(String message) {
        return new Result<>(500, message, null);
    }
    
    /**
     * 返回错误结果（带状态码和消息）
     * 
     * @param <T> 泛型类型
     * @param code 状态码
     * @param message 错误消息
     * @return 错误响应结果
     */
    public static <T> Result<T> error(int code, String message) {
        return new Result<>(code, message, null);
    }
    
    /**
     * 返回带错误详情的错误响应
     * 
     * @param <T> 泛型类型
     * @param code 状态码
     * @param message 错误消息
     * @param errors 错误详情
     * @return 错误响应结果
     */
    public static <T> Result<T> error(int code, String message, Map<String, String> errors) {
        Result<T> result = new Result<>(code, message, null);
        result.setErrors(errors);
        return result;
    }
    
    /**
     * 返回带错误详情的错误响应（使用错误码枚举）
     * 
     * @param <T> 泛型类型
     * @param errorCode 错误码枚举
     * @param errors 错误详情
     * @return 错误响应结果
     */
    public static <T> Result<T> error(ImErrorCode errorCode, Map<String, String> errors) {
        Result<T> result = new Result<>(errorCode.getCode(), errorCode.getMessage(), null);
        result.setErrors(errors);
        return result;
    }
    
    /**
     * 返回带请求追踪ID的成功响应
     * 
     * @param <T> 泛型类型
     * @param message 响应消息
     * @param data 响应数据
     * @param traceId 请求追踪ID
     * @return 成功响应结果
     */
    public static <T> Result<T> successWithTrace(String message, T data, String traceId) {
        Result<T> result = new Result<>(200, message, data);
        result.setTraceId(traceId);
        return result;
    }
    
    /**
     * 返回带请求追踪ID的错误响应
     * 
     * @param <T> 泛型类型
     * @param code 状态码
     * @param message 错误消息
     * @param traceId 请求追踪ID
     * @return 错误响应结果
     */
    public static <T> Result<T> errorWithTrace(int code, String message, String traceId) {
        Result<T> result = new Result<>(code, message, null);
        result.setTraceId(traceId);
        return result;
    }
    
    /**
     * 返回未授权响应
     * 
     * @param <T> 泛型类型
     * @return 未授权响应结果
     */
    public static <T> Result<T> unauthorized() {
        return new Result<>(401, "未授权，请先登录", null);
    }
    
    /**
     * 返回禁止访问响应
     * 
     * @param <T> 泛型类型
     * @return 禁止访问响应结果
     */
    public static <T> Result<T> forbidden() {
        return new Result<>(403, "无权限访问", null);
    }
    
    /**
     * 返回资源不存在响应
     * 
     * @param <T> 泛型类型
     * @return 资源不存在响应结果
     */
    public static <T> Result<T> notFound() {
        return new Result<>(404, "资源不存在", null);
    }
    
    /**
     * 获取状态码
     * 
     * @return 状态码
     */
    public int getCode() {
        return code;
    }
    
    /**
     * 设置状态码
     * 
     * @param code 状态码
     */
    public void setCode(int code) {
        this.code = code;
    }
    
    /**
     * 获取响应消息
     * 
     * @return 响应消息
     */
    public String getMessage() {
        return message;
    }
    
    /**
     * 设置响应消息
     * 
     * @param message 响应消息
     */
    public void setMessage(String message) {
        this.message = message;
    }
    
    /**
     * 获取响应数据
     * 
     * @return 响应数据
     */
    public T getData() {
        return data;
    }
    
    /**
     * 设置响应数据
     * 
     * @param data 响应数据
     */
    public void setData(T data) {
        this.data = data;
    }
    
    /**
     * 获取时间戳
     * 
     * @return 时间戳
     */
    public long getTimestamp() {
        return timestamp;
    }
    
    /**
     * 设置时间戳
     * 
     * @param timestamp 时间戳
     */
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    /**
     * 获取错误详情
     * 
     * @return 错误详情
     */
    public Map<String, String> getErrors() {
        return errors;
    }
    
    /**
     * 设置错误详情
     * 
     * @param errors 错误详情
     */
    public void setErrors(Map<String, String> errors) {
        this.errors = errors;
    }
    
    /**
     * 获取请求追踪ID
     * 
     * @return 请求追踪ID
     */
    public String getTraceId() {
        return traceId;
    }
    
    /**
     * 设置请求追踪ID
     * 
     * @param traceId 请求追踪ID
     */
    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }
    
    /**
     * 验证响应结果的完整性
     * 
     * @return 验证结果
     */
    public Result<T> validate() {
        if (isError() && (message == null || message.isEmpty())) {
            this.message = "操作失败";
        }
        return this;
    }
    
    /**
     * 验证响应结果的完整性（带默认错误消息）
     * 
     * @param defaultErrorMessage 默认错误消息
     * @return 验证结果
     */
    public Result<T> validate(String defaultErrorMessage) {
        if (isError() && (message == null || message.isEmpty())) {
            this.message = defaultErrorMessage;
        }
        return this;
    }
    
    /**
     * 创建结果的副本
     * 
     * @return 结果副本
     */
    public Result<T> copy() {
        Result<T> copy = new Result<>(code, message, data);
        copy.setTimestamp(timestamp);
        copy.setErrors(errors);
        copy.setTraceId(traceId);
        return copy;
    }

    
    /**
     * 创建失败的响应结果
     * 
     * @param <T> 泛型类型
     * @param message 错误消息
     * @return 失败响应结果
     */
    public static <T> Result<T> fail(String message) {
        return error(message);
    }
    
    /**
     * 创建失败的响应结果
     * 
     * @param <T> 泛型类型
     * @param code 状态码
     * @param message 错误消息
     * @return 失败响应结果
     */
    public static <T> Result<T> fail(int code, String message) {
        return error(code, message);
    }
    
    /**
     * 转换为字符串表示
     * 
     * @return 字符串表示
     */
    @Override
    public String toString() {
        return "Result{" +
                "code=" + code +
                ", message='" + message + '\'' +
                ", data=" + data +
                ", timestamp=" + timestamp +
                ", errors=" + errors +
                ", traceId='" + traceId + '\'' +
                '}';
    }
}