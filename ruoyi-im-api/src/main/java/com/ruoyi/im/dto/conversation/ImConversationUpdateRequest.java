package com.ruoyi.im.dto.conversation;

/**
 * 会话更新请求参数
 *
 * @author ruoyi
 */
public class ImConversationUpdateRequest {

    /**
     * 会话名称
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

    /**
     * 备注
     */
    private String remark;

    // Getters and Setters
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

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}