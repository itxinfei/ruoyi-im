package com.ruoyi.im.dto.group;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;

/**
 * 群组创建请求DTO
 * 
 * @author ruoyi
 */
@ApiModel(description = "群组创建请求")
public class ImGroupCreateRequest {

    @ApiModelProperty(value = "群组名称", required = true, example = "技术交流群")
    @NotBlank(message = "群组名称不能为空")
    @Size(max = 50, message = "群组名称长度不能超过50个字符")
    private String name;

    @ApiModelProperty(value = "群主ID", required = true, example = "1")
    @NotNull(message = "群主ID不能为空")
    @Positive(message = "群主ID必须为正数")
    private Long ownerId;

    @ApiModelProperty(value = "群组公告", example = "欢迎大家加入技术交流群")
    @Size(max = 200, message = "群组公告长度不能超过200个字符")
    private String notice;

    @ApiModelProperty(value = "群组头像", example = "/profile/group.png")
    private String avatar;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public String getNotice() {
        return notice;
    }

    public void setNotice(String notice) {
        this.notice = notice;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}