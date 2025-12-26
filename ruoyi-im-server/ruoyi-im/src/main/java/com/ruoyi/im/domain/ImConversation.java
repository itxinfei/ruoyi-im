package com.ruoyi.im.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 会话对象 im_conversation
 * 
 * @author ruoyi
 * @date 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("im_conversation")
public class ImConversation extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 会话ID */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 会话类型（PRIVATE私聊 GROUP群聊） */
    @Excel(name = "会话类型", readConverterExp = "PRIVATE=私聊,GROUP=群聊")
    @TableField("type")
    private String type;

    /** 目标ID（私聊为好友ID，群聊为群组ID） */
    @Excel(name = "目标ID")
    @TableField("target_id")
    private Long targetId;

    /** 最后一条消息ID */
    @Excel(name = "最后一条消息ID")
    @TableField("last_message_id")
    private Long lastMessageId;

    /** 会话标题（非数据库字段） */
    @TableField(exist = false)
    private String title;

    /** 会话头像（非数据库字段） */
    @TableField(exist = false)
    private String avatar;

    /** 最后一条消息内容（非数据库字段） */
    @TableField(exist = false)
    private String lastMessageContent;

    /** 最后一条消息发送者（非数据库字段） */
    @TableField(exist = false)
    private String lastMessageSender;

    /** 最后一条消息时间（非数据库字段） */
    @TableField(exist = false)
    private String lastMessageTime;

    /** 未读消息数（非数据库字段） */
    @TableField(exist = false)
    private Integer unreadCount;

    /** 是否置顶（非数据库字段） */
    @TableField(exist = false)
    private Boolean pinned;

    /** 是否静音（非数据库字段） */
    @TableField(exist = false)
    private Boolean muted;

    /** 是否在线（非数据库字段） */
    @TableField(exist = false)
    private Boolean online;

    /** 成员数量（非数据库字段） */
    @TableField(exist = false)
    private Integer memberCount;
}