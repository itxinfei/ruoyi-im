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
 * IM会话实体
 *
 * @author ruoyi
 */
@TableName("im_conversation")
@Data
@EqualsAndHashCode(callSuper = true)
public class ImConversation extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 会话ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 会话类型：PRIVATE(单聊)、GROUP(群聊) */
    private String type;

    /** 目标ID：单聊为目标用户ID，群聊为群组ID */
    @TableField("target_id")
    private Long targetId;

    /** 会话名称 */
    private String name;

    /** 会话头像 */
    private String avatar;

    /** 最后消息ID */
    @TableField("last_message_id")
    private Long lastMessageId;

    /** 最后消息时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("last_message_time")
    private LocalDateTime lastMessageTime;

    /** 是否删除：0=否, 1=是 */
    @TableField("is_deleted")
    private Integer isDeleted;

    /** 删除时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("deleted_time")
    private LocalDateTime deletedTime;
}
