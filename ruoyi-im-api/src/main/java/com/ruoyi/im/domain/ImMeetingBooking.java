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
 * 会议室预订实体类
 *
 * @author ruoyi
 */
@TableName("im_meeting_booking")
@Data
@Schema(description = "会议室预订")
public class ImMeetingBooking implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "预订ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("room_id")
    @Schema(description = "会议室ID")
    private Long roomId;

    @TableField("booking_user_id")
    @Schema(description = "预订人ID")
    private Long bookingUserId;

    @TableField("meeting_title")
    @Schema(description = "会议主题")
    private String meetingTitle;

    @TableField("meeting_type")
    @Schema(description = "会议类型（REGULAR常规 TRAINING培训 INTERVIEW面试 CLIENT客户）")
    private String meetingType;

    @TableField("start_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @TableField("end_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "结束时间")
    private LocalDateTime endTime;

    @TableField("attendee_count")
    @Schema(description = "参会人数")
    private Integer attendeeCount;

    @TableField("attendees")
    @Schema(description = "参会人员ID列表（JSON格式）")
    private String attendees;

    @TableField("agenda")
    @Schema(description = "会议议程")
    private String agenda;

    @TableField("resources")
    @Schema(description = "所需资源（JSON格式）")
    private String resources;

    @TableField("refreshments")
    @Schema(description = "是否需要茶歇")
    private Boolean refreshments;

    @TableField("recording")
    @Schema(description = "是否需要录像")
    private Boolean recording;

    @TableField("status")
    @Schema(description = "状态（PENDING待确认 CONFIRMED已确认 CANCELLED已取消 COMPLETED已完成）")
    private String status;

    @TableField("check_in_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "签到时间")
    private LocalDateTime checkInTime;

    @TableField("check_out_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "签退时间")
    private LocalDateTime checkOutTime;

    @TableField("reminder_sent")
    @Schema(description = "提醒是否已发送")
    private Boolean reminderSent;

    @TableField("feedback")
    @Schema(description = "会议反馈")
    private String feedback;

    @TableField("rating")
    @Schema(description = "评分（1-5分）")
    private Integer rating;

    @TableField("remark")
    @Schema(description = "备注")
    private String remark;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间")
    @TableField("create_time")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "更新时间")
    @TableField("update_time")
    private LocalDateTime updateTime;

    // ==================== 非数据库字段 ====================

    @TableField(exist = false)
    @Schema(description = "预订人姓名")
    private String bookingUserName;

    @TableField(exist = false)
    @Schema(description = "预订人头像")
    private String bookingUserAvatar;

    @TableField(exist = false)
    @Schema(description = "会议室名称")
    private String roomName;

    @TableField(exist = false)
    @Schema(description = "会议室位置")
    private String roomLocation;
}
