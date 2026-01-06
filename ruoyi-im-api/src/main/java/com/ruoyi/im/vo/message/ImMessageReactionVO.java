package com.ruoyi.im.vo.message;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.time.LocalDateTime;

/**
 * 娑堟伅浜掑姩VO
 * 
 * @author zhangxy
 */
@ApiModel(description = "娑堟伅浜掑姩")
public class ImMessageReactionVO {

    @ApiModelProperty(value = "浜掑姩ID", example = "1")
    private Long id;

    @ApiModelProperty(value = "娑堟伅ID", example = "1")
    private Long messageId;

    @ApiModelProperty(value = "鐢ㄦ埛ID", example = "1")
    private Long userId;

    @ApiModelProperty(value = "鐢ㄦ埛鍚?, example = "zhangsan")
    private String username;

    @ApiModelProperty(value = "鐢ㄦ埛鏄电О", example = "寮犱笁")
    private String nickname;

    @ApiModelProperty(value = "浜掑姩绫诲瀷", example = "LIKE")
    private String reactionType;

    @ApiModelProperty(value = "浜掑姩绫诲瀷鎻忚堪", example = "鐐硅禐")
    private String reactionTypeDesc;

    @ApiModelProperty(value = "鍒涘缓鏃堕棿", example = "2024-01-01 10:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

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

    public String getReactionType() {
        return reactionType;
    }

    public void setReactionType(String reactionType) {
        this.reactionType = reactionType;
    }

    public String getReactionTypeDesc() {
        return reactionTypeDesc;
    }

    public void setReactionTypeDesc(String reactionTypeDesc) {
        this.reactionTypeDesc = reactionTypeDesc;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
