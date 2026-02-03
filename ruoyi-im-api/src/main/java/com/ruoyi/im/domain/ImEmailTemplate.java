package com.ruoyi.im.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.ruoyi.im.common.BaseEntity;

import java.io.Serializable;

/**
 * 邮件模板实体
 *
 * 用于定义常用的邮件模板，支持变量替换
 *
 * @author ruoyi
 */
@TableName("im_email_template")
@Data
@EqualsAndHashCode(callSuper = true)
public class ImEmailTemplate extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 模板ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 模板名称
     */
    @TableField("template_name")
    private String templateName;

    /**
     * 模板编码
     */
    @TableField("template_code")
    private String templateCode;

    /**
     * 模板主题
     */
    @TableField("subject")
    private String subject;

    /**
     * 模板内容（支持变量替换）
     * 变量格式：${variableName}
     */
    @TableField("content")
    private String content;

    /**
     * 模板类型
     * SYSTEM-系统模板, USER-用户自定义
     */
    @TableField("template_type")
    private String templateType;

    /**
     * 适用范围
     * ALL-全部, INTERNAL-内部, EXTERNAL-外部
     */
    @TableField("scope")
    private String scope;

    /**
     * 变量说明（JSON格式）
     * 描述模板中可用的变量及其说明
     */
    @TableField("variables")
    private String variables;

    /**
     * 是否启用
     */
    @TableField("is_enabled")
    private Integer isEnabled;

    /**
     * 使用次数
     */
    @TableField("usage_count")
    private Integer usageCount;

    /**
     * 分类
     */
    @TableField("category")
    private String category;

    /**
     * 创建者
     */
    @TableField("create_by")
    private String createBy;

    /**
     * 更新者
     */
    @TableField("update_by")
    private String updateBy;
}
