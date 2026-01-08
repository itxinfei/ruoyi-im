package com.ruoyi.im.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.ruoyi.im.common.BaseEntity;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统通知实体
 *
 * @author ruoyi
 */
@TableName("im_system_notification")
@Data
@EqualsAndHashCode(callSuper = true)
public class ImSystemNotification extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 通知ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 接收者ID */
    @TableField("receiver_id")
    private Long receiverId;

    /** 发送者ID（系统通知可为NULL） */
    @TableField("sender_id")
    private Long senderId;

    /** 通知类型（SYSTEM系统 APPROVAL审批 MESSAGE消息 GROUP群组 FRIEND好友） */
    private String type;

    /** 通知标题 */
    private String title;

    /** 通知内容 */
    private String content;

    /** 关联类型（USER MESSAGE GROUP CONVERSATION APPROVAL） */
    @TableField("related_type")
    private String relatedType;

    /** 关联ID */
    @TableField("related_id")
    private Long relatedId;

    /** 是否已读：0=否, 1=是 */
    @TableField("is_read")
    private Integer isRead;

    /** 阅读时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("read_time")
    private LocalDateTime readTime;
}
