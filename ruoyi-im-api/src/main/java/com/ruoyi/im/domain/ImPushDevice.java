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
 * 推送设备实体
 *
 * 用于管理用户的推送设备（APNs、FCM等）
 *
 * @author ruoyi
 */
@TableName("im_push_device")
@Data
public class ImPushDevice implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 设备类型：Android */
    public static final String DEVICE_TYPE_ANDROID = "ANDROID";
    /** 设备类型：iOS */
    public static final String DEVICE_TYPE_IOS = "IOS";
    /** 设备类型：Web */
    public static final String DEVICE_TYPE_WEB = "WEB";

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    @TableField("user_id")
    private Long userId;

    /** 设备类型：ANDROID/iOS/WEB */
    @TableField("device_type")
    private String deviceType;

    /** 设备Token（APNs token或FCM token） */
    @TableField("device_token")
    private String deviceToken;

    /** 推送服务客户端ID（Gotify客户端ID） */
    @TableField("gotify_client_id")
    private String gotifyClientId;

    /** 是否激活 */
    @TableField("is_active")
    private Boolean isActive;

    /** 最后活跃时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("last_active_time")
    private LocalDateTime lastActiveTime;

    /** 设备名称 */
    @TableField("device_name")
    private String deviceName;

    /** 应用版本 */
    @TableField("app_version")
    private String appVersion;

    /** 操作系统版本 */
    @TableField("os_version")
    private String osVersion;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("create_time")
    private LocalDateTime createTime;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("update_time")
    private LocalDateTime updateTime;
}
