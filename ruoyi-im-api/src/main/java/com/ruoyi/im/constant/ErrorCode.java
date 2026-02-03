package com.ruoyi.im.constant;

/**
 * IM 统一错误码枚举
 *
 * 错误码规范：
 * - 200: 成功
 * - 400-499: 客户端错误
 * - 500-599: 服务器错误
 * - 1000-1999: 通用业务错误
 * - 2000-2999: 用户相关错误
 * - 3000-3999: 消息相关错误
 * - 4000-4999: 群组相关错误
 * - 5000-5999: 会话相关错误
 * - 6000-6999: 文件相关错误
 * - 7000-7999: 审批相关错误
 * - 8000-8999: 考勤相关错误
 * - 9000-9999: 系统相关错误
 *
 * @author ruoyi
 */
public enum ErrorCode {

    // ==================== 成功 ====================
    SUCCESS(200, "操作成功"),

    // ==================== 通用错误 ====================
    ERROR(500, "系统内部错误"),
    PARAM_ERROR(400, "参数错误"),
    UNAUTHORIZED(401, "未授权，请先登录"),
    FORBIDDEN(403, "权限不足，禁止访问"),
    NOT_FOUND(404, "请求的资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不支持"),
    CONFLICT(409, "数据冲突，请勿重复提交"),
    TOO_MANY_REQUESTS(429, "请求过于频繁，请稍后重试"),

    // ==================== 通用业务错误 (1000-1999) ====================
    OPERATION_FAILED(1000, "操作失败"),
    DATA_NOT_FOUND(1001, "数据不存在"),
    DATA_ALREADY_EXISTS(1002, "数据已存在"),
    DATA_IN_USE(1003, "数据正在使用中，无法删除"),
    INVALID_STATUS(1004, "状态不正确，无法执行此操作"),
    RATE_LIMIT_EXCEEDED(1005, "操作频率过高，请稍后重试"),

    // ==================== 用户相关错误 (2000-2999) ====================
    USER_NOT_EXIST(2001, "用户不存在"),
    USER_ALREADY_EXIST(2002, "用户已存在"),
    USER_DISABLED(2003, "用户已被禁用"),
    USER_LOCKED(2004, "用户已被锁定"),
    PASSWORD_ERROR(2005, "密码错误"),
    PASSWORD_SAME(2006, "新密码不能与旧密码相同"),
    PASSWORD_EXPIRED(2007, "密码已过期，请修改密码"),
    PHONE_ALREADY_REGISTERED(2008, "手机号已注册"),
    EMAIL_ALREADY_REGISTERED(2009, "邮箱已注册"),
    VERIFICATION_CODE_ERROR(2010, "验证码错误"),
    VERIFICATION_CODE_EXPIRED(2011, "验证码已过期"),
    LOGIN_SESSION_EXPIRED(2012, "登录会话已过期，请重新登录"),
    LOGIN_SESSION_INVALID(2013, "登录会话无效"),

    // ==================== 消息相关错误 (3000-3999) ====================
    MESSAGE_NOT_EXIST(3001, "消息不存在"),
    MESSAGE_RECALL_TIMEOUT(3002, "消息撤回时间已过"),
    MESSAGE_EDIT_TIMEOUT(3003, "消息编辑时间已过"),
    MESSAGE_ALREADY_RECALLED(3004, "消息已被撤回"),
    MESSAGE_SEND_FAILED(3005, "消息发送失败"),
    MESSAGE_CONTENT_EMPTY(3006, "消息内容不能为空"),
    MESSAGE_CONTENT_TOO_LONG(3007, "消息内容过长"),
    MESSAGE_TYPE_NOT_SUPPORTED(3008, "不支持的消息类型"),
    MESSAGE_FILE_TOO_LARGE(3009, "文件过大"),
    MESSAGE_CONTAINS_SENSITIVE_WORD(3010, "消息包含敏感词"),
    MENTION_NOT_EXIST(3011, "提及记录不存在"),
    REACTION_NOT_EXIST(3012, "表情反应不存在"),
    FAVORITE_NOT_EXIST(3013, "收藏记录不存在"),

