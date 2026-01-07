package com.ruoyi.im.dto.conversation;

import lombok.Data;

import java.io.Serializable;

/**
 * 会话成员更新请求对象
 *
 * 用于更新会话成员信息
 *
 * @author ruoyi
 */
@Data
public class ImConversationMemberUpdateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 是否置顶 */
    private Integer isPinned;

    /** 是否免打扰 */
    private Integer isMuted;
}
