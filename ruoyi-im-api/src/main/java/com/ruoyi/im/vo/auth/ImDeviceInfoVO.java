package com.ruoyi.im.vo.auth;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 璁惧淇℃伅VO
 * 
 * @author ruoyi
 * @since 2024-01-05
 */
@ApiModel(description = "璁惧淇℃伅")
public class ImDeviceInfoVO {

    @ApiModelProperty(value = "璁惧ID", example = "device-001")
    private String deviceId;

    @ApiModelProperty(value = "璁惧绫诲瀷", example = "web")
    private String deviceType;

    @ApiModelProperty(value = "璁惧鍚嶇О", example = "Chrome 120.0")
    private String deviceName;

    @ApiModelProperty(value = "鎿嶄綔绯荤粺", example = "Windows 10")
    private String os;

    @ApiModelProperty(value = "娴忚鍣?, example = "Chrome")
    private String browser;

    @ApiModelProperty(value = "IP鍦板潃", example = "192.168.1.100")
    private String ipAddress;

    @ApiModelProperty(value = "鐧诲綍鍦扮偣", example = "鍖椾含甯?)
    private String location;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public String getBrowser() {
        return browser;
    }

    public void setBrowser(String browser) {
        this.browser = browser;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
