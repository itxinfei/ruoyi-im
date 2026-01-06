package com.ruoyi.im.dto.message;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 消息发送请求
 *
 * @author ruoyi
 */
public class ImMessageSendRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 会话ID */
    @NotNull(message = "会话ID不能为空")
    private Long sessionId;

    /** 消息类型 */
    @NotBlank(message = "消息类型不能为空")
    private String type;

    /** 消息内容 */
    @NotBlank(message = "消息内容不能为空")
    private String content;

    /** 接收者ID（群组消息时必填） */
    private Long receiverId;

    /** 扩展数据（JSON格式，用于文件、图片等） */
    private String extra;

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
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

    public Long getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(Long receiverId) {
        this.receiverId = receiverId;
    }

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }
}
