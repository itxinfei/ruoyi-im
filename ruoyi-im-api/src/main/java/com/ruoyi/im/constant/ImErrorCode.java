package com.ruoyi.im.constant;

/**
 * IM 错误码常量
 *
 * @author ruoyi
 */
public class ImErrorCode {

    /** 成功 */
    public static final int SUCCESS = 200;

    /** 失败 */
    public static final int ERROR = 500;

    /** 参数错误 */
    public static final int PARAM_ERROR = 400;

    /** 未授权 */
    public static final int UNAUTHORIZED = 401;

    /** 禁止访问 */
    public static final int FORBIDDEN = 403;

    /** 资源不存在 */
    public static final int NOT_FOUND = 404;

    // ========== 业务错误码 4000-4999 ==========

    /** 用户不存在 */
    public static final int USER_NOT_EXIST = 4001;

    /** 用户已存在 */
    public static final int USER_ALREADY_EXIST = 4002;

    /** 密码错误 */
    public static final int PASSWORD_ERROR = 4003;

    /** 会话不存在 */
    public static final int SESSION_NOT_EXIST = 4004;

    /** 消息不存在 */
    public static final int MESSAGE_NOT_EXIST = 4005;

    /** 群组不存在 */
    public static final int GROUP_NOT_EXIST = 4006;

    /** 好友关系不存在 */
    public static final int FRIEND_NOT_EXIST = 4007;

    /** 好友申请已存在 */
    public static final int FRIEND_REQUEST_ALREADY_EXIST = 4008;

    /** 不在群组中 */
    public static final int NOT_IN_GROUP = 4009;

    /** 消息撤回超时 */
    public static final int MESSAGE_RECALL_TIMEOUT = 4010;

    // ========== WebSocket 错误码 5000-5999 ==========

    /** WebSocket 连接失败 */
    public static final int WS_CONNECT_FAILED = 5001;

    /** WebSocket 连接断开 */
    public static final int WS_DISCONNECTED = 5002;

    /** 消息发送失败 */
    public static final int WS_SEND_FAILED = 5003;
}
