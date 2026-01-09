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
 * 用户配置实体
 * 映射到 im_user_setting 表
 *
 * 用于存储用户的个人配置，如通知设置、隐私设置、通用设置等
 *
 * @author ruoyi
 */
@TableName("im_user_setting")
@Data
public class ImUserConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 配置ID，主键，自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    @TableField("user_id")
    private Long userId;

    /** 配置类型：NOTIFICATION=通知设置, PRIVACY=隐私设置, DISPLAY=显示设置 */
    @TableField("setting_type")
    private String configType;

    /** 配置键 */
    @TableField("setting_key")
    private String configKey;

    /** 配置值（JSON格式） */
    @TableField("setting_value")
    private String configValue;

    /** 创建时间 */
    @TableField("create_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField("update_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

}
