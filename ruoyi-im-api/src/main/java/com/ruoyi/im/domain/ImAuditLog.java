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

    /** 操作用户名 */
    @TableField("user_name")
    private String userName;

    /** 模块名称 */
    @TableField("module")
    private String module;

    /** 操作类型 */
    @TableField("operation_type")
    private String operationType;

    /** 操作描述 */
    @TableField("description")
    private String description;

    /** 请求方法（GET POST PUT DELETE） */
    @TableField("request_method")
    private String requestMethod;

    /** 请求URL */
    @TableField("request_url")
    private String requestUrl;

    /** 请求参数 */
    @TableField("request_params")
    private String requestParams;

    /** 响应数据 */
    @TableField("response_data")
    private String responseData;

    /** 操作状态（0失败 1成功） */
    @TableField("status")
    private String status;

    /** 错误信息 */
    @TableField("error_msg")
    private String errorMsg;

    /** 执行时长（毫秒） */
    @TableField("execution_time")
    private Long executionTime;

    /** 客户端IP */
    @TableField("client_ip")
    private String clientIp;

    /** 用户代理 */
    @TableField("user_agent")
    private String userAgent;

    /** 操作时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("operation_time")
    private LocalDateTime operationTime;
}
