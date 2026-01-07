package com.ruoyi.im.vo.conversation;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 会话成员视图对象
 *
 * 用于返回会话成员信息给前端
 *
 * @author ruoyi
 */
@Data
public class ImConversationMemberVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 会话ID */
    private Long conversationId;

    /** 用户ID */
    private Long userId;

    /** 用户昵称 */
    private String userNickname;

    /** 用户头像 */
    private String userAvatar;

    /** 未读消息数 */
    private Integer unreadCount;

    /** 最后阅读消息ID */
    private Long lastReadMessageId;

    /** 是否置顶 */
    private Integer isPinned;

    /** 是否免打扰 */
    private Integer isMuted;

    /** 是否删除 */
    private Integer isDeleted;

    /** 删除时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime deletedTime;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
