package com.ruoyi.im.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户设置表
 * 存储用户的个人设置配置（主题、语言、启动行为等）
 *
 * @author ruoyi
 */
@TableName("im_user_settings")
@Data
@Schema(description = "用户设置")
public class ImUserSettings implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 设置ID
     */
    @TableId(type = IdType.AUTO)
    @Schema(description = "设置ID")
    private Long id;

    /**
     * 用户ID
     */
    @TableField("user_id")
    @Schema(description = "用户ID")
    private Long userId;

    /**
     * 设置键
     * 通用设置：theme, language, timeFormat, autoStart, minimizeToTray
     * 通知设置：desktopNotification, soundEnabled, soundType, showPreview, dndEnabled, dndStartTime, dndEndTime, mentionOnly
     * 安全设置：twoFactorEnabled
     * 存储设置：keepOnLogout
     */
    @TableField("setting_key")
    @Schema(description = "设置键")
    private String settingKey;

    /**
     * 设置值（JSON格式存储）
     * 例如：theme->"dark", language->"zh-CN"
     */
    @TableField("setting_value")
    @Schema(description = "设置值")
    private String settingValue;

    /**
     * 设置类型：general/notification/security/storage
     */
    @TableField("setting_type")
    @Schema(description = "设置类型")
    private String settingType;

    /**
     * 创建时间
     */
    @TableField("create_time")
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField("update_time")
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
