package com.ruoyi.web.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 用户快速操作请求DTO
 * 
 * @author ruoyi
 * @date 2025-01-21
 */
@Data
public class ImUserQuickOperationDTO {

    /**
     * 操作类型: enable-启用, disable-禁用, resetPassword-重置密码, 
     *         assignRole-分配角色, delete-删除, export-导出
     */
    @NotBlank(message = "操作类型不能为空")
    private String operation;

    /**
     * 用户ID列表
     */
    @NotNull(message = "用户ID列表不能为空")
    private List<Long> userIds;

    /**
     * 重置密码时使用的新密码
     */
    private String newPassword;

    /**
     * 管理员密码，用于敏感操作验证
     */
    private String adminPassword;

    /**
     * 分配角色时的角色ID列表
     */
    private List<Long> roleIds;

    /**
     * 操作备注
     */
    private String remark;

    /**
     * 是否发送通知（如重置密码后通知用户）
     */
    private Boolean sendNotification = true;

    /**
     * 生效时间（用于定时操作）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private String effectiveTime;

    /**
     * 操作来源: web-网页, api-接口, batch-批量任务
     */
    private String source = "web";

    /**
     * 客户端IP
     */
    private String clientIp;

    /**
     * 用户代理
     */
    private String userAgent;

    /**
     * 验证操作是否需要管理员密码
     */
    public boolean requiresAdminPassword() {
        return "delete".equals(operation) || "resetPassword".equals(operation);
    }

    /**
     * 验证操作参数
     */
    public boolean isValid() {
        if (userIds == null || userIds.isEmpty()) {
            return false;
        }
        
        switch (operation) {
            case "resetPassword":
                return newPassword != null && !newPassword.trim().isEmpty();
            case "assignRole":
                return roleIds != null && !roleIds.isEmpty();
            case "enable":
            case "disable":
            case "delete":
            case "export":
                return true;
            default:
                return false;
        }
    }
}