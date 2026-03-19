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
 * 任务实体类
 *
 * @author ruoyi
 */
@TableName("im_task")
@Data

public class ImTask implements Serializable {

    private static final long serialVersionUID = 1L;

    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("project_id")
    
    private Long projectId;

    @TableField("task_number")
    
    private String taskNumber;

    @TableField("title")
    
    private String title;

    @TableField("description")
    
    private String description;

    @TableField("creator_id")
    
    private Long creatorId;

    @TableField("assignee_id")
    
    private Long assigneeId;

    @TableField("priority")
    
    private Integer priority;

    @TableField("status")
    
    private String status;

    @TableField("task_type")
    
    private String taskType;

    @TableField("start_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    
    private LocalDate startDate;

    @TableField("due_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    
    private LocalDate dueDate;

    @TableField("estimated_hours")
    
    private Integer estimatedHours;

    @TableField("actual_hours")
    
    private Integer actualHours;

    @TableField("completion_percent")
    
    private Integer completionPercent;

    @TableField("parent_id")
    
    private Long parentId;

    @TableField("has_subtask")
    
    private Boolean hasSubtask;

    @TableField("attachments")
    
    private String attachments;

    @TableField("tags")
    
    private String tags;

    @TableField("followers")
    
    private String followers;

    @TableField("remind_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    
    private LocalDateTime remindTime;

    @TableField("remind_type")
    
    private String remindType;

    @TableField("repeat_type")
    
    private String repeatType;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    
    @TableField("completed_time")
    private LocalDateTime completedTime;

    @TableField("department_id")
    
    private Long departmentId;

    @TableField("remark")
    
    private String remark;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    
    @TableField("create_time")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    
    @TableField("update_time")
    private LocalDateTime updateTime;

    @TableField(exist = false)
    
    private String creatorName;

    @TableField(exist = false)
    
    private String assigneeName;

    @TableField(exist = false)
    
    private String assigneeAvatar;

    @TableField(exist = false)
    
    private Integer subtaskCount;

    @TableField(exist = false)
    
    private Integer completedSubtaskCount;

    @TableField(exist = false)
    
    private Integer commentCount;

    @TableField(exist = false)
    
    private Integer attachmentCount;

    @TableField(exist = false)
    
    private Boolean isOverdue;

    @TableField(exist = false)
    
    private Integer remainingDays;
}

