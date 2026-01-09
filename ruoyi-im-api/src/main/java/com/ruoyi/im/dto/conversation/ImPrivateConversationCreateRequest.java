package com.ruoyi.im.dto.conversation;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 私聊会话创建请求
 *
 * @author ruoyi
 */
@Data
public class ImPrivateConversationCreateRequest {

    /**
     * 对方用户ID
     */
    @NotNull(message = "对方用户ID不能为空")
    private Long peerUserId;
}