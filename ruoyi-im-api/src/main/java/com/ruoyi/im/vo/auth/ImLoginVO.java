package com.ruoyi.im.vo.auth;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 鐧诲綍鍝嶅簲VO
 * 
 * @author ruoyi
 * @since 2024-01-05
 */
@ApiModel(description = "鐧诲綍鍝嶅簲淇℃伅")
public class ImLoginVO {

    @ApiModelProperty(value = "璁块棶浠ょ墝", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String token;

    @ApiModelProperty(value = "浠ょ墝绫诲瀷", example = "Bearer")
    private String tokenType;

    @ApiModelProperty(value = "杩囨湡鏃堕棿锛堢锛?, example = "3600")
    private Long expiresIn;

    @ApiModelProperty(value = "鐢ㄦ埛淇℃伅")
    private ImUserVO userInfo;

    @ApiModelProperty(value = "鐧诲綍鏃堕棿", example = "2024-01-05 10:30:00")
    private String loginTime;

    @ApiModelProperty(value = "璁惧淇℃伅")
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
