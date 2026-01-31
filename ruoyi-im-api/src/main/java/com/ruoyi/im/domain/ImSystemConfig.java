package com.ruoyi.im.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统配置实体
 *
 * @author ruoyi
 */
@Data
@TableName("im_system_config")
public class ImSystemConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 配置ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 配置键 */
    @TableField("config_key")
    private String configKey;

    /** 配置值 */
    @TableField("config_value")
    private String configValue;

    /** 配置类型 */
    @TableField("config_type")
    private String configType;

    /** 描述 */
    private String description;

    /** 状态 */
    @TableField("status")
    private String status;

    /** 创建时间 */
    @TableField("create_time")
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField("update_time")
    private LocalDateTime updateTime;
}
