package com.ruoyi.im.dto.group;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 缇ょ粍鎴愬憳璇锋眰DTO
 * 
 * @author ruoyi
 */
@ApiModel(description = "缇ょ粍鎴愬憳璇锋眰")
public class ImGroupMembersRequest {

    @ApiModelProperty(value = "鐢ㄦ埛ID鍒楄〃", required = true)
    @NotEmpty(message = "鐢ㄦ埛ID鍒楄〃涓嶈兘涓虹┖")
    private List<Long> userIds;

    public List<Long> getUserIds() {
        return userIds;
    }

    public void setUserIds(List<Long> userIds) {
        this.userIds = userIds;
    }
}
