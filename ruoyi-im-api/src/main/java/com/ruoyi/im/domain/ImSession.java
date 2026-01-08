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
 * IM会话实体（视图）
 *
 * im_session是一个数据库视图，不是表
 * 该实体已废弃，请使用ImConversation和ImConversationMember
 *
 * @deprecated 使用 {@link ImConversation} 和 {@link ImConversationMember} 替代
 * @author ruoyi
 */
@Deprecated
@TableName("im_session")
@Data
public class ImSession implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 会话ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 会话类型 */
    private String type;

    /** 用户名称 */
    @TableField("user_name")
    private String userName;

    /** 目标名称 */
    @TableField("target_name")
    private String targetName;

    /** 目标ID */
    @TableField("target_id")
    private Long targetId;

    /** 最后消息ID */
    @TableField("last_message_id")
    private Long lastMessageId;

    /** 最后消息 */
    @TableField("last_message")
    private String lastMessage;

    /** 未读消息数 */
    @TableField("unread_count")
    private Integer unreadCount;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    // ==================== 非数据库字段（用于业务逻辑） ====================

    /** 用户ID */
    @TableField(exist = false)
    private Long userId;

    /** 会话名称 */
    @TableField(exist = false)
    private String name;

    /** 是否置顶：0=否, 1=是 */
    @TableField(exist = false)
    private Integer isPinned;

    /** 是否免打扰：0=否, 1=是 */
    @TableField(exist = false)
    private Integer isMuted;
}
