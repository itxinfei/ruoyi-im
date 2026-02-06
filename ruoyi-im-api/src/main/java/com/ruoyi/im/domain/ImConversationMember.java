package com.ruoyi.im.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ruoyi.im.common.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * IM会话成员实体
 *
 * 用于存储用户在会话中的状态信息，如未读消息数、是否置顶、是否免打扰等
 *
 * @author ruoyi
 */
@TableName("im_conversation_member")
@Data
@EqualsAndHashCode(callSuper = false)
public class ImConversationMember extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 关系ID，主键，自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 会话ID */
    @TableField("conversation_id")
    private Long conversationId;

    /** 用户ID */
    @TableField("user_id")
    private Long userId;

    /** 群内昵称 */
    private String nickname;

    /** 角色 */
    private String role;

    /** 未读消息数 */
    @TableField("unread_count")
    private Integer unreadCount;

    /** 是否置顶 */
    @TableField("is_pinned")
    private Integer isPinned;

    /** 是否免打扰 */
    @TableField("is_muted")
    private Integer isMuted;

    /** 最后已读消息ID */
    @TableField("last_read_message_id")
    private Long lastReadMessageId;

    /** 最后已读时间 */
    @TableField("last_read_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastReadTime;

    /** 是否删除：0=否, 1=是 */
    @TableField("is_deleted")
    private Integer isDeleted;

    /** 是否归档：0=否, 1=是 */
    @TableField("is_archived")
    private Integer isArchived;

    /** 删除时间 */
    @TableField("deleted_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime deletedTime;

    /** 创建时间 */
    @TableField("create_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField("update_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

}