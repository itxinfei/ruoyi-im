package com.ruoyi.im.vo.message;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 消息信息VO
 * 
 * @author zhangxy
 */
@ApiModel(description = "消息信息")
public class ImMessageVO {

    @ApiModelProperty(value = "消息ID", example = "1")
    private Long id;

    @ApiModelProperty(value = "会话ID", example = "1")
    private Long conversationId;

    @ApiModelProperty(value = "发送者用户ID", example = "1")
    private Long senderId;

    @ApiModelProperty(value = "发送者用户名", example = "zhangsan")
    private String senderUsername;

    @ApiModelProperty(value = "发送者昵称", example = "张三")
    private String senderNickname;

    @ApiModelProperty(value = "消息类型", example = "TEXT")
    private String type;

    @ApiModelProperty(value = "消息内容", example = "你好，世界！")
    private String content;

    @ApiModelProperty(value = "消息状态", example = "NORMAL")
    private String status;

    @ApiModelProperty(value = "消息状态描述", example = "正常")
    private String statusDesc;

    @ApiModelProperty(value = "消息扩展字段", example = "{\"fileName\":\"document.pdf\"}")
    private String extData;

    @ApiModelProperty(value = "被回复的消息ID", example = "100")
    private Long replyMessageId;

    @ApiModelProperty(value = "被回复的消息内容", example = "原始消息内容")
    private String replyMessageContent;

    @ApiModelProperty(value = "创建时间", example = "2024-01-01 10:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间", example = "2024-01-01 10:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    @ApiModelProperty(value = "消息互动列表")
    private List<ImMessageReactionVO> reactions;

    @ApiModelProperty(value = "已读回执列表")
    private List<ImMessageReadReceiptVO> readReceipts;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getConversationId() {
        return conversationId;
    }

    public void setConversationId(Long conversationId) {
        this.conversationId = conversationId;
    }

    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public String getSenderUsername() {
        return senderUsername;
    }

    public void setSenderUsername(String senderUsername) {
        this.senderUsername = senderUsername;
    }

    public String getSenderNickname() {
        return senderNickname;
    }

    public void setSenderNickname(String senderNickname) {
        this.senderNickname = senderNickname;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatusDesc() {
        return statusDesc;
    }

    public void setStatusDesc(String statusDesc) {
        this.statusDesc = statusDesc;
    }

    public String getExtData() {
        return extData;
    }

    public void setExtData(String extData) {
        this.extData = extData;
    }

    public Long getReplyMessageId() {
        return replyMessageId;
    }

    public void setReplyMessageId(Long replyMessageId) {
        this.replyMessageId = replyMessageId;
    }

    public String getReplyMessageContent() {
        return replyMessageContent;
    }

    public void setReplyMessageContent(String replyMessageContent) {
        this.replyMessageContent = replyMessageContent;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public List<ImMessageReactionVO> getReactions() {
        return reactions;
    }

    public void setReactions(List<ImMessageReactionVO> reactions) {
        this.reactions = reactions;
    }

    public List<ImMessageReadReceiptVO> getReadReceipts() {
        return readReceipts;
    }

    public void setReadReceipts(List<ImMessageReadReceiptVO> readReceipts) {
        this.readReceipts = readReceipts;
    }
}