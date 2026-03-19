package com.ruoyi.im.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 日程事件实体类
 */
@TableName("im_schedule_event")
@Data

public class ImScheduleEvent implements Serializable {

    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    
    private Long userId;

    
    private String title;

    
    private String description;

    
    private String location;

    @TableField("start_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    
    private LocalDateTime startTime;

    @TableField("end_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    
    private LocalDateTime endTime;

    @TableField("is_all_day")
    
    private Integer isAllDay;

    @TableField("recurrence_type")
    
    private String recurrenceType;

    @TableField("recurrence_end_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    
    private LocalDate recurrenceEndDate;

    @TableField("recurrence_interval")
    
    private Integer recurrenceInterval;

    @TableField("recurrence_days")
    
    private String recurrenceDays;

    
    private String color;

    
    private String visibility;

    
    private String status;

    @TableField("reminder_minutes")
    
    private Integer reminderMinutes;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    
    private LocalDateTime updateTime;

    @TableField(exist = false)
    
    private String userName;

    @TableField(exist = false)
    
    private String userAvatar;

    @TableField(exist = false)
    
    private Integer participantCount;

    @TableField(exist = false)
    
    private Boolean isAccepted;

    @TableField(exist = false)
    
    private String participantStatus;
}

