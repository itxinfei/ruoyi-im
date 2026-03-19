package com.ruoyi.im.constant;

/**
 * API错误码枚举
 * 定义系统中所有可能的错误码及其描述
 *
 * @author ruoyi
 */
public enum ApiErrorCode {
    
    // 通用错误码
    SUCCESS(200, "操作成功"),
    ERROR(500, "操作失败"),
    PARAM_ERROR(400, "参数错误"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "禁止访问"),
    NOT_FOUND(404, "资源不存在"),
    
    // 用户相关错误码
    USER_NOT_EXIST(4001, "用户不存在"),
    USER_ALREADY_EXIST(4002, "用户已存在"),
    PASSWORD_ERROR(4003, "密码错误"),
    REGISTER_FAILED(4011, "注册失败"),
    
    // 会话相关错误码
    SESSION_NOT_EXIST(4004, "会话不存在"),
    
    // 消息相关错误码
    MESSAGE_NOT_EXIST(4005, "消息不存在"),
    MESSAGE_RECALL_TIMEOUT(4010, "消息撤回超时"),
    
    // 群组相关错误码
    GROUP_NOT_EXIST(4006, "群组不存在"),
    
    // 好友相关错误码
    FRIEND_NOT_EXIST(4007, "好友关系不存在"),
    FRIEND_REQUEST_ALREADY_EXIST(4008, "好友申请已存在"),
    NOT_IN_GROUP(4009, "不在群组中"),
    GROUP_MEMBER_NOT_EXIST(4014, "群成员不存在"),
    OWNER_ONLY(4015, "只有群主可以操作"),
    CANNOT_REMOVE_OWNER(4016, "不能移除群主"),
    CANNOT_ADD_SELF(4017, "不能添加自己为好友"),
    ALREADY_FRIENDS(4018, "已经是好友关系"),
    FRIEND_REQUEST_NOT_EXIST(4019, "好友申请不存在"),
    FRIEND_GROUP_NOT_EXIST(4020, "好友分组不存在"),
    
    // 云盘相关错误码
    FOLDER_NOT_EXIST(5101, "文件夹不存在"),
    CLOUD_FILE_NOT_EXIST(5102, "云文件不存在"),
    STORAGE_INSUFFICIENT(5103, "存储空间不足"),
    SHARE_NOT_EXIST(5104, "分享不存在"),
    SHARE_CANCELLED(5105, "分享已取消"),
    SHARE_EXPIRED(5106, "分享已过期"),
    CLOUD_FILE_NOT_IN_TRASH(5107, "云文件不在回收站"),
    FOLDER_NOT_IN_TRASH(5108, "文件夹不在回收站"),
    
    // 会话相关错误码
    CONVERSATION_ALREADY_EXISTS(4012, "会话已存在"),
    CONVERSATION_NOT_FOUND(4013, "会话不存在"),
    GROUP_CONVERSATION_CANNOT_DELETE(4014, "群聊会话不能删除"),
    CONVERSATION_MEMBER_NOT_FOUND(4015, "会话成员不存在"),
    CREATE_CONVERSATION_FAILED(4016, "创建会话失败"),
    INVALID_CONVERSATION_TYPE(4017, "无效的会话类型"),
    CONVERSATION_NO_MEMBER(4018, "会话无成员"),
    NO_PERMISSION(4019, "无权限"),
    
    // 通用业务错误码
    NOT_EXIST(4301, "资源不存在"),
    NAME_EXISTS(4302, "名称已存在"),
    CODE_EXISTS(4303, "编号已存在"),
    ALREADY_IN_GROUP(4304, "已在群组中"),
    NOT_IN_GROUP_COMMON(4305, "不在群组中"),
    STATUS_ERROR(4306, "状态不正确"),
    ALREADY_EXISTS(4307, "已存在"),
    NOT_ALLOWED(4308, "不允许操作"),
    IN_USE(4309, "已被使用"),
    PARAM_INCOMPLETE(4310, "参数不完整"),
    
    // 文件相关错误码
    FILE_NOT_FOUND(4401, "文件不存在"),
    FILE_UPLOAD_FAILED(4402, "文件上传失败"),
    FILE_TYPE_NOT_SUPPORTED(4403, "文件类型不支持"),
    FILE_SIZE_EXCEEDED(4404, "文件大小超限"),
    FILE_EMPTY(4405, "文件为空"),
    FILE_READ_ERROR(4406, "文件读取错误"),
    
    // 部门组织相关错误码
    DEPARTMENT_NOT_EXIST(4501, "部门不存在"),
    DEPARTMENT_NAME_EXISTS(4502, "部门名称已存在"),
    DEPARTMENT_HAS_CHILDREN(4503, "部门存在子部门"),
    DEPARTMENT_HAS_MEMBERS(4504, "部门存在成员"),
    
    // 会议相关错误码
    MEETING_ROOM_NOT_EXIST(4601, "会议室不存在"),
    MEETING_ROOM_CODE_EXISTS(4602, "会议室编号已存在"),
    MEETING_ROOM_NOT_AVAILABLE(4603, "会议室不可预订"),
    TIME_SLOT_BOOKED(4604, "时间段已被预订"),
    BOOKING_NOT_EXIST(4605, "预订不存在"),
    MEETING_NOT_EXIST(4606, "会议不存在"),
    
    // 考勤相关错误码
    ATTENDANCE_GROUP_NOT_EXIST(4701, "考勤组不存在"),
    SHIFT_NOT_EXIST(4702, "班次不存在"),
    ATTENDANCE_NOT_EXIST(4703, "打卡记录不存在"),
    
