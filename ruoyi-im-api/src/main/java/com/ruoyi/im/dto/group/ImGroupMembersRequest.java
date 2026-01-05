package com.ruoyi.im.dto.group;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 群组成员请求DTO
 * 
 * @author ruoyi
 */
@ApiModel(description = "群组成员请求")
public class ImGroupMembersRequest {

    @ApiModelProperty(value = "用户ID列表", required = true)
    @NotEmpty(message = "用户ID列表不能为空")
    private List<Long> userIds;

    public List<Long> getUserIds() {
        return userIds;
    }

    public void setUserIds(List<Long> userIds) {
        this.userIds = userIds;
    }
}