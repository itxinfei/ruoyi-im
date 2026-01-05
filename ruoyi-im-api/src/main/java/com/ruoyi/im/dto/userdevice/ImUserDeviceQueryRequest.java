package com.ruoyi.im.dto.userdevice;

import com.ruoyi.im.common.BasePageRequest;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 用户设备查询请求DTO
 * 
 * @author ruoyi
 */
@ApiModel(description = "用户设备查询请求")
public class ImUserDeviceQueryRequest extends BasePageRequest {

    @ApiModelProperty(value = "用户ID", example = "1")
    private Long userId;

    @ApiModelProperty(value = "设备类型", example = "mobile")
    private String deviceType;

    @ApiModelProperty(value = "设备ID", example = "device123")
    private String deviceId;

    @ApiModelProperty(value = "设备状态", example = "active")
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}