package com.ruoyi.im.vo.audit;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 审计日志视图对象
 * 用于返回给前端的审计日志数据，与数据库实体 ImAuditLog 分离
 *
 * @author ruoyi
 */
@Data
public class ImAuditLogVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 日志ID */
    private Long id;

    /** 操作用户ID */
    private Long userId;

    /** 操作用户名称（关联查询） */
    private String userName;

    /** 操作用户头像（关联查询） */
    private String userAvatar;

    /** 操作类型 */
    private String operationType;

    /** 目标类型 */
    private String targetType;

    /** 目标ID */
    private Long targetId;

    /** 操作结果 */
    private String operationResult;

    /** 错误信息 */
    private String errorMessage;

    /** IP地址 */
    private String ipAddress;

    /** 用户代理 */
    private String userAgent;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
