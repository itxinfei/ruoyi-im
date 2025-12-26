package com.ruoyi.im.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 会话成员对象 im_conversation_member
 * 
 * @author ruoyi
 * @date 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("im_conversation_member")
public class ImConversationMember extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 会话ID */
    @Excel(name = "会话ID")
    @TableField("conversation_id")
    private Long conversationId;

    /** 用户ID */
    @Excel(name = "用户ID")
    @TableField("user_id")
    private Long userId;

    /** 最后已读消息ID */
    @Excel(name = "最后已读消息ID")
    @TableField("last_read_message_id")
    private Long lastReadMessageId;

    /** 是否置顶（0否 1是） */
    @Excel(name = "是否置顶", readConverterExp = "0=否,1=是")
    @TableField("pinned")
    private Boolean pinned;

    /** 是否免打扰（0否 1是） */
    @Excel(name = "是否免打扰", readConverterExp = "0=否,1=是")
    @TableField("muted")
    private Boolean muted;

    /** 用户名（非数据库字段） */
    @TableField(exist = false)
    private String userName;

    /** 用户昵称（非数据库字段） */
    @TableField(exist = false)
    private String userNickName;

    /** 用户头像（非数据库字段） */
    @TableField(exist = false)
    private String userAvatar;

    /** 会话类型（非数据库字段） */
    @TableField(exist = false)
    private String conversationType;

    /** 会话标题（非数据库字段） */
    @TableField(exist = false)
    private String conversationTitle;

    /** 未读消息数量（非数据库字段） */
    @TableField(exist = false)
    private Integer unreadCount;
}