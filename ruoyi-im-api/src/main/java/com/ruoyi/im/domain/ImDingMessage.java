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
 * DING消息实体
 *
 * @author ruoyi
 */
@TableName("im_ding_message")
@Data
@Schema(description = "DING消息")
public class ImDingMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @Schema(description = "DING ID")
    private Long id;

    @Schema(description = "发送者ID")
    @TableField("sender_id")
    private Long senderId;

    @Schema(description = "DING内容")
    private String content;

    @Schema(description = "DING类型（APP应用 SMS短信 PHONE电话）")
    @TableField("ding_type")
    private String dingType;

    @Schema(description = "是否紧急（0否 1是）")
    @TableField("is_urgent")
    private Integer isUrgent;

    @Schema(description = "定时发送时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("schedule_time")
    private LocalDateTime scheduleTime;

    @Schema(description = "实际发送时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("send_time")
    private LocalDateTime sendTime;

    @Schema(description = "状态（DRAFT草稿 SENDING发送中 SENT已发送 CANCELLED已取消 FAILED失败）")
    private String status;

    @Schema(description = "是否需要回执（0否 1是）")
    @TableField("receipt_required")
    private Integer receiptRequired;

    @Schema(description = "总接收人数")
    @TableField("total_count")
    private Integer totalCount;

    @Schema(description = "已读人数")
    @TableField("read_count")
    private Integer readCount;

    @Schema(description = "已确认人数")
    @TableField("confirmed_count")
    private Integer confirmedCount;

    @Schema(description = "附件URL")
    private String attachment;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    // ==================== 非数据库字段 ====================

    /** 发送者名称（关联查询时填充） */
    @TableField(exist = false)
    private String senderName;

    /** 发送者头像（关联查询时填充） */
    @TableField(exist = false)
    private String senderAvatar;

    /** 接收者ID列表（用于创建DING时） */
    @TableField(exist = false)
    private Long[] receiverIds;
}
