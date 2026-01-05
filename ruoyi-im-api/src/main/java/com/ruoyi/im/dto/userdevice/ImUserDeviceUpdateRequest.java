package com.ruoyi.im.dto.userdevice;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;

/**
 * 用户设备更新请求DTO
 * 
 * @author ruoyi
 */
@ApiModel(description = "用户设备更新请求")
public class ImUserDeviceUpdateRequest {

    @ApiModelProperty(value = "用户ID", required = true, example = "1")
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @ApiModelProperty(value = "设备类型", required = true, example = "mobile")
    @NotNull(message = "设备类型不能为空")
    private String deviceType;

    @ApiModelProperty(value = "设备ID", required = true, example = "device123")
    @NotNull(message = "设备ID不能为空")
    private String deviceId;

    @ApiModelProperty(value = "设备名称", example = "iPhone 13")
    private String deviceName;

    @ApiModelProperty(value = "操作系统版本", example = "iOS 15.0")
    private String osVersion;

    @ApiModelProperty(value = "应用版本", example = "1.0.0")
    private String appVersion;

    @ApiModelProperty(value = "IP地址", example = "192.168.1.1")
    private String ipAddress;

    @ApiModelProperty(value = "位置", example = "北京")
    private String location;

    @ApiModelProperty(value = "状态", example = "active")
    private String status;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public void setOsVersion(String osVersion) {
        this.osVersion = osVersion;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}