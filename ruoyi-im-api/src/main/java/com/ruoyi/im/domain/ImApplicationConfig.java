package com.ruoyi.im.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.ruoyi.im.common.BaseEntity;

import java.io.Serializable;

/**
 * 应用配置项实体
 *
 * 定义应用级别的配置项，如主题、语言、通知设置等
 *
 * @author ruoyi
 */
@TableName("im_application_config")
@Data
@EqualsAndHashCode(callSuper = true)
public class ImApplicationConfig extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 应用ID
     */
    @TableField("app_id")
    private Long appId;

    /**
     * 配置键
     */
    @TableField("config_key")
    private String configKey;

    /**
     * 配置值
     */
    @TableField("config_value")
    private String configValue;

    /**
     * 配置类型
     * STRING-字符串, NUMBER-数字, BOOLEAN-布尔, JSON-JSON对象, ARRAY-数组
     */
    @TableField("config_type")
    private String configType;

    /**
     * 配置描述
     */
    @TableField("description")
    private String description;

    /**
     * 是否必填：0=否, 1=是
     */
    @TableField("is_required")
    private Integer isRequired;

    /**
     * 可选值列表（JSON格式数组）
     */
    @TableField("options")
    private String options;

    /**
     * 默认值
     */
    @TableField("default_value")
    private String defaultValue;

    /**
     * 配置分组（用于界面分组显示）
     */
    @TableField("config_group")
    private String configGroup;

    /**
     * 排序
     */
    @TableField("sort_order")
    private Integer sortOrder;

    /**
     * 是否启用：0=禁用, 1=启用
     */
    @TableField("is_enabled")
    private Integer isEnabled;

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
