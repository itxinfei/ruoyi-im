package com.ruoyi.im.dto.group;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;

/**
 * 群组更新请求DTO
 * 
 * @author ruoyi
 */
@ApiModel(description = "群组更新请求")
public class ImGroupUpdateRequest {

    @ApiModelProperty(value = "群组ID", required = true, example = "1")
    @NotNull(message = "群组ID不能为空")
    @Positive(message = "群组ID必须为正数")
    private Long id;

    @ApiModelProperty(value = "群组名称", example = "技术交流群")
    @Size(max = 50, message = "群组名称长度不能超过50个字符")
    private String name;

    @ApiModelProperty(value = "群组公告", example = "欢迎大家加入技术交流群")
    @Size(max = 200, message = "群组公告长度不能超过200个字符")
    private String notice;

    @ApiModelProperty(value = "群组头像", example = "/profile/group.png")
    private String avatar;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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