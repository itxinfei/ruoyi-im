package com.ruoyi.im.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 审计日志实体
 *
 * 对应数据库表 im_audit_log
 * 实际数据库字段：id, user_id, operation_type, target_type, target_id, operation_result, error_message, ip_address, user_agent, create_time
 *
 * @author ruoyi
 */
@TableName("im_audit_log")
@Data
public class ImAuditLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 日志ID，主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 操作用户ID */
    @TableField("user_id")
    private Long userId;

    /** 操作类型: LOGIN登录 LOGOUT登出 SEND_MESSAGE发送消息 DELETE_MESSAGE删除消息 CREATE_GROUP创建群组 JOIN_GROUP加入群组 LEAVE_GROUP退出群组 ADD_FRIEND添加好友 DELETE_FRIEND删除好友等 */
    @TableField("operation_type")
    private String operationType;

    /** 目标类型: USER用户 MESSAGE消息 GROUP群组 CONVERSATION会话 FRIEND好友 */
    @TableField("target_type")
    private String targetType;

    /** 目标ID */
    @TableField("target_id")
    private Long targetId;

    /** 操作结果: SUCCESS成功 FAILED失败 */
    @TableField("operation_result")
    private String operationResult;

    /** 错误信息 */
    @TableField("error_message")
    private String errorMessage;

    /** IP地址 */
    @TableField("ip_address")
    private String ipAddress;

    /** 用户代理 */
    @TableField("user_agent")
    private String userAgent;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("create_time")
    private LocalDateTime createTime;

    // ==================== 以下字段为非数据库字段 ====================

    /**
     * 操作用户名称（非数据库字段，关联查询时填充）
     */
    @TableField(exist = false)
    private String userName;
}
