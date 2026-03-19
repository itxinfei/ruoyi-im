package com.ruoyi.im.vo.schedule;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.im.vo.schedule.ScheduleParticipantVO;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 日程事件详情VO
 */
@Data

public class ScheduleEventDetailVO implements Serializable {

    
    private Long id;

    
    private Long userId;

    
    private String userName;

    
    private String userAvatar;

    
    private String title;

    
    private String description;

    
    private String location;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    
    private LocalDateTime startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    
    private LocalDateTime endTime;

    
    private Boolean isAllDay;

    
    private String recurrenceType;

    
    private String recurrenceTypeName;

    @JsonFormat(pattern = "yyyy-MM-dd")
    
    private LocalDate recurrenceEndDate;

    
    private Integer recurrenceInterval;

    
    private String recurrenceDays;

    
    private String color;

    
    private String visibility;

    
    private String visibilityName;

    
    private String status;

    
    private String statusName;

    
    private Integer reminderMinutes;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    
    private LocalDateTime createTime;

    
    private List<ScheduleParticipantVO> participants;

    
    private Integer participantCount;

    
    private Boolean isAccepted;

    
    private String participantStatus;
}

