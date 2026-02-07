package com.ruoyi.im.dto.push;

import javax.validation.constraints.NotBlank;

/**
 * 推送设备注册请求
 *
 * @author ruoyi
 */
public class PushDeviceRegisterRequest {

    /** 设备类型：ANDROID/iOS/WEB */
    @NotBlank(message = "设备类型不能为空")
    private String deviceType;

    /** 设备Token（APNs token或FCM token） */
    @NotBlank(message = "设备Token不能为空")
    private String deviceToken;

    /** Gotify客户端ID（Web设备使用） */
    private String gotifyClientId;

    /** 设备名称 */
    private String deviceName;

    /** 应用版本 */
    private String appVersion;

    /** 操作系统版本 */
    private String osVersion;

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getDeviceToken() {
        return deviceToken;
    }

    public void setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
    }

    public String getGotifyClientId() {
        return gotifyClientId;
    }

    public void setGotifyClientId(String gotifyClientId) {
        this.gotifyClientId = gotifyClientId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public void setOsVersion(String osVersion) {
        this.osVersion = osVersion;
    }
}
