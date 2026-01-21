package com.ruoyi.web.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 用户生命周期管理结果VO
 * 
 * @author ruoyi
 * @date 2025-01-21
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImUserLifecycleResultVO {

    /**
     * 操作ID
     */
    private String operationId;

    /**
     * 操作类型
     */
    private String operationType;

    /**
     * 操作描述
     */
    private String operationDescription;

    /**
     * 操作状态: pending-待处理, processing-处理中, completed-已完成, failed-失败, partial-部分成功
     */
    private String status;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 操作人
     */
    private String operator;

    /**
     * 处理的总用户数
     */
    private Integer totalCount;

    /**
     * 成功处理的用户数
     */
    private Integer successCount;

    /**
     * 失败处理的用户数
     */
    private Integer failedCount;

    /**
     * 跳过的用户数
     */
    private Integer skippedCount;

    /**
     * 成功处理的用户ID列表
     */
    private List<Long> successUserIds;

    /**
     * 失败处理的用户ID列表
     */
    private List<Long> failedUserIds;

    /**
     * 跳过的用户ID列表
     */
    private List<Long> skippedUserIds;

    /**
     * 错误详情，用户ID -> 错误信息
     */
    private Map<Long, String> errorDetails;

    /**
     * 操作结果详情
     */
    private Map<String, Object> resultDetails;

    /**
     * 处理进度（百分比）
     */
    private Double progressPercentage;

    /**
     * 操作耗时（毫秒）
     */
    private Long processingTime;

    /**
     * 建议或后续操作
     */
    private List<String> suggestions;

    /**
     * 是否可以重试失败的操作
     */
    private Boolean retryable;

    /**
     * 重试建议
     */
    private String retrySuggestion;

    /**
     * 是否为异步操作
     */
    private Boolean isAsync = false;

    /**
     * 异步操作ID（用于查询异步操作状态）
     */
    private String asyncOperationId;

    /**
     * 操作日志ID列表
     */
    private List<String> operationLogIds;

    /**
     * 获取成功率
     */
    public double getSuccessRate() {
        if (totalCount == null || totalCount == 0) {
            return 0.0;
        }
        if (successCount == null) {
            return 0.0;
        }
        return (double) successCount / totalCount * 100;
    }

    /**
     * 判断是否完成
     */
    public boolean isCompleted() {
        return "completed".equals(status);
    }

    /**
     * 判断是否失败
     */
    public boolean isFailed() {
        return "failed".equals(status);
    }

    /**
     * 判断是否部分成功
     */
    public boolean isPartialSuccess() {
        return "partial".equals(status);
    }

    /**
     * 判断是否正在处理
     */
    public boolean isProcessing() {
        return "processing".equals(status);
    }

    /**
     * 获取操作状态文本
     */
    public String getStatusText() {
        switch (status) {
            case "pending":
                return "待处理";
            case "processing":
                return "处理中";
            case "completed":
                return "已完成";
            case "failed":
                return "失败";
            case "partial":
                return "部分成功";
            default:
                return "未知状态";
        }
    }

    /**
     * 获取进度百分比文本
     */
    public String getProgressText() {
        if (progressPercentage == null) {
            return "0%";
        }
        return String.format("%.1f%%", progressPercentage);
    }

    /**
     * 获取操作摘要
     */
    public String getOperationSummary() {
        if (isCompleted()) {
            return String.format("%s 完成 - 成功处理 %d 个用户，失败 %d 个，耗时 %d ms", 
                operationDescription, successCount, failedCount, processingTime);
        } else if (isFailed()) {
            return String.format("%s 失败 - 成功处理 %d 个用户，失败 %d 个，耗时 %d ms", 
                operationDescription, successCount, failedCount, processingTime);
        } else if (isPartialSuccess()) {
            return String.format("%s 部分完成 - 成功处理 %d 个用户，失败 %d 个，耗时 %d ms", 
                operationDescription, successCount, failedCount, processingTime);
        } else {
            return String.format("%s 处理中 - 当前进度 %s，耗时 %d ms", 
                operationDescription, getProgressText(), processingTime);
        }
    }
}