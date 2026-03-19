package com.ruoyi.im.vo.task;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 任务视图对象
 *
 * @author ruoyi
 */
@Data

public class ImTaskVO implements Serializable {

    private static final long serialVersionUID = 1L;

    
    private Long id;

    
    private String taskNumber;

    
    private String title;

    
    private String description;

    
    private Integer priority;

    
    private String priorityDisplay;

    
    private String status;

    
    private String statusDisplay;

    
    private String taskType;

    
    private String taskTypeDisplay;

    
    private LocalDate startDate;

    
    private LocalDate dueDate;

    
    private Integer estimatedHours;

    
    private Integer actualHours;

    
    private Integer completionPercent;

    
    private Boolean hasSubtask;

    
    private Integer subtaskCount;

    
    private Integer completedSubtaskCount;

    
    private List<String> tags;

    
    private Integer commentCount;

    
    private Integer attachmentCount;

    
    private Boolean isOverdue;

    
    private Integer remainingDays;

    
    private Long creatorId;

    
    private String creatorName;

    
    private Long assigneeId;

    
    private String assigneeName;

    
    private String assigneeAvatar;

    
    private LocalDateTime createTime;

    
    private LocalDateTime updateTime;
}

