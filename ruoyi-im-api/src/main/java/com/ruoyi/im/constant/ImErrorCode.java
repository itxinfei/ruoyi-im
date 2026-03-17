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

    /** 群成员不存在 */
    public static final int GROUP_MEMBER_NOT_EXIST = 4014;

    /** 只有群主可以操作 */
    public static final int OWNER_ONLY = 4015;

    /** 不能移除群主 */
    public static final int CANNOT_REMOVE_OWNER = 4016;

    /** 不能添加自己为好友 */
    public static final int CANNOT_ADD_SELF = 4017;

    /** 已经是好友关系 */
    public static final int ALREADY_FRIENDS = 4018;

    /** 好友申请不存在 */
    public static final int FRIEND_REQUEST_NOT_EXIST = 4019;

    /** 好友分组不存在 */
    public static final int FRIEND_GROUP_NOT_EXIST = 4020;

    // ========== 云盘相关错误码 5100-5199 ==========

    /** 文件夹不存在 */
    public static final int FOLDER_NOT_EXIST = 5101;

    /** 云文件不存在 */
    public static final int CLOUD_FILE_NOT_EXIST = 5102;

    /** 存储空间不足 */
    public static final int STORAGE_INSUFFICIENT = 5103;

    /** 分享不存在 */
    public static final int SHARE_NOT_EXIST = 5104;

    /** 分享已取消 */
    public static final int SHARE_CANCELLED = 5105;

    /** 分享已过期 */
    public static final int SHARE_EXPIRED = 5106;

    /** 云文件不在回收站 */
    public static final int CLOUD_FILE_NOT_IN_TRASH = 5107;

    /** 文件夹不在回收站 */
    public static final int FOLDER_NOT_IN_TRASH = 5108;

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

    // ========== 通用业务错误码 4300-4399 ==========

    /** 资源不存在 */
    public static final int NOT_EXIST = 4301;

    /** 名称已存在 */
    public static final int NAME_EXISTS = 4302;

    /** 编号已存在 */
    public static final int CODE_EXISTS = 4303;

    /** 已在群组中 */
    public static final int ALREADY_IN_GROUP = 4304;

    /** 不在群组中 */
    public static final int NOT_IN_GROUP_COMMON = 4305;

    /** 状态不正确 */
    public static final int STATUS_ERROR = 4306;

    /** 已存在 */
    public static final int ALREADY_EXISTS = 4307;

    /** 不允许操作 */
    public static final int NOT_ALLOWED = 4308;

    /** 已被使用 */
    public static final int IN_USE = 4309;

    /** 参数不完整 */
    public static final int PARAM_INCOMPLETE = 4310;

    // ========== 文件相关错误码 4400-4499 ==========

    /** 文件不存在 */
    public static final int FILE_NOT_FOUND = 4401;

    /** 文件上传失败 */
    public static final int FILE_UPLOAD_FAILED = 4402;

    /** 文件类型不支持 */
    public static final int FILE_TYPE_NOT_SUPPORTED = 4403;

    /** 文件大小超限 */
    public static final int FILE_SIZE_EXCEEDED = 4404;

    /** 文件为空 */
    public static final int FILE_EMPTY = 4405;

    /** 文件读取错误 */
    public static final int FILE_READ_ERROR = 4406;

    // ========== 部门组织相关错误码 4500-4599 ==========

    /** 部门不存在 */
    public static final int DEPARTMENT_NOT_EXIST = 4501;

    /** 部门名称已存在 */
    public static final int DEPARTMENT_NAME_EXISTS = 4502;

    /** 部门存在子部门 */
    public static final int DEPARTMENT_HAS_CHILDREN = 4503;

    /** 部门存在成员 */
    public static final int DEPARTMENT_HAS_MEMBERS = 4504;

    // ========== 会议相关错误码 4600-4699 ==========

    /** 会议室不存在 */
    public static final int MEETING_ROOM_NOT_EXIST = 4601;

    /** 会议室编号已存在 */
    public static final int MEETING_ROOM_CODE_EXISTS = 4602;

    /** 会议室不可预订 */
    public static final int MEETING_ROOM_NOT_AVAILABLE = 4603;

    /** 时间段已被预订 */
    public static final int TIME_SLOT_BOOKED = 4604;

    /** 预订不存在 */
    public static final int BOOKING_NOT_EXIST = 4605;

    /** 会议不存在 */
    public static final int MEETING_NOT_EXIST = 4606;

    // ========== 考勤相关错误码 4700-4799 ==========

    /** 考勤组不存在 */
    public static final int ATTENDANCE_GROUP_NOT_EXIST = 4701;

    /** 班次不存在 */
    public static final int SHIFT_NOT_EXIST = 4702;

    /** 打卡记录不存在 */
    public static final int ATTENDANCE_NOT_EXIST = 4703;

    // ========== 文档相关错误码 4800-4899 ==========

    /** 文档不存在 */
    public static final int DOCUMENT_NOT_EXIST = 4801;

    /** 无权限操作文档 */
    public static final int DOCUMENT_NO_PERMISSION = 4802;

    // ========== 任务相关错误码 4900-4999 ==========

    /** 任务不存在 */
    public static final int TASK_NOT_EXIST = 4901;

    /** 评论不存在 */
    public static final int COMMENT_NOT_EXIST = 4902;

    /** 附件不存在 */
    public static final int ATTACHMENT_NOT_EXIST = 4903;

    // ========== 审批相关错误码 5000-5099 ==========

    /** 审批不存在 */
    public static final int APPROVAL_NOT_EXIST = 5001;

    /** 模板不存在 */
    public static final int TEMPLATE_NOT_EXIST = 5002;

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

    // ========== 公告相关错误码 5200-5299 ==========

    /** 公告不存在 */
    public static final int ANNOUNCEMENT_NOT_EXIST = 5201;

    /** 公告不允许评论 */
    public static final int ANNOUNCEMENT_NO_COMMENT = 5202;

    // ========== 邮件相关错误码 5300-5399 ==========

    /** 邮件不存在 */
    public static final int EMAIL_NOT_EXIST = 5301;

    // ========== 应用相关错误码 5400-5499 ==========

    /** 应用不存在 */
    public static final int APPLICATION_NOT_EXIST = 5401;

    /** 应用编码已存在 */
    public static final int APPLICATION_CODE_EXISTS = 5402;

    // ========== 待办相关错误码 5500-5599 ==========

    /** 待办事项不存在 */
    public static final int TODO_NOT_EXIST = 5501;

    // ========== 日程相关错误码 5600-5699 ==========

    /** 日程不存在 */
    public static final int SCHEDULE_NOT_EXIST = 5601;

    // ========== 工作报告相关错误码 5700-5799 ==========

    /** 工作日志不存在 */
    public static final int WORK_REPORT_NOT_EXIST = 5701;

    // ========== 视频通话相关错误码 5800-5899 ==========

    /** 视频通话不存在 */
    public static final int VIDEO_CALL_NOT_EXIST = 5801;

    /** 视频通话状态不正确 */
    public static final int VIDEO_CALL_STATUS_ERROR = 5802;

    /** 对方不在线 */
    public static final int CALL_USER_OFFLINE = 5803;

    /** 对方正在通话中 */
    public static final int CALL_USER_BUSY = 5804;

    /** 通话人数超限 */
    public static final int VIDEO_CALL_MAX_PARTICIPANTS_EXCEEDED = 5805;

    /** 通话人数不足 */
    public static final int VIDEO_CALL_MIN_PARTICIPANTS = 5806;

    /** 邀请人数超限 */
    public static final int VIDEO_CALL_INVITE_EXCEEDS_LIMIT = 5807;

    /** 已在通话中 */
    public static final int VIDEO_CALL_ALREADY_IN_CALL = 5808;

    /** 非群组通话 */
    public static final int VIDEO_CALL_NOT_GROUP_CALL = 5809;

    /** 已加入通话 */
    public static final int VIDEO_CALL_ALREADY_JOINED = 5810;

    /** 已离开通话 */
    public static final int VIDEO_CALL_ALREADY_LEFT = 5811;

    /** 通话已满 */
    public static final int VIDEO_CALL_FULL = 5812;

    /** 不在通话中 */
    public static final int VIDEO_CALL_NOT_IN_CALL = 5813;

    // ========== 视频会议相关错误码 5900-5999 ==========

    /** 视频会议不存在 */
    public static final int VIDEO_MEETING_NOT_EXIST = 5901;

    /** 视频会议状态不正确 */
    public static final int VIDEO_MEETING_STATUS_ERROR = 5902;

    /** 会议人数已满 */
    public static final int MEETING_FULL = 5903;

    /** 会议密码错误 */
    public static final int MEETING_PASSWORD_ERROR = 5904;

    // ========== 外部联系人相关错误码 6000-6099 ==========

    /** 分组不存在 */
    public static final int CONTACT_GROUP_NOT_EXIST = 6001;

    /** 联系人不存在 */
    public static final int EXTERNAL_CONTACT_NOT_EXIST = 6002;

    // ========== 文件上传相关错误码 6100-6199 ==========

    /** 上传任务不存在 */
    public static final int UPLOAD_TASK_NOT_EXIST = 6101;

    /** 分片信息不存在 */
    public static final int CHUNK_NOT_EXIST = 6102;

    /** 文件合并失败 */
    public static final int FILE_MERGE_FAILED = 6103;

    // ========== 收藏相关错误码 6200-6299 ==========

    /** 收藏记录不存在 */
    public static final int FAVORITE_NOT_EXIST = 6201;

    /** 已收藏 */
    public static final int ALREADY_FAVORITED = 6202;

    // ========== 标记相关错误码 6300-6399 ==========

    /** 标记不存在 */
    public static final int MARKER_NOT_EXIST = 6301;

    // ========== 反应相关错误码 6400-6499 ==========

    /** 反应不存在 */
    public static final int REACTION_NOT_EXIST = 6401;

    // ========== 系统通知相关错误码 6500-6599 ==========

    /** 通知不存在 */
    public static final int NOTIFICATION_NOT_EXIST = 6501;

    // ========== DING消息相关错误码 6600-6699 ==========

    /** DING消息不存在 */
    public static final int DING_MESSAGE_NOT_EXIST = 6601;

    /** 语音转文字功能未启用 */
    public static final int VOICE_TRANSCRIPT_DISABLED = 6602;

    // ========== 协作相关错误码 6700-6799 ==========

    /** 协作者不存在 */
    public static final int COLLABORATOR_NOT_EXIST = 6701;

    /** 文档版本不存在 */
    public static final int DOCUMENT_VERSION_NOT_EXIST = 6702;
}