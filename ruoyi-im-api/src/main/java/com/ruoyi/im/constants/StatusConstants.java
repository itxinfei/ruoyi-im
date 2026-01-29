package com.ruoyi.im.constants;

/**
 * 通用状态常量
 * 用于跨模块的统一状态管理
 *
 * @author ruoyi
 */
public class StatusConstants {

    /**
     * 通用任务/工单状态
     */
    public static class Task {
        /** 待办 */
        public static final String PENDING = "PENDING";

        /** 进行中 */
        public static final String IN_PROGRESS = "IN_PROGRESS";

        /** 已完成 */
        public static final String COMPLETED = "COMPLETED";

        /** 已取消 */
        public static final String CANCELLED = "CANCELLED";

        /** 已过期 */
        public static final String EXPIRED = "EXPIRED";

        /** 活跃 */
        public static final String ACTIVE = "ACTIVE";

        /** 阻塞 */
        public static final String BLOCKED = "BLOCKED";
    }

    /**
     * 审批状态
     */
    public static class Approval {
        /** 待审批 */
        public static final String PENDING = "PENDING";

        /** 已批准 */
        public static final String APPROVED = "APPROVED";

        /** 已拒绝 */
        public static final String REJECTED = "REJECTED";
    }

    /**
     * 好友请求状态
     */
    public static class FriendRequest {
        /** 待处理 */
        public static final String PENDING = "PENDING";

        /** 已接受 */
        public static final String ACCEPTED = "ACCEPTED";

        /** 已拒绝 */
        public static final String REJECTED = "REJECTED";
    }

    /**
     * 会议状态
     */
    public static class Meeting {
        /** 已预定 */
        public static final String SCHEDULED = "SCHEDULED";

        /** 进行中 */
        public static final String IN_PROGRESS = "IN_PROGRESS";

        /** 已结束 */
        public static final String ENDED = "ENDED";

        /** 已取消 */
        public static final String CANCELLED = "CANCELLED";
    }

    /**
     * 会议参与者角色
     */
    public static class ParticipantRole {
        /** 主持人 */
        public static final String HOST = "HOST";

        /** 参会者 */
        public static final String ATTENDEE = "ATTENDEE";
    }

    /**
     * 会议参与者状态
     */
    public static class ParticipantStatus {
        /** 已邀请 */
        public static final String INVITED = "INVITED";

        /** 已加入 */
        public static final String JOINED = "JOINED";

        /** 已离开 */
        public static final String LEFT = "LEFT";
    }

    /**
     * 视频会议类型
     */
    public static class VideoMeetingType {
        /** 常规会议 */
        public static final String MEETING = "MEETING";

        /** 网络研讨会 */
        public static final String WEBINAR = "WEBINAR";
    }

    /**
     * 会议事件类型（用于WebSocket通知）
     */
    public static class MeetingEvent {
        /** 已取消 */
        public static final String CANCELLED = "CANCELLED";

        /** 已开始 */
        public static final String STARTED = "STARTED";

        /** 已结束 */
        public static final String ENDED = "ENDED";
    }

    /**
     * 通用活跃状态
     */
    public static class Active {
        /** 活跃/启用 */
        public static final String ACTIVE = "ACTIVE";

        /** 未激活/禁用 */
        public static final String INACTIVE = "INACTIVE";
    }

    /**
     * 操作结果
     */
    public static class OperationResult {
        /** 成功 */
        public static final String SUCCESS = "SUCCESS";

        /** 失败 */
        public static final String FAILED = "FAILED";
    }

    /**
     * 考勤打卡方式
     */
    public static class CheckMethod {
        /** 地理位置 */
        public static final String LOCATION = "LOCATION";

        /** 人脸 */
        public static final String FACE = "FACE";

        /** Wi-Fi */
        public static final String WIFI = "WIFI";

        /** 混合 */
        public static final String MIXED = "MIXED";
    }

    /**
     * 考勤组成员角色
     */
    public static class AttendanceMemberRole {
        /** 管理员 */
        public static final String ADMIN = "ADMIN";

        /** 普通成员 */
        public static final String MEMBER = "MEMBER";
    }

    /**
     * 群组成员角色
     */
    public static class GroupMemberRole {
        /** 群主 */
        public static final String OWNER = "OWNER";

        /** 管理员 */
        public static final String ADMIN = "ADMIN";

        /** 普通成员 */
        public static final String MEMBER = "MEMBER";
    }

    /**
     * 云盘资源类型
     */
    public static class CloudResourceType {
        /** 文件 */
        public static final String FILE = "FILE";

        /** 文件夹 */
        public static final String FOLDER = "FOLDER";
    }

    /**
     * 文件夹权限类型
     */
    public static class FolderPermission {
        /** 私有 */
        public static final String PRIVATE = "PRIVATE";

        /** 部门共享 */
        public static final String DEPARTMENT = "DEPARTMENT";

        /** 全公司共享 */
        public static final String COMPANY = "COMPANY";
    }

    /**
     * 视频通话类型
     */
    public static class CallType {
        /** 视频通话 */
        public static final String VIDEO = "VIDEO";

        /** 语音通话 */
        public static final String AUDIO = "AUDIO";
    }

    /**
     * 公告状态
     */
    public static class AnnouncementStatus {
        /** 草稿 */
        public static final String DRAFT = "DRAFT";

        /** 已发布 */
        public static final String PUBLISHED = "PUBLISHED";

        /** 已撤回 */
        public static final String WITHDRAWN = "WITHDRAWN";

        /** 已过期 */
        public static final String EXPIRED = "EXPIRED";
    }

    /**
     * 公告类型
     */
    public static class AnnouncementType {
        /** 系统公告 */
        public static final String SYSTEM = "SYSTEM";

        /** 部门公告 */
        public static final String DEPARTMENT = "DEPARTMENT";

        /** 项目公告 */
        public static final String PROJECT = "PROJECT";
    }

    /**
     * 班次类型
     */
    public static class ShiftType {
        /** 正常班 */
        public static final String NORMAL = "NORMAL";

        /** 早晚班 */
        public static final String EARLY_LATE = "EARLY_LATE";
    }

    private StatusConstants() {
        throw new UnsupportedOperationException("常量类不允许实例化");
    }
}