    // ==================== 群组相关错误 (4000-4999) ====================
    GROUP_NOT_EXIST(4001, "群组不存在"),
    GROUP_ALREADY_EXISTS(4002, "群组已存在"),
    GROUP_NAME_DUPLICATE(4003, "群组名称已存在"),
    GROUP_MEMBER_LIMIT_EXCEEDED(4004, "群成员数量已达上限"),
    NOT_IN_GROUP(4005, "不在群组中"),
    NOT_GROUP_OWNER(4006, "不是群主"),
    NOT_GROUP_ADMIN(4007, "不是群管理员"),
    ALREADY_IN_GROUP(4008, "已在群组中"),
    GROUP_DISBANDED(4009, "群组已解散"),
    CANNOT_LEAVE_OWN_GROUP(4010, "群主不能退出群组，请先转让群主"),
    CANNOT_REMOVE_GROUP_OWNER(4011, "不能移除群主"),
    GROUP_ANNOUNCEMENT_NOT_EXIST(4012, "群公告不存在"),
    GROUP_FILE_NOT_EXIST(4013, "群文件不存在"),
    GROUP_BOT_LIMIT_EXCEEDED(4014, "群机器人数量已达上限"),
    BOT_NOT_FOUND(4015, "群机器人不存在"),
    WEBHOOK_URL_INVALID(4016, "Webhook URL无效"),
    BOT_RULE_NOT_FOUND(4017, "机器人规则不存在"),
    GROUP_MUTED(4018, "群组已开启全员禁言"),

    // ==================== 会话相关错误 (5000-5999) ====================
    CONVERSATION_NOT_FOUND(5001, "会话不存在"),
    CONVERSATION_ALREADY_EXISTS(5002, "会话已存在"),
    INVALID_CONVERSATION_TYPE(5003, "无效的会话类型"),
    CONVERSATION_MEMBER_NOT_FOUND(5004, "会话成员不存在"),
    CREATE_CONVERSATION_FAILED(5005, "创建会话失败"),
    GROUP_CONVERSATION_CANNOT_DELETE(5006, "群聊会话不能删除"),
    CONVERSATION_NO_MEMBER(5007, "会话没有成员"),

    // ==================== 好友相关错误 (6000-6099) ====================
    FRIEND_NOT_EXIST(6001, "好友关系不存在"),
    FRIEND_ALREADY_EXISTS(6002, "已经是好友关系"),
    FRIEND_REQUEST_ALREADY_EXIST(6003, "好友申请已存在"),
    FRIEND_REQUEST_NOT_EXIST(6004, "好友申请不存在"),
    FRIEND_REQUEST_ALREADY_PROCESSED(6005, "好友申请已处理"),
    CANNOT_ADD_SELF_AS_FRIEND(6006, "不能添加自己为好友"),
    IN_BLACKLIST(6007, "对方已在黑名单中"),
    FRIEND_GROUP_NOT_EXIST(6008, "好友分组不存在"),
    EXTERNAL_CONTACT_NOT_EXIST(6009, "外部联系人不存在"),

    // ==================== 文件相关错误 (7000-7099) ====================
    FILE_NOT_EXIST(7001, "文件不存在"),
    FILE_UPLOAD_FAILED(7002, "文件上传失败"),
    FILE_DOWNLOAD_FAILED(7003, "文件下载失败"),
    FILE_DELETE_FAILED(7004, "文件删除失败"),
    FILE_SIZE_EXCEEDED(7005, "文件大小超过限制"),
    FILE_TYPE_NOT_ALLOWED(7006, "不支持的文件类型"),
    FILE_NAME_INVALID(7007, "文件名无效"),
    ATTACHMENT_NOT_EXIST(7008, "附件不存在"),
    CHUNK_UPLOAD_FAILED(7009, "分片上传失败"),
    CHUNK_NOT_FOUND(7010, "分片不存在"),
    CHUNK_UPLOAD_MISMATCH(7011, "分片上传不匹配"),

    // ==================== 邮箱相关错误 (8000-8099) ====================
    EMAIL_NOT_EXIST(8001, "邮件不存在"),
    EMAIL_SEND_FAILED(8002, "邮件发送失败"),
    EMAIL_FOLDER_NOT_EXIST(8003, "邮件文件夹不存在"),
    EMAIL_ATTACHMENT_NOT_EXIST(8004, "邮件附件不存在"),
    EMAIL_TEMPLATE_NOT_EXIST(8005, "邮件模板不存在"),
    EMAIL_ALREADY_READ(8006, "邮件已标记为已读"),

    // ==================== 任务相关错误 (9000-9099) ====================
    TASK_NOT_EXIST(9001, "任务不存在"),
    TASK_ALREADY_COMPLETED(9002, "任务已完成"),
    TASK_CANNOT_UPDATE(9003, "任务状态不允许此操作"),
    TASK_COMMENT_NOT_EXIST(9004, "任务评论不存在"),
    TASK_PARENT_NOT_EXIST(9005, "父任务不存在"),
    TASK_HAS_SUBTASKS(9006, "任务包含子任务，无法删除"),
    TASK_ALREADY_FOLLOWED(9007, "已关注此任务"),
    TASK_NOT_FOLLOWED(9008, "未关注此任务"),
    TASK_ASSIGNEE_INVALID(9009, "任务负责人无效"),
    TASK_DEADLINE_INVALID(9010, "任务截止时间无效"),

