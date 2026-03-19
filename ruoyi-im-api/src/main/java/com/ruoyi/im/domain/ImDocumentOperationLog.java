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
 * 文档操作日志实体
 * 记录文档的所有编辑操作
 *
 * @author ruoyi
 */
@TableName("im_document_operation_log")
@Data

public class ImDocumentOperationLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 日志ID
     */
    @TableId(type = IdType.AUTO)
    
    private Long id;

    /**
     * 文档ID
     */
    
    @TableField("document_id")
    private Long documentId;

    /**
     * 操作用户ID
     */
    
    @TableField("user_id")
    private Long userId;

    /**
     * 用户名称（冗余）
     */
    
    @TableField("user_name")
    private String userName;

    /**
     * 操作类型：INSERT插入, DELETE删除, UPDATE修改, FORMAT格式化
     */
    
    @TableField("operation_type")
    private String operationType;

    /**
     * 操作位置（偏移量）
     */
    
    @TableField("position")
    private Integer position;

    /**
     * 操作内容（被删除/插入的内容）
     */
    
    @TableField("content")
    private String content;

    /**
     * 内容长度
     */
    
    @TableField("content_length")
    private Integer contentLength;

    /**
     * 操作前的版本号
     */
    
    @TableField("before_version")
    private Integer beforeVersion;

    /**
     * 操作后的版本号
     */
    
    @TableField("after_version")
    private Integer afterVersion;

    /**
     * 变更摘要
     */
    
    @TableField("change_summary")
    private String changeSummary;

    /**
     * 操作时间
     */
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("operation_time")
    private LocalDateTime operationTime;

    /**
     * IP地址
     */
    
    @TableField("ip_address")
    private String ipAddress;

    /**
     * 用户代理
     */
    
    @TableField("user_agent")
    private String userAgent;
}

