package com.ruoyi.im.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 操作审计日志实体
 *
 * 用于记录系统中的敏感操作，如用户登录、权限变更、数据删除等
 *
 * @author ruoyi
 */
@TableName("im_audit_log")
@Data
public class ImAuditLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 日志ID，主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 操作用户ID
     */
    private Long userId;

    /**
     * 操作用户名
     */
    private String userName;

    /**
     * 操作模块（LOGIN登录、MESSAGE消息、GROUP群组、USER用户、FILE文件、APPROVAL审批等）
     */
    private String module;

    /**
     * 操作类型（CREATE新增、UPDATE修改、DELETE删除、QUERY查询、EXPORT导出、IMPORT导入等）
     */
    private String operationType;

    /**
     * 操作描述
     */
    private String description;

    /**
     * 请求方法
     */
    private String requestMethod;

    /**
     * 请求URL
     */
    private String requestUrl;

    /**
     * 请求参数
     */
    private String requestParams;

    /**
     * 响应结果
     */
    private String responseData;

    /**
     * 操作状态（SUCCESS成功、FAILURE失败）
     */
    private String status;

    /**
     * 错误信息
     */
    private String errorMsg;

    /**
     * 执行时长（毫秒）
     */
    private Long executionTime;

    /**
     * 客户端IP
     */
    private String clientIp;

    /**
     * 用户代理
     */
    private String userAgent;

    /**
     * 操作时间
     */
    private LocalDateTime operationTime;
}
