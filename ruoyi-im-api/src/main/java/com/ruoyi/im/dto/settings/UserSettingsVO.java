package com.ruoyi.im.dto.settings;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户设置查询结果 VO
 */
@Data
@Schema(description = "用户设置查询结果")
public class UserSettingsVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 通用设置
     */
    @Schema(description = "通用设置")
    private GeneralSettings general;

    /**
     * 通知设置
     */
    @Schema(description = "通知设置")
    private NotificationSettings notification;

    @Data
    @Schema(description = "通用设置")
    public static class GeneralSettings implements Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * 主题：light/dark/auto
         */
        @Schema(description = "主题模式")
        private String theme;

        /**
         * 语言：zh-CN/en-US
         */
        @Schema(description = "界面语言")
        private String language;

        /**
         * 时间格式：24h/12h
         */
        @Schema(description = "时间格式")
        private String timeFormat;

        /**
         * 开机自启动
         */
        @Schema(description = "开机自启动")
        private Boolean autoStart;

        /**
         * 关闭最小化到托盘
         */
        @Schema(description = "关闭最小化到托盘")
        private Boolean minimizeToTray;
    }

    @Data
    @Schema(description = "通知设置")
    public static class NotificationSettings implements Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * 是否启用通知
         */
        @Schema(description = "启用通知")
        private Boolean enabled;

        /**
         * 桌面通知
         */
        @Schema(description = "桌面通知")
        private Boolean desktopNotification;

        /**
         * 声音提醒
         */
        @Schema(description = "声音提醒")
        private Boolean soundEnabled;

        /**
         * 声音类型：default/silent/custom
         */
        @Schema(description = "声音类型")
        private String soundType;

        /**
         * 自定义声音 URL
         */
        @Schema(description = "自定义声音URL")
        private String customSoundUrl;

        /**
         * 显示消息预览
         */
        @Schema(description = "显示消息预览")
        private Boolean showPreview;

        /**
         * 免打扰模式
         */
        @Schema(description = "免打扰模式")
        private Boolean dndEnabled;

        /**
         * 免打扰开始时间
         */
        @Schema(description = "免打扰开始时间")
        private String dndStartTime;

        /**
         * 免打扰结束时间
         */
        @Schema(description = "免打扰结束时间")
        private String dndEndTime;

        /**
         * 仅接收@提及
         */
        @Schema(description = "仅接收@提及")
        private Boolean mentionOnly;
    }
}
