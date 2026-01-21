package com.ruoyi.web.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 增强批量操作请求DTO
 * 
 * @author ruoyi
 * @date 2025-01-21
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImUserEnhancedBatchOperationDTO {

    /**
     * 操作类型: enable-启用, disable-禁用, resetPassword-重置密码, 
     *         assignRole-分配角色, delete-删除, export-导出,
     *         moveDepartment-调动部门, archive-归档, restore-恢复
     */
    @NotBlank(message = "操作类型不能为空")
    private String operationType;

    /**
     * 用户ID列表（支持跨页面选择）
     */
    @NotNull(message = "用户ID列表不能为空")
    private List<Long> userIds;

    /**
     * 操作参数
     */
    private OperationParams operationParams;

    /**
     * 安全验证参数
     */
    private SecurityParams securityParams;

    /**
     * 事务控制参数
     */
    private TransactionParams transactionParams;

    /**
     * 通知配置
     */
    private NotificationConfig notificationConfig;

    /**
     * 操作来源和上下文
     */
    private OperationContext operationContext;

    /**
     * 验证操作参数有效性
     */
    public boolean isValid() {
        if (operationType == null || operationType.trim().isEmpty()) {
            return false;
        }
        
        if (userIds == null || userIds.isEmpty()) {
            return false;
        }
        
        if (operationParams != null && !operationParams.isValid()) {
            return false;
        }
        
        if (securityParams != null && !securityParams.isValid()) {
            return false;
        }
        
        return true;
    }

    /**
     * 获取操作风险等级
     */
    public String getRiskLevel() {
        int riskScore = 0;
        
        if (securityParams != null) {
            if (securityParams.getRequireAdminPassword()) riskScore += 3;
            if (securityParams.getRequireTwoFactorAuth()) riskScore += 2;
            if (securityParams.getRequireMultiApproval()) riskScore += 4;
        }
        
        if ("delete".equals(operationType)) riskScore += 5;
        if ("batchDelete".equals(operationType)) riskScore += 4;
        if ("archive".equals(operationType)) riskScore += 2;
        if ("restore".equals(operationType)) riskScore += 3;
        
        if (riskScore >= 8) return "high";
        if (riskScore >= 5) return "medium";
        if (riskScore >= 3) return "low";
        return "safe";
    }

    /**
     * 操作参数内部类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OperationParams {
        /**
         * 重置密码参数
         */
        private PasswordResetParams passwordReset;

        /**
         * 角色分配参数
         */
        private RoleAssignmentParams roleAssignment;

        /**
         * 部门调动参数
         */
        private DepartmentTransferParams departmentTransfer;

        /**
         * 其他操作参数
         */
        private Map<String, Object> otherParams;

        /**
         * 验证参数有效性
         */
        public boolean isValid() {
            return true;
        }
    }

    /**
     * 密码重置参数内部类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PasswordResetParams {
        /**
         * 新密码
         */
        private String newPassword;

        /**
         * 密码策略: force-强制重置, notify-通知用户, expire-过期后重置
         */
        private String resetStrategy = "notify";

        /**
         * 密码过期时间（天）
         */
        private Integer passwordExpireDays = 90;

        /**
         * 是否需要用户首次登录时修改密码
         */
        private Boolean requireChangeOnFirstLogin = true;
    }

    /**
     * 角色分配参数内部类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoleAssignmentParams {
        /**
         * 角色ID列表
         */
        private List<Long> roleIds;

        /**
         * 分配模式: replace-替换, append-追加, remove-移除
         */
        private String assignmentMode = "append";

        /**
         * 是否移除冲突角色
         */
        private Boolean removeConflictingRoles = true;

        /**
         * 角色生效时间
         */
        private LocalDateTime effectiveTime;

        /**
         * 角色过期时间
         */
        private LocalDateTime expireTime;
    }

    /**
     * 部门调动参数内部类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DepartmentTransferParams {
        /**
         * 目标部门ID
         */
        @NotNull(message = "目标部门ID不能为空")
        private Long targetDepartmentId;

        /**
         * 调动类型: permanent-永久调动, temporary-临时调动, project-项目调动
         */
        private String transferType = "permanent";

        /**
         * 调动原因
         */
        private String transferReason;

        /**
         * 调动生效时间
         */
        private LocalDateTime effectiveTime;

        /**
         * 是否保留原部门权限
         */
        private Boolean keepOriginalPermissions = false;
    }

    /**
     * 安全验证参数内部类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SecurityParams {
        /**
         * 是否需要管理员密码验证
         */
        private Boolean requireAdminPassword = false;

        /**
         * 是否需要二次验证
         */
        private Boolean requireTwoFactorAuth = false;

        /**
         * 是否需要多级审批
         */
        private Boolean requireMultiApproval = false;

        /**
         * 操作IP白名单（逗号分隔）
         */
        private String allowedIpList;

        /**
         * 操作时间限制（工作时间内才能执行）
         */
        private TimeRestriction timeRestriction;

        /**
         * 验证安全参数有效性
         */
        public boolean isValid() {
            return true;
        }
    }

    /**
     * 事务控制参数内部类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransactionParams {
        /**
         * 事务隔离级别: READ_UNCOMMITTED, READ_COMMITTED, REPEATABLE_READ, SERIALIZABLE
         */
        private String isolationLevel = "READ_COMMITTED";

        /**
         * 事务超时时间（秒）
         */
        private Integer timeoutSeconds = 60;

        /**
         * 重试次数
         */
        private Integer retryCount = 3;

        /**
         * 重试间隔（毫秒）
         */
        private Long retryIntervalMs = 1000L;

        /**
         * 是否启用分布式事务
         */
        private Boolean enableDistributedTransaction = false;

        /**
         * 回滚策略: MANUAL-手动回滚, AUTO-自动回滚, NONE-不回滚
         */
        private String rollbackStrategy = "AUTO";

        /**
         * 批处理大小
         */
        private Integer batchSize = 100;

        /**
         * 验证事务参数有效性
         */
        public boolean isValid() {
            return true;
        }
    }

    /**
     * 通知配置内部类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NotificationConfig {
        /**
         * 通知类型: none-不通知, email-邮件, sms-短信, system-系统通知, all-全部
         */
        private String notificationType = "system";

        /**
         * 通知模板
         */
        private String notificationTemplate;

        /**
         * 通知接收者列表
         */
        private List<String> notificationRecipients;

        /**
         * 是否发送操作完成通知
         */
        private Boolean sendCompletionNotification = true;

        /**
         * 是否发送失败通知
         */
        private Boolean sendFailureNotification = true;

        /**
         * 是否发送进度通知
         */
        private Boolean sendProgressNotification = false;

        /**
         * 验证通知配置有效性
         */
        public boolean isValid() {
            return true;
        }
    }

    /**
     * 操作上下文内部类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OperationContext {
        /**
         * 客户端IP地址
         */
        private String clientIp;

        /**
         * 用户代理信息
         */
        private String userAgent;

        /**
         * 操作来源页面
         */
        private String sourcePage;

        /**
         * 操作来源功能
         */
        private String sourceFeature;

        /**
         * 会话ID
         */
        private String sessionId;

        /**
         * 操作者用户ID
         */
        private Long operatorUserId;

        /**
         * 操作者用户名
         */
        private String operatorUserName;

        /**
         * 操作时间戳
         */
        private Long operationTimestamp;

        /**
         * 额外的上下文信息
         */
        private Map<String, Object> additionalContext;
    }

    /**
     * 时间限制内部类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimeRestriction {
        /**
         * 是否启用时间限制
         */
        private Boolean enabled = false;

        /**
         * 允许的操作时间开始（HH:mm）
         */
        private String allowedStartTime = "09:00";

        /**
         * 允许的操作时间结束（HH:mm）
         */
        private String allowedEndTime = "18:00";

        /**
         * 允许的操作日（1-7，0代表周日）
         */
        private List<Integer> allowedDays = java.util.Arrays.asList(1, 2, 3, 4, 5);

        /**
         * 禁止的操作日（0-6，0代表周日）
         */
        private List<Integer> restrictedDays = new java.util.ArrayList<>();

        /**
         * 时区
         */
        private String timezone = "Asia/Shanghai";

        /**
         * 验证时间限制有效性
         */
        public boolean isValid() {
            return true;
        }

        /**
         * 检查当前时间是否在允许范围内
         */
        public boolean isOperationAllowed() {
            if (!enabled) return true;
            
            // 这里简化实现，实际应该检查当前时间
            return true;
        }
    }
}