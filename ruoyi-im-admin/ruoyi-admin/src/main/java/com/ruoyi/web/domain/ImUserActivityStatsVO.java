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
 * 用户活跃度统计VO
 * 
 * @author ruoyi
 * @date 2025-01-21
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImUserActivityStatsVO {

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
     * 总用户数
     */
    private Integer totalUsers;

    /**
     * 活跃用户数（有登录记录的用户数）
     */
    private Integer activeUsers;

    /**
     * 新注册用户数
     */
    private Integer newUsers;

    /**
     * 平均登录次数
     */
    private Double avgLoginCount;

    /**
     * 总登录次数
     */
    private Integer totalLoginCount;

    /**
     * 日均活跃用户数
     */
    private Double avgDailyActiveUsers;

    /**
     * 每周活跃用户数
     */
    private Double avgWeeklyActiveUsers;

    /**
     * 每月活跃用户数
     */
    private Double avgMonthlyActiveUsers;

    /**
     * 用户活跃度分布
     */
    private UserActivityDistribution activityDistribution;

    /**
     * 部门活跃度分布
     */
    private Map<String, DepartmentActivityStats> departmentStats;

    /**
     * 时段活跃度分布
     */
    private Map<String, TimeSlotActivityStats> timeSlotStats;

    /**
     * 活跃用户列表
     */
    private UserActivityDetail mostActiveUsers;

    /**
     * 用户增长趋势
     */
    private UserGrowthTrend userGrowthTrend;

    /**
     * 活跃度排行榜
     */
    private UserActivityRanking activityRanking;

    /**
     * 统计时间
     */
    private LocalDateTime statisticsTime;

    /**
     * 获取活跃度百分比
     */
    public Double getActiveRate() {
        if (totalUsers == null || totalUsers == 0) {
            return 0.0;
        }
        return (double) activeUsers / totalUsers * 100;
    }

    /**
     * 获取新增用户百分比
     */
    public Double getNewUserRate() {
        if (totalUsers == null || totalUsers == 0) {
            return 0.0;
        }
        return (double) newUsers / totalUsers * 100;
    }

    /**
     * 用户活跃度分布内部类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserActivityDistribution {
        /**
         * 活跃度等级分布
         */
        private Map<String, Integer> activityLevel;

        /**
         * 登录频率分布
         */
        private Map<String, Integer> loginFrequency;

        /**
         * 设备类型分布
         */
        private Map<String, Integer> deviceTypeDistribution;

        /**
         * 最后活跃时间分布
         */
        private Map<String, Integer> lastActiveTimeDistribution;
    }

    /**
     * 部门活跃度统计内部类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DepartmentActivityStats {
        /**
         * 部门名称
         */
        private String departmentName;

        /**
         * 部门ID
         */
        private Long departmentId;

        /**
         * 总用户数
         */
        private Integer totalUsers;

        /**
         * 活跃用户数
         */
        private Integer activeUsers;

        /**
         * 活跃率
         */
        private Double activeRate;

        /**
         * 日均登录次数
         */
        private Double avgDailyLoginCount;
        /**
         * 最近7天活跃用户数
         */
        private Integer recentActiveUsers;
    }

    /**
     * 时段活跃度统计内部类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimeSlotActivityStats {
        /**
         * 时段名称
         */
        private String timeSlot;

        /**
         * 活跃用户数
         */
        private Integer activeUsers;

        /**
         * 占总活跃用户数的百分比
         */
        private Double percentage;
    }

    /**
     * 用户活跃详情内部类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserActivityDetail {
        /**
         * 用户ID
         */
        private Long userId;

        /**
         * 用户名称
         */
        private String username;

        /**
         * 昵称
         */
        private String nickname;

        /**
         * 部门名称
         */
        private String departmentName;

        /**
         * 登录次数
         */
        private Integer loginCount;

        /**
         * 最后登录时间
         */
        private LocalDateTime lastLoginTime;

        /**
         * 活跃度评分
         */
        private Double activityScore;

        /**
         * 活跃度等级
         */
        private String activityLevel;

        /**
         * 在线时长（分钟）
         */
        private Long onlineDuration;

        /**
         * 消息发送数
         */
        private Integer messageCount;
    }

    /**
     * 用户增长趋势内部类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserGrowthTrend {
        /**
         * 日期列表
         */
        private java.util.List<String> dates;

        /**
         * 新增用户数列表
         */
        private java.util.List<Integer> newUsers;

        /**
         * 累计用户数列表
         */
        private java.util.List<Integer> totalUsers;

        /**
         * 增长率列表
         */
        private java.util.List<Double> growthRates;
    }

    /**
     * 用户活跃度排行榜内部类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserActivityRanking {
        /**
         * 活跃用户列表
         */
        private java.util.List<UserActivityDetail> topActiveUsers;

        /**
         * 活跃用户部门分布
         */
        private Map<String, Integer> departmentRanking;

        /**
         * 排行方式
         */
        private String rankingType;

        /**
         * 排行时间
         */
        private LocalDateTime rankingTime;
    }

    /**
     * 部门活跃度内部类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DepartmentActivityDetail {
        /**
         * 部门ID
         */
        private Long departmentId;

        /**
         * 部门名称
         */
        private String departmentName;

        /**
         * 活跃用户数
         */
        private Integer activeUsers;

        /**
         * 总用户数
         */
        private Integer totalUsers;

        /**
         * 活跃率
         */
        private Double activeRate;

        /**
         * 日均活跃用户数
         */
        private Double avgDailyActiveUsers;

        /**
         * 用户活跃度分布
         */
        private Map<String, Integer> activityLevelDistribution;

        /**
         * 最近的活跃用户ID列表
         */
        private java.util.List<Long> recentActiveUserIds;

        /**
         * 统计时间
         */
        private LocalDateTime statisticsTime;
    }
}