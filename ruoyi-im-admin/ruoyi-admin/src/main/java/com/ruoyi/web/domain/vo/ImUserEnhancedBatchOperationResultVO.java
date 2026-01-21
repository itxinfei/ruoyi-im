package com.ruoyi.web.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 增强批量操作结果VO
 * 
 * @author ruoyi
 * @date 2025-01-21
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImUserEnhancedBatchOperationResultVO {

    /**
     * 操作ID
     */
    private String operationId;

    /**
     * 操作类型
     */
    private String operationType;

    /**
     * 操作状态: pending-待处理, processing-处理中, completed-已完成, failed-失败, 
     *           partial-部分成功, cancelled-已取消, rolled_back-已回滚
     */
    private String status;

    /**
     * 操作描述
     */
    private String operationDescription;

    /**
     * 风险等级: safe-安全, low-低风险, medium-中等风险, high-高风险
     */
    private String riskLevel;

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
     * 总处理数量
     */
    private Integer totalCount;

    /**
     * 成功处理数量
     */
    private Integer successCount;

    /**
     * 失败处理数量
     */
    private Integer failedCount;

    /**
     * 跳过处理数量
     */
    private Integer skippedCount;

    /**
     * 重试次数
     */
    private Integer retryCount;

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
     * 警告信息列表
     */
    private List<WarningInfo> warnings;

    /**
     * 操作结果详情
     */
    private Map<String, Object> resultDetails;

    /**
     * 操作耗时（毫秒）
     */
    private Long processingTime;

    /**
     * 事务ID（如果有事务的话）
     */
    private String transactionId;

    /**
     * 是否可以撤销
     */
    private Boolean cancellable = false;

    /**
     * 撤销截止时间
     */
    private LocalDateTime cancelDeadline;

    /**
     * 进度百分比
     */
    private Double progressPercentage;

    /**
     * 操作建议
     */
    private List<String> suggestions;

    /**
     * 是否支持重试
     */
    private Boolean retryable = false;

    /**
     * 重试建议
     */
    private String retrySuggestion;

    /**
     * 安全验证结果
     */
    private SecurityVerificationResult securityVerification;

    /**
     * 事务状态
     */
    private TransactionStatus transactionStatus;

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
            case "cancelled":
                return "已取消";
            case "rolled_back":
                return "已回滚";
            default:
                return "未知状态";
        }
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
     * 安全验证结果内部类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SecurityVerificationResult {
        /**
         * 密码验证结果
         */
        private Boolean adminPasswordVerified = false;

        /**
         * IP验证结果
         */
        private Boolean ipVerified = false;

        /**
         * 时间限制验证结果
         */
        private Boolean timeRestrictionVerified = false;

        /**
         * 权限验证结果
         */
        private Boolean permissionVerified = false;

        /**
         * 二次验证结果
         */
        private Boolean twoFactorVerified = false;

        /**
         * 验证失败原因
         */
        private String verificationFailureReason;

        /**
         * 安全验证总分
         */
        private Integer securityScore = 0;

        /**
         * 验证时间戳
         */
        private LocalDateTime verificationTime;

        /**
         * 获取验证状态文本
         */
        public String getVerificationStatusText() {
            int passedCount = 0;
            if (adminPasswordVerified != null && adminPasswordVerified) passedCount++;
            if (ipVerified != null && ipVerified) passedCount++;
            if (timeRestrictionVerified != null && timeRestrictionVerified) passedCount++;
            if (permissionVerified != null && permissionVerified) passedCount++;
            if (twoFactorVerified != null && twoFactorVerified) passedCount++;
            
            if (passedCount == 4) return "验证通过";
            if (passedCount >= 3) return "基本通过";
            if (passedCount >= 1) return "部分通过";
            return "验证失败";
        }
    }

    /**
     * 事务状态内部类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransactionStatus {
        /**
         * 事务ID
         */
        private String transactionId;

        /**
         * 事务状态: active-活跃, committed-已提交, rolled_back-已回滚, failed-失败
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
         * 事务涉及的表
         */
        private List<String> affectedTables;

        /**
         * 事务隔离级别
         */
        private String isolationLevel;

        /**
         * 提交的记录数
         */
        private Integer committedRecordCount;

        /**
         * 回滚的记录数
         */
        private Integer rolledBackRecordCount;

        /**
         * 事务超时时间（秒）
         */
        private Integer timeoutSeconds;

        /**
         * 获取事务状态文本
         */
        public String getStatusText() {
            switch (status) {
                case "active":
                    return "活跃";
                case "committed":
                    return "已提交";
                case "rolled_back":
                    return "已回滚";
                case "failed":
                    return "失败";
                default:
                    return "未知";
            }
        }
    }

    /**
     * 警告信息内部类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WarningInfo {
        /**
         * 警告类型: security-安全风险, business-业务风险, performance-性能风险, data-数据风险
         */
        private String warningType;

        /**
         * 警告级别: info-信息, warning-警告, error-错误, critical-严重
         */
        private String warningLevel;

        /**
         * 警告消息
         */
        private String message;

        /**
         * 建议处理方式
         */
        private String suggestedAction;

        /**
         * 相关用户ID列表
         */
        private List<Long> relatedUserIds;

        /**
         * 警告时间
         */
        private LocalDateTime warningTime;

        /**
         * 获取警告类型文本
         */
        public String getWarningTypeText() {
            switch (warningType) {
                case "security":
                    return "安全风险";
                case "business":
                    return "业务风险";
                case "performance":
                    return "性能风险";
                case "data":
                    return "数据风险";
                default:
                    return "未知";
            }
        }
    }
}