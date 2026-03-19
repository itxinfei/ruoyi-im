package com.ruoyi.im.dto.task;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 任务更新请求
 *
 * @author ruoyi
 */
@Data

public class ImTaskUpdateRequest {

    
    private Long id;

    
    private String title;

    
    private String description;

    
    private Long assigneeId;

    
    private Integer priority;

    
    private String status;

    
    private LocalDate startDate;

    
    private LocalDate dueDate;

    
    private Integer estimatedHours;

    
    private Integer actualHours;

    
    private Integer completionPercent;

    
    private List<String> tags;

    
    private List<Long> followerIds;

    
    private LocalDateTime remindTime;

    
    private String remindType;

    
    private String repeatType;

    
    private String remark;
}

