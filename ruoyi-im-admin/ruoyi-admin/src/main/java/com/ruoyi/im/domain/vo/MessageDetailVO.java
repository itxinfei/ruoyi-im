package com.ruoyi.im.domain.vo;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

/**
 * 消息详细信息VO
 * 
 * @author ruoyi
 * @version 1.0
 * @since 2025-01-22
 */
@Data
public class MessageDetailVO {

    private Long id;

    private Long conversationId;

    private String conversationName;

    private Long senderId;

    private String senderName;

    private String senderAvatar;

    private String messageType;

    private String messageTypeDisplay;

    private String content;

    private Integer sensitiveLevel;

    private String sensitiveLevelDisplay;

    private Long replyToMessageId;

    private String replyToContent;

    private Long forwardFromMessageId;

    private String forwardFromContent;

    private Integer isRevoked;

    private Integer isEdited;

    private Integer isDeleted;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime revokedTime;

    public String getMessageTypeDisplay() {
        if (messageType == null) return "未知";
        switch (messageType) {
            case "TEXT": return "文本";
            case "IMAGE": return "图片";
            case "FILE": return "文件";
            case "VOICE": return "语音";
            case "VIDEO": return "视频";
            case "SYSTEM": return "系统消息";
            default: return messageType;
        }
    }

    public String getSensitiveLevelDisplay() {
        if (sensitiveLevel == null) return "普通";
        switch (sensitiveLevel) {
            case 0: return "普通";
            case 1: return "敏感";
            case 2: return "高度敏感";
            default: return "未知";
        }
    }
}