package com.ruoyi.im.vo.message;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.time.LocalDateTime;

/**
 * 娑堟伅宸茶鍥炴墽VO
 * 
 * @author zhangxy
 */
@ApiModel(description = "娑堟伅宸茶鍥炴墽")
public class ImMessageReadReceiptVO {

    @ApiModelProperty(value = "鍥炴墽ID", example = "1")
    private Long id;

    @ApiModelProperty(value = "娑堟伅ID", example = "1")
    private Long messageId;

    @ApiModelProperty(value = "浼氳瘽ID", example = "1")
    private Long conversationId;

    @ApiModelProperty(value = "鐢ㄦ埛ID", example = "1")
    private Long userId;

    @ApiModelProperty(value = "鐢ㄦ埛鍚?, example = "zhangsan")
    private String username;

    @ApiModelProperty(value = "鐢ㄦ埛鏄电О", example = "寮犱笁")
    private String nickname;

    @ApiModelProperty(value = "宸茶鏃堕棿", example = "2024-01-01 10:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime readTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getMessageId() {
        return messageId;
    }

    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }

    public Long getConversationId() {
        return conversationId;
    }

    public void setConversationId(Long conversationId) {
        this.conversationId = conversationId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public LocalDateTime getReadTime() {
        return readTime;
    }

    public void setReadTime(LocalDateTime readTime) {
        this.readTime = readTime;
    }
}
