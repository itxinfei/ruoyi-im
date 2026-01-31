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
 * 用户通知设置表
 * 存储用户的通知偏好设置
 *
 * @author ruoyi
 */
@TableName("im_notification_setting")
@Data
@Schema(description = "用户通知设置")
public class ImNotificationSetting implements Serializable {

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
     * 是否启用通知
     */
    @TableField("enabled")
    @Schema(description = "是否启用通知")
    private Integer enabled;

    /**
     * 桌面通知
     */
    @TableField("desktop_notification")
    @Schema(description = "桌面通知")
    private Integer desktopNotification;

    /**
     * 声音提醒
     */
    @TableField("sound_enabled")
    @Schema(description = "声音提醒")
    private Integer soundEnabled;

    /**
     * 声音类型：default/silent/custom
     */
    @TableField("sound_type")
    @Schema(description = "声音类型")
    private String soundType;

    /**
     * 自定义声音 URL
     */
    @TableField("custom_sound_url")
    @Schema(description = "自定义声音URL")
    private String customSoundUrl;

    /**
     * 显示消息预览
     */
    @TableField("show_preview")
    @Schema(description = "显示消息预览")
    private Integer showPreview;

    /**
     * 免打扰模式
     */
    @TableField("dnd_enabled")
    @Schema(description = "免打扰模式")
    private Integer dndEnabled;

    /**
     * 免打扰开始时间
     */
    @TableField("dnd_start_time")
    @Schema(description = "免打扰开始时间")
    private String dndStartTime;

    /**
     * 免打扰结束时间
     */
    @TableField("dnd_end_time")
    @Schema(description = "免打扰结束时间")
    private String dndEndTime;

    /**
     * 仅接收@提及
     */
    @TableField("mention_only")
    @Schema(description = "仅接收@提及")
    private Integer mentionOnly;

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
