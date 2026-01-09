package com.ruoyi.im.dto.conversation;

import lombok.Data;

/**
 * 会话更新请求参数
 *
 * @author ruoyi
 */
@Data
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
}