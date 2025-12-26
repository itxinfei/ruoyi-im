package com.ruoyi.im.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 系统配置对象 im_system_config
 * 
 * @author ruoyi
 * @date 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("im_system_config")
public class ImSystemConfig extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 配置键 */
    @Excel(name = "配置键")
    @TableField("config_key")
    private String configKey;

    /** 配置值 */
    @Excel(name = "配置值")
    @TableField("config_value")
    private String configValue;

    /** 配置描述 */
    @Excel(name = "配置描述")
    @TableField("description")
    private String description;

    /** 配置类型（STRING字符串 INTEGER整数 BOOLEAN布尔 JSON对象） */
    @Excel(name = "配置类型", readConverterExp = "STRING=字符串,INTEGER=整数,BOOLEAN=布尔,JSON=对象")
    @TableField("type")
    private String type;

    /** 是否启用（0否 1是） */
    @Excel(name = "是否启用", readConverterExp = "0=否,1=是")
    @TableField("enabled")
    private Boolean enabled;

    /**
     * 获取字符串类型的配置值
     * @return 字符串值
     */
    public String getStringValue() {
        return configValue;
    }

    /**
     * 获取整数类型的配置值
     * @return 整数值
     */
    public Integer getIntegerValue() {
        if (configValue == null) {
            return null;
        }
        try {
            return Integer.parseInt(configValue);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 获取布尔类型的配置值
     * @return 布尔值
     */
    public Boolean getBooleanValue() {
        if (configValue == null) {
            return null;
        }
        return Boolean.parseBoolean(configValue);
    }

    /**
     * 获取长整型的配置值
     * @return 长整型值
     */
    public Long getLongValue() {
        if (configValue == null) {
            return null;
        }
        try {
            return Long.parseLong(configValue);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}