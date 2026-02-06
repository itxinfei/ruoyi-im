package com.ruoyi.im.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 聊天背景设置实体
 * 存储用户的聊天背景设置（全局或特定会话）
 *
 * @author ruoyi
 */
@TableName("im_chat_background")
@Data
@Schema(description = "聊天背景设置")
public class ImChatBackground implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;

    /**
     * 用户ID
     */
    @TableField("user_id")
    @Schema(description = "用户ID")
    private Long userId;

    /**
     * 会话ID（NULL表示全局背景）
     */
    @TableField("conversation_id")
    @Schema(description = "会话ID")
    private Long conversationId;

    /**
     * 背景类型：default=默认, color=纯色, image=图片
     */
    @TableField("background_type")
    @Schema(description = "背景类型")
    private String backgroundType;

    /**
     * 背景值（颜色值或图片URL）
     */
    @TableField("background_value")
    @Schema(description = "背景值")
    private String backgroundValue;

    /**
     * 创建时间
     */
    @TableField("create_time")
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField("update_time")
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
