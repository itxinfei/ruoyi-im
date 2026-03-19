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
 * 会议室预订实体类
 *
 * @author ruoyi
 */
@TableName("im_meeting_booking")
@Data

public class ImMeetingBooking implements Serializable {

    private static final long serialVersionUID = 1L;

    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("room_id")
    
    private Long roomId;

    @TableField("booking_user_id")
    
    private Long bookingUserId;

    @TableField("meeting_title")
    
    private String meetingTitle;

    @TableField("meeting_type")
    
    private String meetingType;

    @TableField("start_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    
    private LocalDateTime startTime;

    @TableField("end_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    
    private LocalDateTime endTime;

    @TableField("attendee_count")
    
    private Integer attendeeCount;

    @TableField("attendees")
    
    private String attendees;

    @TableField("agenda")
    
    private String agenda;

    @TableField("resources")
    
    private String resources;

    @TableField("refreshments")
    
    private Boolean refreshments;

    @TableField("recording")
    
    private Boolean recording;

    @TableField("status")
    
    private String status;

    @TableField("check_in_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    
    private LocalDateTime checkInTime;

    @TableField("check_out_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    
    private LocalDateTime checkOutTime;

    @TableField("reminder_sent")
    
    private Boolean reminderSent;

    @TableField("feedback")
    
    private String feedback;

    @TableField("rating")
    
    private Integer rating;

    @TableField("remark")
    
    private String remark;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    
    @TableField("create_time")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    
    @TableField("update_time")
    private LocalDateTime updateTime;

    // ==================== 非数据库字段 ====================

    @TableField(exist = false)
    
    private String bookingUserName;

    @TableField(exist = false)
    
    private String bookingUserAvatar;

    @TableField(exist = false)
    
    private String roomName;

    @TableField(exist = false)
    
    private String roomLocation;
}

