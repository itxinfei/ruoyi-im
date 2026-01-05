package com.ruoyi.im.dto.message;

import com.ruoyi.im.dto.BasePageRequest;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

/**
 * 消息查询请求DTO
 * 
 * @author zhangxy
 */
@ApiModel(description = "消息查询请求")
public class ImMessageQueryRequest extends BasePageRequest {

    @ApiModelProperty(value = "会话ID", required = true, example = "1")
    @NotNull(message = "会话ID不能为空")
    @Positive(message = "会话ID必须为正数")
    private Long conversationId;

    @ApiModelProperty(value = "消息类型", example = "TEXT")
    private String messageType;

    @ApiModelProperty(value = "关键词查询条件，支持模糊匹配", example = "hello")
    private String keywords;

    @ApiModelProperty(value = "消息ID，用于分页查询", example = "100")
    private Long lastMessageId;

    public Long getConversationId() {
        return conversationId;
    }

    public void setConversationId(Long conversationId) {
        this.conversationId = conversationId;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public Long getLastMessageId() {
        return lastMessageId;
    }

    public void setLastMessageId(Long lastMessageId) {
        this.lastMessageId = lastMessageId;
    }
}