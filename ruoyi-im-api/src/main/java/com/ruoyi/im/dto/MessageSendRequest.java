package com.ruoyi.im.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class MessageSendRequest {
    
    @NotNull(message = "会话ID不能为空")
    private Long conversationId;
    
    @NotNull(message = "消息类型不能为空")
    private Integer messageType;
    
    @NotBlank(message = "消息内容不能为空")
    @Size(max = 10000, message = "消息内容不能超过10000个字符")
    private String content;
    
    private String extra;
    
    public Long getConversationId() {
        return conversationId;
    }
    
    public void setConversationId(Long conversationId) {
        this.conversationId = conversationId;
    }
    
    public Integer getMessageType() {
        return messageType;
    }
    
    public void setMessageType(Integer messageType) {
        this.messageType = messageType;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public String getExtra() {
        return extra;
    }
    
    public void setExtra(String extra) {
        this.extra = extra;
    }
}
