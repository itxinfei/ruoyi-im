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
 * 音视频通话实体
 *
 * @author ruoyi
 */
@TableName("im_video_call")
@Data
@Schema(description = "音视频通话记录")
public class ImVideoCall implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "通话ID")
    @TableId(type = IdType.AUTO)
    private Long id;

    @Schema(description = "发起者ID")
    @TableField("caller_id")
    private Long callerId;

    @Schema(description = "接收者ID")
    @TableField("callee_id")
    private Long calleeId;

    @Schema(description = "通话类型：VOICE语音 VIDEO视频")
    @TableField("type")
    private String callType;

    @Schema(description = "状态：PENDING进行中 ACCEPTED接通 REJECTED拒绝 ENDED结束")
    private String status;

    @Schema(description = "开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("start_time")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("end_time")
    private LocalDateTime endTime;

    @Schema(description = "时长（秒）")
    private Integer duration;

    @Schema(description = "结束原因/拒绝原因")
    @TableField("reason")
    private String rejectReason;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("create_time")
    private LocalDateTime createTime;

    // ==================== 非数据库字段 ====================

    /** 会话ID（非数据库字段，用于业务关联） */
    @TableField(exist = false)
    private Long conversationId;

    /** 通话模式：PRIVATE单聊, GROUP群组多人（非数据库字段） */
    @TableField(exist = false)
    private String callMode;

    /** 最大参与者数量（非数据库字段） */
    @TableField(exist = false)
    private Integer maxParticipants;

    /** 当前参与者数量（非数据库字段） */
    @TableField(exist = false)
    private Integer currentParticipants;

    /** 房间号（非数据库字段，用于WebRTC房间标识） */
    @TableField(exist = false)
    private String roomId;
}
