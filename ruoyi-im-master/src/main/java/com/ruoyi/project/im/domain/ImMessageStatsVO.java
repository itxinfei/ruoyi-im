package com.ruoyi.web.domain;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 消息统计VO
 * 
 * @author ruoyi
 * @date 2025-01-21
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImMessageStatsVO {

    /**
     * 统计时间范围
     */
    @NotBlank(message = "时间范围不能为空")
    private String timeRange;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 总消息数
     */
    private Long totalMessages;

    /**
     * 发送消息数
     */
    private Integer sentMessages;

    /**
     * 接收消息数
     */
    private Integer receivedMessages;

    /**
     * 文本消息数
     */
    private Integer textMessages;

    /**
     * 图片消息数
     */
    private Integer imageMessages;

    /**
     * 文件消息数
     */
    private Integer fileMessages;

    /**
     * 语音消息数
     */
    private Integer voiceMessages;

    /**
     * 视频消息数
     */
    private Integer videoMessages;

    /**
     * 系统消息数
     */
    private Integer systemMessages;

    /**
     * 平均每日消息数
     */
    private Double avgDailyMessages;

    /**
     * 消息类型分布
     */
    private Map<String, Integer> messageTypeDistribution;

    /**
     * 消息发送者分布
     */
    private Map<String, Integer> senderDistribution;

    /**
     * 消息接收者分布
     */
    private Map<String, Integer> receiverDistribution;

    /**
     * 时段消息量分布
     */
    private Map<String, Integer> timeSlotMessageCount;

    /**
     * 每周消息量趋势
     */
    private MessageVolumeTrend weeklyTrend;

    /**
     * 活跃会话数
     */
    private Integer activeConversations;

    /**
     * 今日消息数
     */
    private Integer todayMessages;

    /**
     * 本周消息数
     */
    private Integer weekMessages;

    /**
     * 本月消息数
     */
    private Integer monthMessages;

    /**
     * 附件使用统计
     */
    private AttachmentUsageStats attachmentUsage;

    /**
     * 消息审核统计
     */
    private MessageAuditStats auditStats;

    /**
     * 热门消息量统计
     */
    private Map<String, Integer> departmentMessageStats;

    /**
     * 消息时长统计
     */
    private MessageDurationStats durationStats;

    /**
     * 统计时间
     */
    private LocalDateTime statisticsTime;

    /**
     * 获取平均消息量增长率
     */
    public Double getAverageGrowthRate() {
        if (sentMessages == null || sentMessages == 0) {
            return 0.0;
        }
        return (double) (sentMessages - receivedMessages) / sentMessages * 100;
    }

    /**
     * 获取消息量日增长率
     */
    public Double getDailyGrowthRate() {
        if (sentMessages == null || sentMessages == 0) {
            return 0.0;
        }
        return avgDailyMessages / 30; // 30天的平均值
    }

    /**
     * 消息类型分布内部类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MessageDistribution {
        /**
         * 消息类型分布
         */
        @JSONField(name = "message_type")
        private Map<String, Integer> typeDistribution;

        /**
         * 消息状态分布
         */
        @JSONField(name = "message_status")
        private Map<String, Integer> statusDistribution;

        /**
         * 消息大小分布
         */
        @JSONField(name = "message_size")
        private Map<String, Integer> sizeDistribution;

        /**
         * 时段分布
         */
        @JSONField(name = "time_slot")
        private Map<String, Integer> timeSlotDistribution;
    }

    /**
     * 发送者/接收者分布内部类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SenderReceiverDistribution {
        /**
         * 发送者分布
         */
        @JSONField(name = "senders")
        private Map<String, Integer> senderStats;

        /**
         * 接收者分布
         */
        @JSONField(name = "receivers")
        private Map<String, Integer> receiverStats;

        /**
         * 群组消息统计
         */
        @JSONField(name = "group_messages")
        private Map<String, Integer> groupMessageStats;

        /**
         * 私聊消息统计
         */
        @JSONField(name = "private_messages")
        private Map<String, Integer> privateMessageStats;
    }

    /**
     * 附件使用统计内部类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AttachmentUsageStats {
        /**
         * 附件类型分布
         */
        @JSONField(name = "attachment_types")
        private Map<String, Integer> typeDistribution;

        /**
         * 附件大小分布
         */
        @JSONField(name = "attachment_sizes")
        private Map<String, Integer> sizeDistribution;

        /**
         * 附件下载次数
         */
        private Integer totalDownloads;

        /**
         * 附件总大小
         */
        private Long totalSize;

        /**
         * 平均附件大小
         */
        private Double avgSize;

        /**
         * 热门附件统计
         */
        private Map<String, Integer> departmentAttachmentStats;
    }

    /**
     * 消息审核统计内部类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MessageAuditStats {
        /**
         * 总审核消息数
         */
        private Integer totalAuditedMessages;

        /**
         * 违规消息数
         */
        private Integer violationMessages;

        /**
         * 违规类型分布
         */
        private Map<String, Integer> violationTypeDistribution;

        /**
         * 审核通过率
         */
        private Double approvalRate;

        /**
         * 自动拦截率
         */
        private Double autoBlockRate;

        /**
         * 飀查结果
         */
        private Map<String, Integer> checkResults;

        /**
         * 最近审核时间
         */
        private LocalDateTime lastAuditTime;

        /**
         * 审核队列长度
         */
        private Integer auditQueueLength;
    }

    /**
     * 热门消息量统计内部类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DepartmentMessageStats {
        /**
         * 部门ID
         */
        private Long departmentId;

        /**
         * 部门名称
         */
        private String departmentName;

        /**
         * 发送消息数
         */
        private Integer sentMessages;

        /**
         * 接收消息数
         */
        private Integer receivedMessages;

        /**
         * 活跃会话数
         */
        private Integer activeConversations;

        /**
         * 消息量增长率
         */
        private Double messageGrowthRate;

        /**
         * 统计时间
         */
        private LocalDateTime statisticsTime;
    }

    /**
     * 消息时长统计内部类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MessageDurationStats {
        /**
         * 平均响应时间（秒）
         */
        private Double avgResponseTime;

        /**
         * 最长响应时间
         */
        private Long maxResponseTime;

        /**
         * 最短响应时间
         */
        private Long minResponseTime;

        /**
         * 90分位数响应时间
         */
        private Long percentile90;

        /**
         * 95分位数响应时间
         */
        private Long percentile95;

        /**
         * 响应时间分布
         */
        private Map<String, Integer> responseTimeDistribution;

        /**
         * 处理时长分布
         */
        private Map<String, Integer> processingTimeDistribution;

        /**
         * 峰段响应时间分布
         */
        private Map<String, Long> timeSlotAvgResponseTime;

        /**
         * 统计时间
         */
        private LocalDateTime statisticsTime;
    }

    /**
     * 每周消息量趋势内部类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MessageVolumeTrend {
        /**
         * 日期列表
         */
        private java.util.List<String> dates;

        /**
         * 消息量列表
         */
        private java.util.List<Long> messageCounts;

        /**
         * 发送消息量列表
         */
        private java.util.List<Long> sentMessageCounts;

        /**
         * 接收消息量列表
         */
        private java.util.List<Long> receivedMessageCounts;

        /**
         * 增长率列表
         */
        private java.util.List<Double> growthRates;
    }
}