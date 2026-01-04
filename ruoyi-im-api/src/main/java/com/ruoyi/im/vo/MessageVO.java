package com.ruoyi.im.vo;

import com.ruoyi.im.domain.ImMessage;
import com.ruoyi.im.domain.ImUser;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * 消息视图对象
 * 包含消息详情和发送者信息，用于前端展示
 *
 * @author ruoyi
 */
public class MessageVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 消息ID
     */
    private Long id;

    /**
     * 会话ID
     */
    private Long sessionId;

    /**
     * 发送者ID
     */
    private Long senderId;

    /**
     * 发送者昵称
     */
    private String senderName;

    /**
     * 发送者头像
     */
    private String senderAvatar;

    /**
     * 消息类型
     */
    private String type;

    /**
     * 消息内容
     */
    private Object content;

    /**
     * 消息状态
     */
    private String status;

    /**
     * 回复的消息ID
     */
    private Long replyTo;

    /**
     * 被回复的消息内容（简要）
     */
    private MessageVO replyToMessage;

    /**
     * 时间戳（毫秒）
     */
    private Long timestamp;

    /**
     * ISO格式时间字符串
     */
    private String time;

    /**
     * 客户端消息ID（用于去重）
     */
    private String clientMsgId;

    /**
     * 是否已撤回
     */
    private Boolean revoked;

    /**
     * 撤回时间
     */
    private Long revokedTime;

    public MessageVO() {
    }

    /**
     * 从ImMessage和ImUser构建MessageVO
     */
    public static MessageVO fromMessage(ImMessage message, ImUser sender) {
        if (message == null) {
            return null;
        }

        MessageVO vo = new MessageVO();
        vo.setId(message.getId());
        vo.setSessionId(message.getConversationId());
        vo.setSenderId(message.getSenderId());
        vo.setType(message.getType() != null ? message.getType().toLowerCase() : "text");
        vo.setStatus(message.getStatus() != null ? message.getStatus().toLowerCase() : "sent");
        vo.setReplyTo(message.getReplyToMessageId());

        // 处理消息内容
        vo.setContent(parseContent(message.getType(), message.getContent()));

        // 处理时间
        if (message.getCreateTime() != null) {
            vo.setTimestamp(message.getCreateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
            vo.setTime(message.getCreateTime().toString());
        } else {
            vo.setTimestamp(System.currentTimeMillis());
            vo.setTime(LocalDateTime.now().toString());
        }

        // 处理撤回状态
        vo.setRevoked("REVOKED".equalsIgnoreCase(message.getStatus()));
        if (message.getRevokedTime() != null) {
            vo.setRevokedTime(message.getRevokedTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        }

        // 设置发送者信息
        if (sender != null) {
            vo.setSenderName(sender.getNickname() != null ? sender.getNickname() : sender.getUsername());
            vo.setSenderAvatar(sender.getAvatar());
        } else {
            vo.setSenderName("未知用户");
            vo.setSenderAvatar(null);
        }

        return vo;
    }

    /**
     * 解析消息内容
     * 文本消息返回纯文本，其他类型返回解析后的对象
     */
    private static Object parseContent(String type, String content) {
        if (content == null) {
            return "";
        }

        // 尝试解析JSON格式的内容
        if (content.startsWith("{") && content.endsWith("}")) {
            try {
                // 如果是文本消息，提取text字段
                if ("TEXT".equalsIgnoreCase(type)) {
                    // 简单解析 {"text":"xxx"} 格式
                    if (content.contains("\"text\"")) {
                        int start = content.indexOf("\"text\"") + 8;
                        int end = content.lastIndexOf("\"");
                        if (start < end) {
                            return content.substring(start, end);
                        }
                    }
                }
                // 其他类型返回原始JSON字符串，让前端解析
                return content;
            } catch (Exception e) {
                return content;
            }
        }

        return content;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getSenderAvatar() {
        return senderAvatar;
    }

    public void setSenderAvatar(String senderAvatar) {
        this.senderAvatar = senderAvatar;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Object getContent() {
        return content;
    }

    public void setContent(Object content) {
        this.content = content;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getReplyTo() {
        return replyTo;
    }

    public void setReplyTo(Long replyTo) {
        this.replyTo = replyTo;
    }

    public MessageVO getReplyToMessage() {
        return replyToMessage;
    }

    public void setReplyToMessage(MessageVO replyToMessage) {
        this.replyToMessage = replyToMessage;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getClientMsgId() {
        return clientMsgId;
    }

    public void setClientMsgId(String clientMsgId) {
        this.clientMsgId = clientMsgId;
    }

    public Boolean getRevoked() {
        return revoked;
    }

    public void setRevoked(Boolean revoked) {
        this.revoked = revoked;
    }

    public Long getRevokedTime() {
        return revokedTime;
    }

    public void setRevokedTime(Long revokedTime) {
        this.revokedTime = revokedTime;
    }
}
