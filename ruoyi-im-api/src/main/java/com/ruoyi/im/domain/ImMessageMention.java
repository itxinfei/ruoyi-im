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
 * 消息@提及实体
 *
 * @author ruoyi
 */
@TableName("im_message_mention")
@Data
public class ImMessageMention implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 消息ID */
    @TableField("message_id")
    private Long messageId;

    /** 被@的用户ID */
    @TableField("mentioned_user_id")
    private Long mentionedUserId;

    /** @操作者ID */
    @TableField("mentioned_by")
    private Long mentionedBy;

    /** 提及类型：USER用户、ALL所有人 */
    @TableField("mention_type")
    private String mentionType;

    /** 是否已读：0未读、1已读 */
    @TableField("is_read")
    private Integer isRead;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
