package com.ruoyi.im.dto.auth;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 修改密码请求DTO
 * 
 * @author ruoyi
 */
@ApiModel(description = "修改密码请求")
public class ImUpdatePasswordRequest {

    @ApiModelProperty(value = "原密码", required = true, example = "oldPassword123")
    @NotNull(message = "原密码不能为空")
    @Size(min = 6, max = 20, message = "原密码长度必须在6-20个字符之间")
    private String oldPassword;

    @ApiModelProperty(value = "新密码", required = true, example = "newPassword123")
    @NotNull(message = "新密码不能为空")
    @Size(min = 6, max = 20, message = "新密码长度必须在6-20个字符之间")
    private String newPassword;

    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}