package com.ruoyi.im.dto.session;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * 会话更新请求（已废弃）
 *
 * 该请求已被 ImConversationMemberUpdateRequest 替代
 * 请使用 ImConversationMemberUpdateRequest 更新会话成员信息
 *
 * @deprecated 使用 {@link ImConversationMemberUpdateRequest} 替代
 * @author ruoyi
 */
@Data
@Deprecated
public class ImSessionUpdateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 会话名称 */
    @Size(max = 100, message = "会话名称长度不能超过100")
    private String name;

    /** 是否置顶: 0=否, 1=是 */
    private Integer isPinned;

    /** 是否免打扰: 0=否, 1=是 */
    private Integer isMuted;
}
