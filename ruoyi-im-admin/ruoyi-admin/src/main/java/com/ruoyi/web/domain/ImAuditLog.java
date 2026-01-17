package com.ruoyi.web.domain;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 审计日志对象 im_audit_log
 *
 * @author ruoyi
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ImAuditLog extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 日志ID */
    private Long id;

    /** 操作用户ID */
    @Excel(name = "用户ID")
    private Long userId;

    /** 用户名（关联查询） */
    @Excel(name = "用户名")
    private String username;

    /** 昵称（关联查询） */
    @Excel(name = "昵称")
    private String nickname;

    /** 操作类型 */
    @Excel(name = "操作类型")
    private String operationType;

    /** 目标类型 */
    @Excel(name = "目标类型")
    private String targetType;

    /** 目标ID */
    @Excel(name = "目标ID")
    private Long targetId;

    /** 操作结果 */
    @Excel(name = "操作结果")
    private String operationResult;

    /** 错误信息 */
    @Excel(name = "错误信息")
    private String errorMessage;

    /** IP地址 */
    @Excel(name = "IP地址")
    private String ipAddress;

    /** 用户代理 */
    @Excel(name = "用户代理")
    private String userAgent;

}
