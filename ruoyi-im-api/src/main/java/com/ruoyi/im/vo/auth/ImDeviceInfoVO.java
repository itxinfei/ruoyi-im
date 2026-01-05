package com.ruoyi.im.vo.auth;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 设备信息VO
 * 
 * @author ruoyi
 * @since 2024-01-05
 */
@ApiModel(description = "设备信息")
public class ImDeviceInfoVO {

    @ApiModelProperty(value = "设备ID", example = "device-001")
    private String deviceId;

    @ApiModelProperty(value = "设备类型", example = "web")
    private String deviceType;

    @ApiModelProperty(value = "设备名称", example = "Chrome 120.0")
    private String deviceName;

    @ApiModelProperty(value = "操作系统", example = "Windows 10")
    private String os;

    @ApiModelProperty(value = "浏览器", example = "Chrome")
    private String browser;

    @ApiModelProperty(value = "IP地址", example = "192.168.1.100")
    private String ipAddress;

    @ApiModelProperty(value = "登录地点", example = "北京市")
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