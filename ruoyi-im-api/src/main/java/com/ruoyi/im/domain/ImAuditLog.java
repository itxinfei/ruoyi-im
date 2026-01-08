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
 * 操作审计日志实体
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

    /** 操作类型（LOGIN登录 LOGOUT登出 SEND_MESSAGE发送消息 DELETE_MESSAGE撤回消息 CREATE_GROUP创建群组 JOIN_GROUP加入群组 LEAVE_GROUP退出群组 ADD_FRIEND添加好友 DELETE_FRIEND删除好友） */
    @TableField("operation_type")
    private String operationType;

    /** 目标类型（USER MESSAGE GROUP CONVERSATION FRIEND） */
    @TableField("target_type")
    private String targetType;

    /** 目标ID */
    @TableField("target_id")
    private Long targetId;

    /** 操作结果（SUCCESS成功 FAILED失败） */
    @TableField("operation_result")
    private String operationResult;

    /** 错误信息 */
    @TableField("error_message")
    private String errorMessage;

    /** 客户端IP */
    @TableField("ip_address")
    private String ipAddress;

    /** 用户代理 */
    @TableField("user_agent")
    private String userAgent;

    /** 操作时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
