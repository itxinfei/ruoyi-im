package com.ruoyi.im.dto.group;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.util.List;

/**
 * 群组成员添加请求DTO
 * 
 * @author ruoyi
 */
@ApiModel(description = "群组成员添加请求")
public class ImGroupMemberAddRequest {

    @ApiModelProperty(value = "群组ID", required = true, example = "1")
    @NotNull(message = "群组ID不能为空")
    @Positive(message = "群组ID必须为正数")
    private Long groupId;

    @ApiModelProperty(value = "用户ID列表", required = true, example = "[1, 2, 3]")
    @NotNull(message = "用户ID列表不能为空")
    private List<@Positive(message = "用户ID必须为正数") Long> userIds;

    @ApiModelProperty(value = "邀请人ID", example = "1")
    private Long inviterId;

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public List<Long> getUserIds() {
        return userIds;
    }

    public void setUserIds(List<Long> userIds) {
        this.userIds = userIds;
    }

    public Long getInviterId() {
        return inviterId;
    }

    public void setInviterId(Long inviterId) {
        this.inviterId = inviterId;
    }
}