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

    /** 账号异常 */
    public static final int ACCOUNT_ERROR = 4030;

    /** 注册失败 */
    public static final int REGISTER_FAILED = 4011;

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

    /** 会话已存在 */
    public static final int CONVERSATION_ALREADY_EXISTS = 4012;

    /** 会话不存在 */
    public static final int CONVERSATION_NOT_FOUND = 4013;

    /** 群聊会话不能删除 */
    public static final int GROUP_CONVERSATION_CANNOT_DELETE = 4014;

    /** 会话成员不存在 */
    public static final int CONVERSATION_MEMBER_NOT_FOUND = 4015;

    /** 创建会话失败 */
    public static final int CREATE_CONVERSATION_FAILED = 4016;

    /** 无效的会话类型 */
    public static final int INVALID_CONVERSATION_TYPE = 4017;

    /** 会话成员不存在 */
    public static final int CONVERSATION_NO_MEMBER = 4018;

    /** 无权限 */
    public static final int NO_PERMISSION = 4019;

    // ========== 钉钉相关错误码 4100-4199 ==========

    /** 钉钉用户不存在 */
    public static final int DING_NO_TARGET_USER = 4101;

    /** 钉钉消息不存在 */
    public static final int DING_NOT_FOUND = 4102;

    // ========== 群机器人相关错误码 4200-4299 ==========

    /** 群组不存在（群机器人使用） */
    public static final int GROUP_NOT_FOUND = 4201;

    /** 群机器人不存在 */
    public static final int BOT_NOT_FOUND = 4202;

    /** 群机器人数量超限 */
    public static final int GROUP_BOT_LIMIT_EXCEEDED = 4203;

    /** Webhook URL无效 */
    public static final int WEBHOOK_URL_INVALID = 4204;

    /** 机器人规则不存在 */
    public static final int BOT_RULE_NOT_FOUND = 4205;

    // ========== WebSocket 错误码 5000-5999 ==========

    /** WebSocket 连接失败 */
    public static final int WS_CONNECT_FAILED = 5001;

    /** WebSocket 连接断开 */
    public static final int WS_DISCONNECTED = 5002;

    /** 消息发送失败 */
    public static final int WS_SEND_FAILED = 5003;
}