    // 文档相关错误码
    DOCUMENT_NOT_EXIST(4801, "文档不存在"),
    DOCUMENT_NO_PERMISSION(4802, "无权限操作文档"),
    
    // 任务相关错误码
    TASK_NOT_EXIST(4901, "任务不存在"),
    COMMENT_NOT_EXIST(4902, "评论不存在"),
    ATTACHMENT_NOT_EXIST(4903, "附件不存在"),
    
    // 审批相关错误码
    APPROVAL_NOT_EXIST(5001, "审批不存在"),
    TEMPLATE_NOT_EXIST(5002, "模板不存在"),
    
    // 钉钉相关错误码
    DING_NO_TARGET_USER(4101, "钉钉用户不存在"),
    DING_NOT_FOUND(4102, "钉钉消息不存在"),
    
    // 群机器人相关错误码
    GROUP_NOT_FOUND(4201, "群组不存在（群机器人使用）"),
    BOT_NOT_FOUND(4202, "群机器人不存在"),
    GROUP_BOT_LIMIT_EXCEEDED(4203, "群机器人数量超限"),
    WEBHOOK_URL_INVALID(4204, "Webhook URL无效"),
    BOT_RULE_NOT_FOUND(4205, "机器人规则不存在"),
    
    // WebSocket错误码
    WS_CONNECT_FAILED(5001, "WebSocket连接失败"),
    WS_DISCONNECTED(5002, "WebSocket连接断开"),
    WS_SEND_FAILED(5003, "消息发送失败"),
    
    // 公告相关错误码
    ANNOUNCEMENT_NOT_EXIST(5201, "公告不存在"),
    ANNOUNCEMENT_NO_COMMENT(5202, "公告不允许评论"),
    
    // 邮件相关错误码
    EMAIL_NOT_EXIST(5301, "邮件不存在"),
    
    // 应用相关错误码
    APPLICATION_NOT_EXIST(5401, "应用不存在"),
    APPLICATION_CODE_EXISTS(5402, "应用编码已存在"),
    
    // 待办相关错误码
    TODO_NOT_EXIST(5501, "待办事项不存在"),
    
    // 日程相关错误码
    SCHEDULE_NOT_EXIST(5601, "日程不存在"),
    
    // 工作报告相关错误码
    WORK_REPORT_NOT_EXIST(5701, "工作报告不存在"),
    
    // 视频通话相关错误码
    VIDEO_CALL_NOT_EXIST(5801, "视频通话不存在"),
    VIDEO_CALL_STATUS_ERROR(5802, "视频通话状态不正确"),
    CALL_USER_OFFLINE(5803, "对方不在线"),
    CALL_USER_BUSY(5804, "对方正在通话中"),
    VIDEO_CALL_MAX_PARTICIPANTS_EXCEEDED(5805, "通话人数超限"),
    VIDEO_CALL_MIN_PARTICIPANTS(5806, "通话人数不足"),
    VIDEO_CALL_INVITE_EXCEEDS_LIMIT(5807, "邀请人数超限"),
    VIDEO_CALL_ALREADY_IN_CALL(5808, "已在通话中"),
    VIDEO_CALL_NOT_GROUP_CALL(5809, "非群组通话"),
    VIDEO_CALL_ALREADY_JOINED(5810, "已加入通话"),
    VIDEO_CALL_ALREADY_LEFT(5811, "已离开通话"),
    VIDEO_CALL_FULL(5812, "通话已满"),
    VIDEO_CALL_NOT_IN_CALL(5813, "不在通话中"),
    
    // 视频会议相关错误码
    VIDEO_MEETING_NOT_EXIST(5901, "视频会议不存在"),
    VIDEO_MEETING_STATUS_ERROR(5902, "视频会议状态不正确"),
    MEETING_FULL(5903, "会议人数已满"),
    MEETING_PASSWORD_ERROR(5904, "会议密码错误"),
    
    // 外部联系人相关错误码
    CONTACT_GROUP_NOT_EXIST(6001, "分组不存在"),
    EXTERNAL_CONTACT_NOT_EXIST(6002, "联系人不存在"),
    
    // 文件上传相关错误码
    UPLOAD_TASK_NOT_EXIST(6101, "上传任务不存在"),
    CHUNK_NOT_EXIST(6102, "分片信息不存在"),
    FILE_MERGE_FAILED(6103, "文件合并失败"),
    
    // 收藏相关错误码
    FAVORITE_NOT_EXIST(6201, "收藏记录不存在"),
    ALREADY_FAVORITED(6202, "已收藏"),
    
    // 标记相关错误码
    MARKER_NOT_EXIST(6301, "标记不存在"),
    
    // 反应相关错误码
    REACTION_NOT_EXIST(6401, "反应不存在"),
    
    // 系统通知相关错误码
    NOTIFICATION_NOT_EXIST(6501, "通知不存在"),
    
    // DING消息相关错误码
    DING_MESSAGE_NOT_EXIST(6601, "DING消息不存在"),
    VOICE_TRANSCRIPT_DISABLED(6602, "语音转文字功能未启用"),
    
    // 协作相关错误码
    COLLABORATOR_NOT_EXIST(6701, "协作者不存在"),
    DOCUMENT_VERSION_NOT_EXIST(6702, "文档版本不存在");

    private final int code;
    private final String message;

    ApiErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    /**
     * 根据错误码获取枚举
     *
     * @param code 错误码
     * @return 错误码枚举
     */
    public static ApiErrorCode fromCode(int code) {
        for (ApiErrorCode errorCode : values()) {
            if (errorCode.getCode() == code) {
                return errorCode;
            }
        }
        return ERROR; // 默认返回通用错误
    }
}