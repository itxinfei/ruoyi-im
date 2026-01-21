package com.ruoyi.web.domain;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 系统监控VO
 *
 * @author ruoyi
 * @date 2025-01-21
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImSystemMonitorVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 监控时间范围
     */
    @NotBlank(message = "监控时间范围不能为空")
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
     * 监控时间
     */
    private LocalDateTime monitorTime;

    /**
     * 系统名称
     */
    private String systemName;

    /**
     * 系统状态
     */
    private String systemStatus;

    /**
     * 运行时间（秒）
     */
    private Long uptime;

    /**
     * 最后检查时间
     */
    private LocalDateTime lastCheckTime;

    /**
     * CPU使用率
     */
    private Double cpuUsage;

    /**
     * 内存使用率
     */
    private Double memoryUsage;

    /**
     * 磁盘使用率
     */
    private Double diskUsage;

    /**
     * 网络状态
     */
    private Map<String, Object> networkStatus;

    /**
     * 数据库状态
     */
    private Map<String, Object> databaseStatus;

    /**
     * 缓存状态
     */
    private Map<String, Object> cacheStatus;

    /**
     * 服务状态列表
     */
    private List<Map<String, Object>> serviceStatus;

    /**
     * 告警信息列表
     */
    private List<Map<String, Object>> alerts;

    /**
     * 性能指标
     */
    private Map<String, Object> performanceMetrics;

    /**
     * 资源使用
     */
    private Map<String, Object> resourceUsage;

    /**
     * 统计时间
     */
    private LocalDateTime statisticsTime;

    /**
     * 元数据
     */
    private Map<String, Object> metadata;
}
