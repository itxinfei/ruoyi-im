package com.ruoyi.im.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户会话状态实体
 * 
 * 用于存储用户与会话的关联状态，如置顶、免打扰、未读数、草稿等
 * 优化查询性能，避免关联查询 im_conversation_member
 *
 * @author ruoyi
 */
@TableName("im_user_session")
@Data
public class ImUserSession implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    @TableField("user_id")
    private Long userId;

    /** 会话ID */
    @TableField("conversation_id")
    private Long conversationId;

    /** 是否置顶: 0否 1是 */
    @TableField("is_pinned")
    private Integer isPinned;

    /** 置顶时间 */
    @TableField("pinned_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime pinnedTime;

    /** 是否免打扰: 0否 1是 */
    @TableField("is_muted")
    private Integer isMuted;

    /** 是否归档: 0否 1是 */
    @TableField("is_archived")
    private Integer isArchived;

    /** 未读消息数 */
    @TableField("unread_count")
    private Integer unreadCount;

    /** 最后已读消息ID */
    @TableField("last_read_message_id")
    private Long lastReadMessageId;

    /** 草稿内容 */
    @TableField("draft_content")
    private String draftContent;

    /** 草稿时间 */
    @TableField("draft_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime draftTime;

    /** 是否删除: 0否 1是 */
    @TableField("is_deleted")
    private Integer isDeleted;

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

    // ==================== 非数据库字段 ====================

    /** 会话信息（非数据库字段） */
    @TableField(exist = false)
    private ImConversation conversation;

    /** 最后一条消息（非数据库字段） */
    @TableField(exist = false)
    private ImMessage lastMessage;

    /** 会话名称（非数据库字段，用于前端显示） */
    @TableField(exist = false)
    private String conversationName;

    /** 会话头像（非数据库字段） */
    @TableField(exist = false)
    private String conversationAvatar;

    /** 会话类型（非数据库字段） */
    @TableField(exist = false)
    private String conversationType;

    /** 目标ID（非数据库字段） */
    @TableField(exist = false)
    private Long targetId;
}