    // ==================== 审批相关错误 (10000-10099) ====================
    APPROVAL_NOT_EXIST(10001, "审批不存在"),
    APPROVAL_ALREADY_APPROVED(10002, "审批已处理"),
    APPROVAL_NO_PERMISSION(10003, "无权限审批"),
    APPROVAL_FLOW_NOT_EXIST(10004, "审批流程不存在"),
    APPROVAL_NODE_NOT_EXIST(10005, "审批节点不存在"),
    APPROVAL_INSTANCE_NOT_EXIST(10006, "审批实例不存在"),

    // ==================== 考勤相关错误 (11000-11099) ====================
    ATTENDANCE_GROUP_NOT_EXIST(11001, "考勤组不存在"),
    ATTENDANCE_ALREADY_CHECKED_IN(11002, "今日已签到"),
    ATTENDANCE_ALREADY_CHECKED_OUT(11003, "今日已签退"),
    ATTENDANCE_TIME_INVALID(11004, "考勤时间无效"),
    ATTENDANCE_SHIFT_NOT_EXIST(11005, "班次不存在"),
    ATTENDANCE_HOLIDAY_NOT_EXIST(11006, "节假日不存在"),
    ATTENDANCE_RECORD_NOT_EXIST(11007, "考勤记录不存在"),

    // ==================== DING消息相关错误 (12000-12099) ====================
    DING_NOT_FOUND(12001, "DING消息不存在"),
    DING_ALREADY_CONFIRMED(12002, "DING已确认"),
    DING_NO_TARGET_USER(12003, "DING目标用户不存在"),
    DING_TEMPLATE_NOT_EXIST(12004, "DING模板不存在"),
    DING_TIME_INVALID(12005, "DING定时时间无效"),

    // ==================== 文档相关错误 (13000-13099) ====================
    DOCUMENT_NOT_EXIST(13001, "文档不存在"),
    DOCUMENT_NO_PERMISSION(13002, "无权限操作此文档"),
    DOCUMENT_VERSION_NOT_EXIST(13003, "文档版本不存在"),
    DOCUMENT_COMMENT_NOT_EXIST(13004, "文档评论不存在"),
    DOCUMENT_LOCKED(13005, "文档已被锁定"),

    // ==================== 视频通话相关错误 (14000-14099) ====================
    VIDEO_CALL_NOT_EXIST(14001, "通话不存在"),
    VIDEO_CALL_ALREADY_ENDED(14002, "通话已结束"),
    VIDEO_CALL_PARTICIPANT_FULL(14003, "通话人数已满"),
    VIDEO_CALL_NOT_IN_CALL(14004, "不在通话中"),
    VIDEO_CALL_ALREADY_ACCEPTED(14005, "通话已被接听"),
    VIDEO_CALL_TIMEOUT(14006, "通话超时未接听"),
    VIDEO_CALL_BUSY(14007, "用户忙"),
    VIDEO_CALL_REJECTED(14008, "通话被拒绝"),
    MEETING_ROOM_NOT_EXIST(14009, "会议室不存在"),

    // ==================== 云盘相关错误 (15000-15099) ====================
    CLOUD_FOLDER_NOT_EXIST(15001, "文件夹不存在"),
    CLOUD_FOLDER_HAS_CHILDREN(15002, "文件夹非空，无法删除"),
    CLOUD_FILE_NOT_EXIST(15003, "云盘文件不存在"),
    CLOUD_SHARE_NOT_EXIST(15004, "分享记录不存在"),
    CLOUD_SHARE_EXPIRED(15005, "分享已过期"),
    CLOUD_SHARE_PASSWORD_ERROR(15006, "分享密码错误"),
    CLOUD_STORAGE_LIMIT_EXCEEDED(15007, "存储空间不足"),

    // ==================== 应用中心相关错误 (16000-16099) ====================
    APP_NOT_EXIST(16001, "应用不存在"),
    APP_ALREADY_INSTALLED(16002, "应用已安装"),
    APP_NOT_INSTALLED(16003, "应用未安装"),
    APP_INSTALL_FAILED(16004, "应用安装失败"),
    APP_UNINSTALL_FAILED(16005, "应用卸载失败"),
    APP_CONFIG_NOT_EXIST(16006, "应用配置不存在"),
    APP_PERMISSION_DENIED(16007, "应用权限不足"),

    // ==================== 日程相关错误 (17000-17099) ====================
    SCHEDULE_NOT_EXIST(17001, "日程不存在"),
    SCHEDULE_TIME_CONFLICT(17002, "日程时间冲突"),
    SCHEDULE_PARTICIPANT_INVALID(17003, "日程参与者无效"),
    SCHEDULE_ALREADY_PAST(17004, "日程时间已过"),
    SCHEDULE_REPEAT_INVALID(17005, "重复规则无效"),
    SCHEDULE_REMINDER_NOT_EXIST(17006, "提醒记录不存在"),

