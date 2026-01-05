package com.ruoyi.im.vo.auth;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 登录响应VO
 * 
 * @author ruoyi
 * @since 2024-01-05
 */
@ApiModel(description = "登录响应信息")
public class ImLoginVO {

    @ApiModelProperty(value = "访问令牌", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String token;

    @ApiModelProperty(value = "令牌类型", example = "Bearer")
    private String tokenType;

    @ApiModelProperty(value = "过期时间（秒）", example = "3600")
    private Long expiresIn;

    @ApiModelProperty(value = "用户信息")
    private ImUserVO userInfo;

    @ApiModelProperty(value = "登录时间", example = "2024-01-05 10:30:00")
    private String loginTime;

    @ApiModelProperty(value = "设备信息")
    private ImDeviceInfoVO deviceInfo;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public Long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public ImUserVO getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(ImUserVO userInfo) {
        this.userInfo = userInfo;
    }

    public String getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(String loginTime) {
        this.loginTime = loginTime;
    }

    public ImDeviceInfoVO getDeviceInfo() {
        return deviceInfo;
    }

    public void setDeviceInfo(ImDeviceInfoVO deviceInfo) {
        this.deviceInfo = deviceInfo;
    }
}