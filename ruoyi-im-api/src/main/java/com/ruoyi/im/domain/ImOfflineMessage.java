package com.ruoyi.im.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ruoyi.im.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 离线消息实体
 *
 * 用于存储用户离线期间的消息，用户上线后推送给客户端。
 * 参考野火IM的离线消息队列设计。
 *
 * @author ruoyi
 */
@TableName("im_offline_message")
@Data
@EqualsAndHashCode(callSuper = false)
public class ImOfflineMessage extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 状态：未拉取
     */
    public static final int STATUS_UNPULLED = 0;

    /**
     * 状态：已拉取
     */
    public static final int STATUS_PULLED = 1;

    /**
     * 状态：已删除
     */
    public static final int STATUS_DELETED = 2;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 接收用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 消息ID
     */
    @TableField("message_id")
    private Long messageId;

    /**
     * 会话ID
     */
    @TableField("conversation_id")
    private Long conversationId;

    /**
     * 消息类型（TEXT/FILE/IMAGE/VOICE/VIDEO等）
     */
    @TableField("message_type")
    private String messageType;

    /**
     * 消息内容（文本或JSON格式）
     */
    @TableField("message_content")
    private String messageContent;

    /**
     * 发送者ID
     */
    @TableField("sender_id")
    private Long senderId;

    /**
     * 发送者名称（快照，避免查询用户表）
     */
    @TableField("sender_name")
    private String senderName;

    /**
     * 状态：0=未拉取 1=已拉取 2=已删除
     */
    @TableField("status")
    private Integer status;

    /**
     * 拉取时间
     */
    @TableField("pull_time")
    private java.time.LocalDateTime pullTime;
}