    // ==================== 工作报告相关错误 (18000-18099) ====================
    WORK_REPORT_NOT_EXIST(18001, "工作报告不存在"),
    WORK_REPORT_ALREADY_SUBMITTED(18002, "报告已提交"),
    WORK_REPORT_COMMENT_NOT_EXIST(18003, "评论不存在"),
    WORK_REPORT_PERIOD_INVALID(18004, "报告周期无效"),

    // ==================== 系统相关错误 (90000-90999) ====================
    SYSTEM_CONFIG_NOT_EXIST(90001, "系统配置不存在"),
    SYSTEM_MAINTENANCE(90002, "系统维护中"),
    DATA_IMPORT_FAILED(90003, "数据导入失败"),
    DATA_EXPORT_FAILED(90004, "数据导出失败"),
    BACKUP_NOT_EXIST(90005, "备份不存在"),
    BACKUP_RESTORE_FAILED(90006, "备份恢复失败"),
    AUDIT_LOG_NOT_EXIST(90007, "审计日志不存在"),
    DEPARTMENT_NOT_EXIST(90008, "部门不存在"),
    ROLE_NOT_EXIST(90009, "角色不存在"),
    PERMISSION_NOT_EXIST(90010, "权限不存在"),
    ROLE_ALREADY_ASSIGNED(90011, "角色已分配"),
    ROLE_HAS_USERS(90012, "角色包含用户，无法删除"),

    // ==================== WebSocket相关错误 (95000-95099) ====================
    WS_CONNECT_FAILED(95001, "WebSocket连接失败"),
    WS_DISCONNECTED(95002, "WebSocket连接断开"),
    WS_SEND_FAILED(95003, "消息发送失败"),
    WS_AUTH_FAILED(95004, "WebSocket认证失败"),
    WS_SESSION_NOT_EXIST(95005, "WebSocket会话不存在"),
    WS_MESSAGE_FORMAT_ERROR(95006, "消息格式错误");

    /**
     * 错误码
     */
    private final int code;

    /**
     * 错误描述
     */
    private final String message;

    /**
     * 错误级别（用于日志记录）
     */
    private final Level level;

    /**
     * 错误级别枚举
     */
    public enum Level {
        INFO, WARN, ERROR
    }

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
        this.level = determineLevel(code);
    }

    /**
     * 根据错误码确定错误级别
     */
    private Level determineLevel(int code) {
        if (code >= 500) {
            return Level.ERROR;
        } else if (code >= 400) {
            return Level.WARN;
        }
        return Level.INFO;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public Level getLevel() {
        return level;
    }

    /**
     * 根据错误码获取枚举
     */
    public static ErrorCode fromCode(int code) {
        for (ErrorCode errorCode : values()) {
            if (errorCode.code == code) {
                return errorCode;
            }
        }
        return ERROR;
    }

    /**
     * 判断是否为成功
     */
    public boolean isSuccess() {
        return this == SUCCESS;
    }

    /**
     * 判断是否为客户端错误 (4xx)
     */
    public boolean isClientError() {
        return code >= 400 && code < 500;
    }

    /**
     * 判断是否为服务器错误 (5xx)
     */
    public boolean isServerError() {
        return code >= 500 && code < 600;
    }

    /**
     * 获取业务错误码分类
     */
    public String getCategory() {
        if (code >= 2000 && code < 3000) {
            return "USER";
        } else if (code >= 3000 && code < 4000) {
            return "MESSAGE";
        } else if (code >= 4000 && code < 5000) {
            return "GROUP";
        } else if (code >= 5000 && code < 6000) {
            return "CONVERSATION";
        } else if (code >= 6000 && code < 7000) {
            return "FRIEND";
        } else if (code >= 7000 && code < 8000) {
            return "FILE";
        } else if (code >= 8000 && code < 9000) {
            return "EMAIL";
        } else if (code >= 9000 && code < 10000) {
            return "TASK";
        } else if (code >= 10000 && code < 11000) {
            return "APPROVAL";
        } else if (code >= 11000 && code < 12000) {
            return "ATTENDANCE";
        } else if (code >= 12000 && code < 13000) {
            return "DING";
        } else if (code >= 13000 && code < 14000) {
            return "DOCUMENT";
        } else if (code >= 14000 && code < 15000) {
            return "VIDEO_CALL";
        } else if (code >= 15000 && code < 16000) {
            return "CLOUD";
        } else if (code >= 16000 && code < 17000) {
            return "APP";
        } else if (code >= 17000 && code < 18000) {
            return "SCHEDULE";
        } else if (code >= 18000 && code < 19000) {
            return "WORK_REPORT";
        } else if (code >= 90000) {
            return "SYSTEM";
        }
        return "UNKNOWN";
    }
}
