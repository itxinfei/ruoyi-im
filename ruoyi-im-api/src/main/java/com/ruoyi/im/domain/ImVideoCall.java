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
 * 视频通话实体
 *
 * @author ruoyi
 */
@TableName("im_video_call")
@Data
public class ImVideoCall implements Serializable {

    private static final long serialVersionUID = 1L;

    /** ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 发起者ID */
    @TableField("caller_id")
    private Long callerId;

    /** 接收者ID */
    @TableField("callee_id")
    private Long calleeId;

    /** 会话ID（关联的聊天会话） */
    @TableField("conversation_id")
    private Long conversationId;

    /** 通话类型：VIDEO视频, VOICE语音 */
    private String callType;

    /** 通话状态：CALLING呼叫中, CONNECTED通话中, ENDED已结束, REJECTED拒绝, TIMEOUT超时 */
    private String status;

    /** 开始时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    /** 结束时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("end_time")
    private LocalDateTime endTime;

    /** 通话时长（秒） */
    @TableField("duration")
    private Integer duration;

    /** 拒绝原因 */
    @TableField("reject_reason")
    private String rejectReason;
}
