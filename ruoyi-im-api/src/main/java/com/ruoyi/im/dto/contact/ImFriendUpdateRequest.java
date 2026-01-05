package com.ruoyi.im.dto.contact;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;

/**
 * 好友信息更新请求
 * 
 * @author ruoyi
 */
@ApiModel(description = "好友信息更新请求")
public class ImFriendUpdateRequest {

    @ApiModelProperty(value = "好友关系ID", required = true, example = "1")
    @NotNull(message = "好友关系ID不能为空")
    @Positive(message = "好友关系ID必须为正数")
    private Long id;

    @ApiModelProperty(value = "好友别名", example = "张三")
    @Size(max = 50, message = "好友别名长度不能超过50个字符")
    private String alias;

    @ApiModelProperty(value = "好友备注", example = "技术专家")
    @Size(max = 200, message = "好友备注长度不能超过200个字符")
    private String remark;

    @ApiModelProperty(value = "好友用户ID", required = true, example = "1")
    @NotNull(message = "好友用户ID不能为空")
    @Positive(message = "好友用户ID必须为正数")
    private Long friendUserId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Long getFriendUserId() {
        return friendUserId;
    }

    public void setFriendUserId(Long friendUserId) {
        this.friendUserId = friendUserId;
    }
}