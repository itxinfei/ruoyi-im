package com.ruoyi.web.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 会话成员实体
 *
 * 对应数据库表 im_conversation_member
 * 实际数据库字段：id, conversation_id, user_id, nickname, role, unread_count,
 * is_pinned, is_muted, last_read_message_id, last_read_time, is_deleted, deleted_time, create_time, update_time
 *
 * @author ruoyi
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImConversationMember implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 成员ID，主键
     */
    private Long id;

    /**
     * 会话ID
     */
    private Long conversationId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 群昵称
     */
    private String nickname;

    /**
     * 角色: OWNER所有者 MEMBER成员
     */
    private String role;

    /**
     * 未读数
     */
    private Integer unreadCount;

    /**
     * 是否置顶: 0否 1是
     */
    private Integer isPinned;

    /**
     * 是否免打扰: 0否 1是
     */
    private Integer isMuted;

    /**
     * 最后已读消息ID
     */
    private Long lastReadMessageId;

    /**
     * 最后已读时间
     */
    private LocalDateTime lastReadTime;

    /**
     * 是否删除: 0否 1是
     */
    private Integer isDeleted;

    /**
     * 删除时间
     */
    private LocalDateTime deletedTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    // ========== 关联查询字段（非数据库字段） ==========

    /**
     * 用户名
     */
    private String userName;

    /**
     * 用户昵称
     */
    private String userNickname;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 会话名称
     */
    private String conversationName;

}
