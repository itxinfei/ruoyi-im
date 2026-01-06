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
     * @param code 错误码
     */
    public BusinessException(int code) {
        super(getMessageByCode(code));
        this.code = code;
        this.errorCode = String.valueOf(code);
    }

    /**
     * 构造一个带错误码和额外消息的异常
     *
     * @param code          错误码
     * @param customMessage 自定义错误消息
     */
    public BusinessException(int code, String customMessage) {
        super(customMessage);
        this.code = code;
        this.errorCode = String.valueOf(code);
    }
    
    /**
     * 根据错误码获取默认消息
     *
     * @param code 错误码
     * @return 默认消息
     */
    private static String getMessageByCode(int code) {
        switch (code) {
            case ImErrorCode.SUCCESS:
                return "成功";
            case ImErrorCode.ERROR:
                return "失败";
            case ImErrorCode.PARAM_ERROR:
                return "参数错误";
            case ImErrorCode.UNAUTHORIZED:
                return "未授权";
            case ImErrorCode.FORBIDDEN:
                return "禁止访问";
            case ImErrorCode.NOT_FOUND:
                return "资源不存在";
            case ImErrorCode.USER_NOT_EXIST:
                return "用户不存在";
            case ImErrorCode.USER_ALREADY_EXIST:
                return "用户已存在";
            case ImErrorCode.PASSWORD_ERROR:
                return "密码错误";
            case ImErrorCode.SESSION_NOT_EXIST:
                return "会话不存在";
            case ImErrorCode.MESSAGE_NOT_EXIST:
                return "消息不存在";
            case ImErrorCode.GROUP_NOT_EXIST:
                return "群组不存在";
            case ImErrorCode.FRIEND_NOT_EXIST:
                return "好友关系不存在";
            case ImErrorCode.FRIEND_REQUEST_ALREADY_EXIST:
                return "好友申请已存在";
            case ImErrorCode.NOT_IN_GROUP:
                return "不在群组中";
            case ImErrorCode.MESSAGE_RECALL_TIMEOUT:
                return "消息撤回超时";
            case ImErrorCode.WS_CONNECT_FAILED:
                return "WebSocket连接失败";
            case ImErrorCode.WS_DISCONNECTED:
                return "WebSocket连接断开";
            case ImErrorCode.WS_SEND_FAILED:
                return "消息发送失败";
            default:
                return "未知错误";
        }
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
