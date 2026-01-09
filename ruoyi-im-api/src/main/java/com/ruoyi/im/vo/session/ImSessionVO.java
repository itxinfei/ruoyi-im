package com.ruoyi.im.vo.session;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 会话响应VO（已废弃）
 *
 * 该VO已被 ImConversationMemberVO 替代
 * 请使用 ImConversationMemberVO 表示会话成员信息
 *
 * @deprecated 使用 {@link ImConversationMemberVO} 替代
 * @author ruoyi
 */
@Data
@Deprecated
public class ImSessionVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 会话ID */
    private Long id;

    /** 会话类型: private=私聊, group=群聊 */
    private String type;

    /** 会话名称 */
    private String name;

    /** 会话头像 */
    private String avatar;

    /** 对方ID（私聊时为对方ID，群聊时为群组ID） */
    private Long peerId;

    /** 未读消息数 */
    private Integer unreadCount;

    /** 是否置顶: 0=否, 1=是 */
    private Integer isPinned;

    /** 是否免打扰: 0=否, 1=是 */
    private Integer isMuted;

    /** 最后消息内容 */
    private String lastMessage;

    /** 最后消息时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastMessageTime;

    /** 最后消息类型 */
    private String lastMessageType;

    /** 对方在线状态（仅私聊） */
    private Boolean online;
}
