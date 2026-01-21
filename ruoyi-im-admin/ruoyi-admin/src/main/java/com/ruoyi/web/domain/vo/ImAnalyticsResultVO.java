package com.ruoyi.web.domain.vo;

import com.ruoyi.web.domain.ImSystemMonitorVO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 数据分析结果VO
 *
 * @author ruoyi
 * @date 2025-01-21
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImAnalyticsResultVO {

    /**
     * 分析类型
     */
    private String analysisType;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 时间范围
     */
    private String timeRange;

    /**
     * 统计时间
     */
    private LocalDateTime statisticsTime;

    /**
     * 用户活跃度统计
     */
    private Map<String, Object> userActivityStats;

    /**
     * 消息统计
     */
    private Map<String, Object> messageStats;

    /**
     * 系统监控
     */
    private ImSystemMonitorVO systemMonitor;

    /**
     * 部门统计
     */
    private Map<String, Object> departmentStats;

    /**
     * 告警信息
     */
    private List<Map<String, Object>> alerts;

    /**
     * 分析结果元数据
     */
    private Map<String, Object> metadata;

    /**
     * 性能指标概览
     */
    private Map<String, Object> performanceOverview;

    /**
     * 统计建议
     */
    private List<String> recommendations;

    /**
     * 是否可以使用缓存优化
     */
    private Boolean canOptimize;

    /**
     * 缓存命中率提升空间
     */
    private Map<String, Double> optimizationOpportunities;

    /**
     * 趋分提示
     */
    private List<String> improvementSuggestions;

    /**
     * 统计图表数据
     */
    private Map<String, Object> chartData;

    /**
     * 报表数据缓存键
     */
    private Map<String, String> chartDataCache;

    /**
     * 生成时间
     */
    private LocalDateTime generatedTime;

    // ==================== 网络性能内部类 ====================
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NetworkPerformance {
        private Integer activeConnections;
        private Integer totalConnections;
        private Double currentBandwidth;
        private Double avgResponseTime;
        private Double packetLossRate;
        private Double latency;
        private LocalDateTime lastCheckTime;
    }

    // ==================== 缓存状态内部类 ====================
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CacheStatus {
        private Map<String, Object> cacheStatus;
        private List<String> activeCaches;
        private List<String> cachedItems;
        private Map<String, Object> healthStatus;
        private LocalDateTime lastCleanupTime;
        private LocalDateTime nextCleanupTime;
        private String cleanupFrequency;
    }

    // ==================== 资源使用内部类 ====================
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResourceUsage {
        private Map<String, Object> cpuUsage;
        private Map<String, Object> memoryUsage;
        private Map<String, Object> jvmStats;
        private Map<String, Object> diskUsage;
        private Map<String, Object> networkStats;
    }

    // ==================== 服务状态内部类 ====================
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ServiceStatus {
        private Map<String, Object> serviceStatus;
        private Map<String, Object> healthCheck;
        private LocalDateTime lastCheckTime;
        private Boolean isHealthy;
    }

    // ==================== 任务统计内部类 ====================
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TaskStatistics {
        private Map<String, Object> taskExecutionDetails;
        private Map<String, Object> executionStats;
        private Map<String, Object> avgExecutionTime;
        private Map<String, Object> failureRateTrend;
        private Map<String, Object> retryStats;
        private List<String> commonFailureReasons;
        private Map<String, Object> failureAnalysis;
        private Map<String, Object> failureReasonTrend;
        private Map<String, String> successPaths;
        private Map<String, Object> failurePathTrend;
    }
}
