package com.ruoyi.im.constants;

/**
 * 会议室状态常量
 *
 * @author ruoyi
 */
public class MeetingRoomConstants {

    /**
     * 会议室状态
     */
    public static class Status {
        /** 可用 */
        public static final String AVAILABLE = "AVAILABLE";

        /** 维护中 */
        public static final String MAINTENANCE = "MAINTENANCE";

        /** 已停用 */
        public static final String DISABLED = "DISABLED";
    }

    /**
     * 预订状态
     */
    public static class BookingStatus {
        /** 待确认 */
        public static final String PENDING = "PENDING";

        /** 已确认 */
        public static final String CONFIRMED = "CONFIRMED";

        /** 已取消 */
        public static final String CANCELLED = "CANCELLED";

        /** 已完成 */
        public static final String COMPLETED = "COMPLETED";
    }

    /**
     * 会议类型
     */
    public static class MeetingType {
        /** 常规会议 */
        public static final String REGULAR = "REGULAR";

        /** 培训 */
        public static final String TRAINING = "TRAINING";

        /** 面试 */
        public static final String INTERVIEW = "INTERVIEW";

        /** 客户会议 */
        public static final String CLIENT = "CLIENT";
    }

    private MeetingRoomConstants() {
        throw new UnsupportedOperationException("常量类不允许实例化");
    }
}
