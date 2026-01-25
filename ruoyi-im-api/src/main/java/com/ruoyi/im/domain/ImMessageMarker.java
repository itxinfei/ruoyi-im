package com.ruoyi.im.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 消息标记实体
 * 用于标记重要消息、设置待办提醒等
 *
 * @author ruoyi
 */
@TableName("im_message_marker")
@Data
@Schema(description = "消息标记")
public class ImMessageMarker implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 标记ID
     */
    @TableId(type = IdType.AUTO)
    @Schema(description = "标记ID")
    private Long id;

    /**
     * 消息ID
     */
    @Schema(description = "消息ID")
    @TableField("message_id")
    private Long messageId;

    /**
     * 会话ID
     */
    @Schema(description = "会话ID")
    @TableField("conversation_id")
    private Long conversationId;

    /**
     * 用户ID
     */
    @Schema(description = "用户ID")
    @TableField("user_id")
    private Long userId;

    /**
     * 标记类型：FLAG标记, TODO待办, IMPORTANT重要
     */
    @Schema(description = "标记类型")
    @TableField("marker_type")
    private String markerType;

    /**
     * 标记颜色
     */
    @Schema(description = "标记颜色")
    @TableField("color")
    private String color;

    /**
     * 待办提醒时间
     */
    @Schema(description = "提醒时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("remind_time")
    private LocalDateTime remindTime;

    /**
     * 待办状态：PENDING待办, DONE已完成
     */
    @Schema(description = "待办状态")
    @TableField("todo_status")
    private String todoStatus;

    /**
     * 备注
     */
    @Schema(description = "备注")
    @TableField("remark")
    private String remark;

    /**
     * 完成时间
     */
    @Schema(description = "完成时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("done_time")
    private LocalDateTime doneTime;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("update_time")
    private LocalDateTime updateTime;
}
