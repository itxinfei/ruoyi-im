package com.ruoyi.im.dto.schedule;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 创建日程事件请求
 */
@Data

public class ScheduleEventCreateRequest implements Serializable {

    @NotBlank(message = "日程标题不能为空")
    
    private String title;

    
    private String description;

    
    private String location;

    @NotNull(message = "开始时间不能为空")
    
    private LocalDateTime startTime;

    @NotNull(message = "结束时间不能为空")
    
    private LocalDateTime endTime;

    
    private Boolean isAllDay = false;

    
    private String recurrenceType = "NONE";

    
    private LocalDate recurrenceEndDate;

    
    private Integer recurrenceInterval;

    
    private String recurrenceDays;

    
    private String color;

    
    private String visibility = "PRIVATE";

    
    private Integer reminderMinutes;

    
    private List<Long> participantIds;
}

