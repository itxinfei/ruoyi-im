package com.ruoyi.im.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * IM用户设置实体
 *
 * 用于集中管理用户的偏好设置，包括通知、隐私、显示等
 * 支持灵活的键值对存储方式，便于扩展新设置项
 *
 * @author ruoyi
 */
@TableName("im_user_setting")
public class ImUserSetting implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID，自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID，关联到im_user表 */
    private Long userId;

    /** 设置键名，如: message_notification, sound_enabled, show_online_status 等 */
    private String settingKey;

    /** 设置值，JSON格式存储，支持复杂结构 */
    private String settingValue;

    /** 设置类型: NOTIFICATION=通知设置, PRIVACY=隐私设置, DISPLAY=显示设置 */
    private String settingType;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getSettingKey() {
        return settingKey;
    }

    public void setSettingKey(String settingKey) {
        this.settingKey = settingKey;
    }

    public String getSettingValue() {
        return settingValue;
    }

    public void setSettingValue(String settingValue) {
        this.settingValue = settingValue;
    }

    public String getSettingType() {
        return settingType;
    }

    public void setSettingType(String settingType) {
        this.settingType = settingType;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public String toString() {
        return "ImUserSetting{" +
                "id=" + id +
                ", userId=" + userId +
                ", settingKey='" + settingKey + '\'' +
                ", settingValue='" + settingValue + '\'' +
                ", settingType='" + settingType + '\'' +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                '}';
    }

    /**
     * 设置类型常量
     */
    public static class SettingType {
        /** 通知设置 */
        public static final String NOTIFICATION = "NOTIFICATION";
        /** 隐私设置 */
        public static final String PRIVACY = "PRIVACY";
        /** 显示设置 */
        public static final String DISPLAY = "DISPLAY";
    }

    /**
     * 常用设置键名常量
     */
    public static class SettingKey {
        /** 消息通知开关 */
        public static final String MESSAGE_NOTIFICATION = "message_notification";
        /** 声音提醒开关 */
        public static final String SOUND_ENABLED = "sound_enabled";
        /** 桌面通知开关 */
        public static final String DESKTOP_NOTIFICATION = "desktop_notification";
        /** 显示在线状态 */
        public static final String SHOW_ONLINE_STATUS = "show_online_status";
        /** 允许陌生人消息 */
        public static final String ALLOW_STRANGER_MESSAGE = "allow_stranger_message";
        /** 消息预览 */
        public static final String MESSAGE_PREVIEW = "message_preview";
    }
}
