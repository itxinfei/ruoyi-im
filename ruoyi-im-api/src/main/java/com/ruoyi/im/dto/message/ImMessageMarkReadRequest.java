package com.ruoyi.im.dto.message;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.util.List;

/**
 * 消息标记已读请求DTO
 * 
 * @author zhangxy
 */
@ApiModel(description = "消息标记已读请求")
public class ImMessageMarkReadRequest {

    @ApiModelProperty(value = "消息ID列表，为空表示标记会话所有消息已读", example = "[1, 2, 3]")
    private List<@Positive(message = "消息ID必须为正数") Long> messageIds;

    @ApiModelProperty(value = "是否标记会话所有消息已读", example = "false")
    private Boolean markAllRead = false;

    public List<Long> getMessageIds() {
        return messageIds;
    }

    public void setMessageIds(List<Long> messageIds) {
        this.messageIds = messageIds;
    }

    public Boolean getMarkAllRead() {
        return markAllRead;
    }

    public void setMarkAllRead(Boolean markAllRead) {
        this.markAllRead = markAllRead;
    }
}