package com.ruoyi.im.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 消息收藏实体
 *
 * @author ruoyi
 */
@TableName("im_message_favorite")
@Data
@EqualsAndHashCode(callSuper = false)

public class ImMessageFavorite implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    
    private Long id;

    
    @TableField("user_id")
    private Long userId;

    
    @TableField("message_id")
    private Long messageId;

    
    @TableField("conversation_id")
    private Long conversationId;

    
    private String remark;

    
    private String tags;

    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    // ==================== 非数据库字段 ====================

    /** 消息内容（关联查询时填充） */
    @TableField(exist = false)
    private String messageContent;

    /** 发送者ID（关联查询时填充） */
    @TableField(exist = false)
    private Long senderId;

    /** 发送者名称（关联查询时填充） */
    @TableField(exist = false)
    private String senderName;

    /** 发送者头像（关联查询时填充） */
    @TableField(exist = false)
    private String senderAvatar;

    /** 消息类型（关联查询时填充） */
    @TableField(exist = false)
    private String messageType;

    /** 消息时间（关联查询时填充） */
    @TableField(exist = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime messageTime;
}

