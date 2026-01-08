package com.ruoyi.im.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "消息收藏")
public class ImMessageFavorite implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @Schema(description = "收藏ID")
    private Long id;

    @Schema(description = "用户ID")
    @TableField("user_id")
    private Long userId;

    @Schema(description = "消息ID")
    @TableField("message_id")
    private Long messageId;

    @Schema(description = "会话ID")
    @TableField("conversation_id")
    private Long conversationId;

    @Schema(description = "收藏备注")
    private String remark;

    @Schema(description = "标签（多个标签用逗号分隔）")
    private String tags;

    @Schema(description = "收藏时间")
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
