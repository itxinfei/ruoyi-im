package com.ruoyi.web.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 用户生命周期管理请求DTO
 * 
 * @author ruoyi
 * @date 2025-01-21
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImUserLifecycleDTO {

    /**
     * 操作类型: onboard-入职, departure-离职, transfer-调动, batchActivate-批量激活, batchDeactivate-批量停用
     */
    @NotBlank(message = "操作类型不能为空")
    private String operationType;

    /**
     * 用户列表（用于批量操作）
     */
    private List<Long> userIds;

    /**
     * 入职信息（仅用于onboard操作）
     */
    private OnboardInfo onboardInfo;

    /**
     * 离职信息（仅用于departure操作）
     */
    private DepartureInfo departureInfo;

    /**
     * 调动信息（仅用于transfer操作）
     */
    private TransferInfo transferInfo;

    /**
     * 生效时间（用于定时操作）
     */
    private LocalDateTime effectiveTime;

    /**
     * 操作备注
     */
    private String remark;

    /**
     * 操作人
     */
    private String operator;

    /**
     * 部门ID（用于批量操作时的部门归属）
     */
    private Long departmentId;

    /**
     * 角色ID列表（用于批量操作时的角色分配）
     */
    private List<Long> roleIds;

    /**
     * 是否发送通知
     */
    private Boolean sendNotification = true;

    /**
     * 验证操作是否有效
     */
    public boolean isValid() {
        if (operationType == null || operationType.trim().isEmpty()) {
            return false;
        }
        
        switch (operationType) {
            case "onboard":
                return onboardInfo != null && onboardInfo.isValid();
            case "departure":
                return departureInfo != null && departureInfo.isValid();
            case "transfer":
                return transferInfo != null && transferInfo.isValid();
            case "batchActivate":
            case "batchDeactivate":
                return userIds != null && !userIds.isEmpty();
            default:
                return false;
        }
    }

    /**
     * 获取操作描述
     */
    public String getOperationDescription() {
        switch (operationType) {
            case "onboard":
                return "用户入职";
            case "departure":
                return "用户离职";
            case "transfer":
                return "用户调动";
            case "batchActivate":
                return "批量激活用户";
            case "batchDeactivate":
                return "批量停用用户";
            default:
                return "未知操作";
        }
    }

    /**
     * 入职信息内部类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OnboardInfo {
        /**
         * 用户基本信息列表
         */
        @NotEmpty(message = "用户信息不能为空")
        private List<UserInfo> users;

        /**
         * 默认角色ID列表
         */
        private List<Long> defaultRoleIds;

        /**
         * 默认部门ID
         */
        private Long defaultDepartmentId;

        /**
         * 是否发送欢迎消息
         */
        private Boolean sendWelcomeMessage = true;

        /**
         * 是否发送入职邮件
         */
        private Boolean sendWelcomeEmail = true;

        /**
         * 密码策略: generated-系统生成, specified-指定密码, custom-自定义密码
         */
        private String passwordStrategy = "generated";

        /**
         * 自定义密码（当passwordStrategy为custom时使用）
         */
        private String customPassword;

        /**
         * 初始权限配置
         */
        private PermissionConfig permissionConfig;

        /**
         * 验证入职信息
         */
        public boolean isValid() {
            if (users == null || users.isEmpty()) {
                return false;
            }
            
            for (UserInfo user : users) {
                if (!user.isValid()) {
                    return false;
                }
            }
            
            return true;
        }
    }

    /**
     * 离职信息内部类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DepartureInfo {
        /**
         * 用户ID列表
         */
        @NotEmpty(message = "用户ID列表不能为空")
        private List<Long> userIds;

        /**
         * 离职类型: voluntary-主动离职, involuntary-被动离职, termination-解雇, retirement-退休
         */
        private String departureType = "voluntary";

        /**
         * 离职原因
         */
        private String departureReason;

        /**
         * 离职日期
         */
        private LocalDateTime departureDate;

        /**
         * 生效日期
         */
        private LocalDateTime effectiveDate;

        /**
         * 工作交接信息
         */
        private HandoverInfo handoverInfo;

        /**
         * 是否备份用户数据
         */
        private Boolean backupUserData = true;

        /**
         * 是否立即禁用账号
         */
        private Boolean disableImmediately = true;

        /**
         * 转交对象ID（用于工作交接）
         */
        private Long handoverToUserId;

        /**
         * 离职后处理选项
         */
        private DepartureOptions departureOptions;

        /**
         * 验证离职信息
         */
        public boolean isValid() {
            if (userIds == null || userIds.isEmpty()) {
                return false;
            }
            
            if (departureDate == null) {
                return false;
            }
            
            if ("involuntary".equals(departureType) || "termination".equals(departureType)) {
                if (departureReason == null || departureReason.trim().isEmpty()) {
                    return false;
                }
            }
            
            return true;
        }
    }

    /**
     * 调动信息内部类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransferInfo {
        /**
         * 用户ID
         */
        @NotNull(message = "用户ID不能为空")
        private Long userId;

        /**
         * 目标部门ID
         */
        @NotNull(message = "目标部门ID不能为空")
        private Long targetDepartmentId;

        /**
         * 目标角色ID列表
         */
        private List<Long> targetRoleIds;

        /**
         * 调动类型: department-部门调动, role-角色变更, location-工作地点变更
         */
        private String transferType = "department";

        /**
         * 调动原因
         */
        private String transferReason;

        /**
         * 调动生效日期
         */
        private LocalDateTime effectiveDate;

        /**
         * 调动说明
         */
        private String description;

        /**
         * 是否保留原权限
         */
        private Boolean keepOriginalPermissions = true;

        /**
         * 验证调动信息
         */
        public boolean isValid() {
            if (userId == null || targetDepartmentId == null) {
                return false;
            }
            
            return true;
        }
    }

    /**
     * 用户基本信息内部类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        /**
         * 用户名
         */
        @NotBlank(message = "用户名不能为空")
        private String username;

        /**
         * 昵称
         */
        private String nickname;

        /**
         * 邮箱
         */
        private String email;

        /**
         * 手机号
         */
        private String mobile;

        /**
         * 性别: 0-未知, 1-男, 2-女
         */
        private Integer gender;

        /**
         * 部门ID
         */
        private Long departmentId;

        /**
         * 职务
         */
        private String jobTitle;

        /**
         * 入职日期
         */
        private LocalDateTime onboardDate;

        /**
         * 密码
         */
        private String password;

        /**
         * 头像URL
         */
        private String avatar;

        /**
         * 备注
         */
        private String remark;

        /**
         * 验证用户信息
         */
        public boolean isValid() {
            if (username == null || username.trim().isEmpty()) {
                return false;
            }
            
            if (email != null && !email.matches("^[\\w.-]+@[\\w.-]+\\.[A-Za-z]{2,}$")) {
                return false;
            }
            
            return true;
        }
    }

    /**
     * 权限配置内部类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PermissionConfig {
        /**
         * 默认权限列表
         */
        private List<String> defaultPermissions;

        /**
         * 权限生效时间
         */
        private LocalDateTime effectiveTime;

        /**
         * 权限过期时间
         */
        private LocalDateTime expireTime;

        /**
         * 是否启用特殊权限
         */
        private Boolean enableSpecialPermissions = false;
    }

    /**
     * 工作交接信息内部类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HandoverInfo {
        /**
         * 交接说明
         */
        private String description;

        /**
         * 交接文档URL
         */
        private String documentUrl;

        /**
         * 交接联系人
         */
        private String handoverContact;

        /**
         * 交接完成时间
         */
        private LocalDateTime handoverTime;

        /**
         * 交接确认状态: pending-待确认, confirmed-已确认, completed-已完成
         */
        private String status = "pending";
    }

    /**
     * 离职后处理选项内部类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DepartureOptions {
        /**
         * 是否发送离职通知邮件
         */
        private Boolean sendDepartureEmail = true;

        /**
         * 是否清理系统访问权限
         */
        private Boolean clearSystemAccess = true;

        /**
         * 是否禁用相关账户
         */
        private Boolean disableRelatedAccounts = false;

        /**
         * 是否数据归档
         */
        private Boolean archiveData = true;

        /**
         * 是否通知相关人员
         */
        private Boolean notifyRelatedUsers = true;

        /**
         * 离职后保留期限（天数）
         */
        private Integer retentionDays = 90;
    }
}