package com.ruoyi.im.dto.contact;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

/**
 * 好友添加请求
 * 
 * @author ruoyi
 */
@ApiModel(description = "好友添加请求")
public class ImFriendAddRequest {

    @ApiModelProperty(value = "好友用户ID", required = true, example = "1")
    @NotNull(message = "好友用户ID不能为空")
    @Positive(message = "好友用户ID必须为正数")
    private Long friendUserId;

    @ApiModelProperty(value = "好友申请消息", example = "你好，我想加你为好友")
    @NotBlank(message = "好友申请消息不能为空")
    private String message;

    @ApiModelProperty(value = "好友别名", example = "张三")
    private String alias;

    public Long getFriendUserId() {
        return friendUserId;
    }

    public void setFriendUserId(Long friendUserId) {
        this.friendUserId = friendUserId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }
}