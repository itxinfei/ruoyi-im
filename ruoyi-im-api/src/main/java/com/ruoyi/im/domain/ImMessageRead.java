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
 * IM消息已读回执实体
 *
 * @author ruoyi
 */
@TableName("im_message_read")
@Data
public class ImMessageRead implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 消息ID */
    @TableField("message_id")
    private Long messageId;

    /** 已读用户ID */
    @TableField("user_id")
    private Long userId;

    /** 会话ID */
    @TableField("conversation_id")
    private Long conversationId;

    /** 已读时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("read_time")
    private LocalDateTime readTime;

    /** 已读类型 */
    @TableField("read_type")
    private String readType;

    /** 设备类型 */
    @TableField("device_type")
    private String deviceType;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
