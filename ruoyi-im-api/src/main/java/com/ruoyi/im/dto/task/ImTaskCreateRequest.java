package com.ruoyi.im.dto.task;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 任务创建请求
 *
 * @author ruoyi
 */
@Data

public class ImTaskCreateRequest {

    
    private Long projectId;

    
    @NotBlank(message = "任务标题不能为空")
    private String title;

    
    private String description;

    
    @NotNull(message = "负责人不能为空")
    private Long assigneeId;

    
    private Integer priority = 2;

    
    private String taskType = "PERSONAL";

    
    private LocalDate startDate;

    
    private LocalDate dueDate;

    
    private Integer estimatedHours;

    
    private Long parentId;

    
    private List<String> tags;

    
    private List<Long> followerIds;

    
    private LocalDateTime remindTime;

    
    private String remindType = "NONE";

    
    private String repeatType = "NONE";

    
    private Long departmentId;

    
    private String remark;
}

