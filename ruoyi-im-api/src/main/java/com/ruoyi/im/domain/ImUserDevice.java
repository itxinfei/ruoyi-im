package com.ruoyi.im.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ruoyi.im.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户设备实体
 *
 * 用于管理用户的多设备登录，支持设备类型识别、在线状态查询
 * 参考野火IM的设备管理设计
 *
 * @author ruoyi
 */
@TableName("im_user_device")
@Data
@EqualsAndHashCode(callSuper = false)
public class ImUserDevice extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 设备类型：Web端
     */
    public static final String DEVICE_TYPE_WEB = "web";

    /**
     * 设备类型：iOS
     */
    public static final String DEVICE_TYPE_IOS = "ios";

    /**
     * 设备类型：Android
     */
    public static final String DEVICE_TYPE_ANDROID = "android";

    /**
     * 设备类型：PC Windows
     */
    public static final String DEVICE_TYPE_PC = "pc";

    /**
     * 设备类型：Mac
     */
    public static final String DEVICE_TYPE_MAC = "mac";

    /**
     * 设备类型：小程序
     */
    public static final String DEVICE_TYPE_MINI = "mini";

    /**
     * 在线状态：离线
     */
    public static final int STATUS_OFFLINE = 0;

    /**
     * 在线状态：在线
     */
    public static final int STATUS_ONLINE = 1;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 设备ID（客户端生成，UUID格式）
     */
    @TableField("device_id")
    private String deviceId;

    /**
     * 设备类型（web/ios/android/pc/mac/mini）
     */
    @TableField("device_type")
    private String deviceType;

    /**
     * 设备名称（如：iPhone 14 Pro、Chrome浏览器、Windows PC）
     */
    @TableField("device_name")
    private String deviceName;

    /**
     * 在线状态：0=离线 1=在线
     */
    @TableField("online_status")
    private Integer onlineStatus;

    /**
     * 最后上线时间
     */
    @TableField("last_online_time")
    private java.time.LocalDateTime lastOnlineTime;

    /**
     * 最后心跳时间
     */
    @TableField("last_heartbeat_time")
    private java.time.LocalDateTime lastHeartbeatTime;

    /**
     * 客户端版本号
     */
    @TableField("client_version")
    private String clientVersion;

    /**
     * 操作系统版本
     */
    @TableField("os_version")
    private String osVersion;

    /**
     * IP地址
     */
    @TableField("ip_address")
    private String ipAddress;

    /**
     * 是否当前活跃设备
     */
    @TableField("is_active")
    private Boolean isActive;
}
