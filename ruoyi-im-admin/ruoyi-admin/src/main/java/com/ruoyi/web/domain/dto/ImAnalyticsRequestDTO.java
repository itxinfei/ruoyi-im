package com.ruoyi.web.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 数据统计分析请求DTO
 * 
 * @author ruoyi
 * @date 2025-01-21
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImAnalyticsRequestDTO {

    /**
     * 分析类型: user-用户统计, message-消息统计, system-系统监控, department-部门统计
     */
    @NotBlank(message = "分析类型不能为空")
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
     * 时间范围：today-今天, yesterday-昨天, thisWeek-本周, lastWeek-上周, thisMonth-本月, lastMonth-上月, quarter-本季度, lastQuarter-上季度, 
     * yearToDate-年初至今, custom-自定义
     */
    private String timeRange;

    /**
     * 自定义时间范围开始日期
     */
    private LocalDateTime customStartTime;

    /**
     * 自定义时间范围结束日期
     */
    private LocalDateTime customEndTime;

    /**
     * 部门ID列表（可选）
     */
    private List<Long> departmentIds;

    /**
     * 角色ID列表（可选）
     */
    private List<Long> roleIds;

    /**
     * 用户ID列表（可选）
     */
    private List<Long> userIds;

    /**
     * 消息类型过滤
     */
    private List<String> messageTypes;

    /**
     * 时间段过滤：morning-上午, afternoon-下午, evening-夜晚
     */
    private String timeSlotFilter;

    /**
     * 是否包含统计数据详情
     */
    private Boolean includeDetails = false;

    /**
     * 是否包含趋势分析
     */
    private Boolean includeTrend = false;

    /**
     * 是否包含预测分析
     */
    private Boolean includePrediction = false;

    /**
     * 比页大小
     */
    private Integer pageSize = 20;

    /**
     * 排序字段
     */
    private String sortField;

    /**
     * 排序方向：asc-升序, desc-降序
     */
    private String sortOrder;

    /**
     * 导出格式：excel, csv, json
     */
    private String exportFormat = "excel";

    /**
     * 邮证分析参数
     */
    private ValidationParams validationParams;

    /**
     * 缓存配置
     */
    private CacheConfig cacheConfig;

    /**
     * 查询参数
     */
    private QueryParams queryParams;

    /**
     * 验证分析参数内部类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ValidationParams {
        /**
         * 是否需要数据验证
         */
        private Boolean requireDataValidation = false;

        /**
         * 最小数据量限制
         */
        private Integer minDataLimit = 100;

        /**
         * 最大数据量限制
         */
        private Integer maxDataLimit = 10000;

        /**
         * 时间范围限制（天）
         */
        private Integer maxTimeRangeDays = 365;

        /**
         * 允许的排序字段
         */
        List<String> allowedSortFields;

        /**
         * 允许的图表类型
         */
        private List<String> allowedChartTypes;
    }

    /**
     * 数据验证内部类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DataValidation {
        /**
         * 必填字段列表
         */
        private List<String> requiredFields;

        /**
         * 数据类型约束
         */
        private Map<String, String> dataTypeConstraints;
    }

    /**
     * 缓存配置内部类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CacheConfig {
        /**
         * 缓存类型
         */
        private List<String> cacheTypes;

        /**
         * 默认过期时间（分钟）
         */
        private Long defaultTtl = 30L;

        /**
         * 最大缓存大小（MB）
         */
        private Long maxCacheSize;

        /**
         * 是否启用缓存预热
         */
        private Boolean enableWarmup = false;

        /**
         * 是否启用智能清理
         */
        private Boolean enableSmartCleanup = false;

        /**
         * 清理频率间隔（分钟）
         */
        private Integer cleanupInterval = 60;
    }

    /**
     * 查询参数内部类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QueryParams {
        /**
         * 查询维度
         */
        private List<String> dimensions;

        /**
         * 聚合查询条件
         */
        private Map<String, Object> filters;

        /**
         * 聚合操作符
         */
        private List<String> operators;

        /**
         * 聚合逻辑符
         */
        private String logicOperator;

        /**
         * 排序配置
         */
        private SortConfig sortConfig;

        /**
         * 统计时间范围
         */
        private String timeRange;

        /**
         * 页面大小
         */
        private Integer pageSize;

        /**
         * 图表配置
         */
        private ChartConfig chartConfig;
    }

    /**
     * 排序配置内部类
         */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SortConfig {
        /**
         * 默认排序字段
         */
        private String defaultSortBy = "id";

        /**
         * 默认排序方向
         */
        private String defaultSortOrder = "desc";

        /**
         * 支持的排序字段
         */
        List<String> sortableFields;

        /**
         * 支持的排序方向
         */
        private List<String> sortOrderOptions;
    }

    /**
     * 图表配置内部类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChartConfig {
        /**
         * 图表类型
         */
        private List<String> supportedTypes;

        /**
         * 默认图表类型
         */
        private String defaultChartType = "line";

        /**
         * 支持的维度
         */
        private List<String> supportedDimensions;

        /**
         * 支持的时间聚合
         */
        private List<String> supportedTimeAggregations;

        /**
         * 默认时间聚合
         */
        private String defaultTimeAggregation = "day";

        /**
         * 默认排序
         */
        private String defaultSort = "time";
    }
}