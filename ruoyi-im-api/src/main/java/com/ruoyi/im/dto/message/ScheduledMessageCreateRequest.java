package com.ruoyi.im.dto.message;

import com.fasterxml.jackson.annotation.JsonFormat;

import javax.validation.constraints.Future;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * 定时消息创建请求
 *
 * @author ruoyi
 */
public class ScheduledMessageCreateRequest {

    /** 会话ID */
    @NotNull(message = "会话ID不能为空")
    private Long conversationId;

    /** 消息类型：TEXT/FILE/IMAGE/VOICE/VIDEO */
    @NotNull(message = "消息类型不能为空")
    private String messageType;

    /** 消息内容 */
    private String content;

    /** 文件URL */
    private String fileUrl;

    /** 文件名 */
    private String fileName;

    /** 回复的消息ID */
    private Long replyToMessageId;

    /** 定时发送时间 */
    @NotNull(message = "定时发送时间不能为空")
    @Future(message = "定时发送时间必须是未来时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime scheduledTime;

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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Long getReplyToMessageId() {
        return replyToMessageId;
    }

    public void setReplyToMessageId(Long replyToMessageId) {
        this.replyToMessageId = replyToMessageId;
    }

    public LocalDateTime getScheduledTime() {
        return scheduledTime;
    }

    public void setScheduledTime(LocalDateTime scheduledTime) {
        this.scheduledTime = scheduledTime;
    }
}
