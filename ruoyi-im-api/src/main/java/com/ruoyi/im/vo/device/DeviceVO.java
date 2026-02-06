package com.ruoyi.im.vo.device;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 设备信息VO
 *
 * @author ruoyi
 */
@Data
public class DeviceVO {

    /**
     * 设备ID
     */
    private String deviceId;

    /**
     * 设备类型（web/ios/android/pc/mac/mini）
     */
    private String deviceType;

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 在线状态：0=离线 1=在线
     */
    private Integer onlineStatus;

    /**
     * 最后上线时间
     */
    private LocalDateTime lastOnlineTime;

    /**
     * 最后心跳时间
     */
    private LocalDateTime lastHeartbeatTime;

    /**
     * 客户端版本号
     */
    private String clientVersion;

    /**
     * 是否当前活跃设备
     */
    private Boolean isActive;

    /**
     * 设备类型显示名称
     */
    private String deviceTypeName;

    public String getDeviceTypeName() {
        if (deviceType == null) {
            return "未知设备";
        }
        switch (deviceType) {
            case "web":
                return "Web浏览器";
            case "ios":
                return "iOS";
            case "android":
                return "Android";
            case "pc":
                return "Windows";
            case "mac":
                return "Mac";
            case "mini":
                return "小程序";
            default:
                return deviceType;
        }
    }
}
