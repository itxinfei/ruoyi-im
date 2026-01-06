package com.ruoyi.im.dto.conversation;

import javax.validation.constraints.NotNull;

/**
 * 私聊会话创建请求
 *
 * @author ruoyi
 */
public class ImPrivateConversationCreateRequest {

    /**
     * 对方用户ID
     */
    @NotNull(message = "对方用户ID不能为空")
    private Long peerUserId;

    public Long getPeerUserId() {
        return peerUserId;
    }

    public void setPeerUserId(Long peerUserId) {
        this.peerUserId = peerUserId;
    }
}