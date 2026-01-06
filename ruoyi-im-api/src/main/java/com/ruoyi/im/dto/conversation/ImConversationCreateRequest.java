package com.ruoyi.im.dto.conversation;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 会话创建请求参数
 *
 * @author ruoyi
 */
public class ImConversationCreateRequest {

    /**
     * 会话类型：PRIVATE(单聊)、GROUP(群聊)
     */
    @NotBlank(message = "会话类型不能为空")
    private String type;

    /**
     * 目标ID：单聊为目标用户ID，群聊为群组ID
     */
    @NotNull(message = "目标ID不能为空")
    private Long targetId;

    /**
     * 会话名称（可选）
     */
    private String name;

    /**
     * 是否置顶
     */
    private Boolean isPinned;

    /**
     * 是否免打扰
     */
    private Boolean isMuted;

    // Getters and Setters
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getTargetId() {
        return targetId;
    }

    public void setTargetId(Long targetId) {
        this.targetId = targetId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getIsPinned() {
        return isPinned;
    }

    public void setIsPinned(Boolean isPinned) {
        this.isPinned = isPinned;
    }

    public Boolean getIsMuted() {
        return isMuted;
    }

    public void setIsMuted(Boolean isMuted) {
        this.isMuted = isMuted;
    }
}