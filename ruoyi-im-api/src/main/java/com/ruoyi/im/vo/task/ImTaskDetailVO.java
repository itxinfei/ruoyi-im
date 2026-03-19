package com.ruoyi.im.vo.task;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 任务详情视图对象
 *
 * @author ruoyi
 */
@Data

public class ImTaskDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    
    private Long id;

    
    private Long projectId;

    
    private String projectName;

    
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

    
    private Long parentId;

    
    private String parentTitle;

    
    private Boolean hasSubtask;

    
    private Integer subtaskCount;

    
    private Integer completedSubtaskCount;

    
    private Integer attachmentCount;

    
    private List<ImTaskVO> subtasks;

    
    private List<String> tags;

    
    private List<TaskAttachment> attachments;

    
    private List<TaskFollower> followers;

    
    private Integer commentCount;

    
    private List<TaskComment> comments;

    
    private List<TaskLog> logs;

    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime remindTime;

    
    private String remindType;

    
    private String remindTypeDisplay;

    
    private String repeatType;

    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime completedTime;

    
    private Long departmentId;

    
    private String departmentName;

    
    private String remark;

    
    private Boolean isOverdue;

    
    private Integer remainingDays;

    
    private Long creatorId;

    
    private String creatorName;

    
    private String creatorAvatar;

    
    private Long assigneeId;

    
    private String assigneeName;

    
    private String assigneeAvatar;

    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /**
     * 任务附件
     */
    @Data
    
    public static class TaskAttachment implements Serializable {
        
        private Long id;

        
        private String name;

        
        private String url;

        
        private Long size;

        
        private String fileType;

        
        private Long uploaderId;

        
        private String uploaderName;

        
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime uploadTime;
    }

    /**
     * 任务关注人
     */
    @Data
    
    public static class TaskFollower implements Serializable {
        
        private Long userId;

        
        private String userName;

        
        private String userAvatar;

        
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime followTime;
    }

    /**
     * 任务评论
     */
    @Data
    
    public static class TaskComment implements Serializable {
        
        private Long id;

        
        private String content;

        
        private Long commentatorId;

        
        private String commentatorName;

        
        private String commentatorAvatar;

        
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime commentTime;

        
        private Long replyToId;

        
        private String replyToName;
    }

    /**
     * 任务操作日志
     */
    @Data
    
    public static class TaskLog implements Serializable {
        
        private Long id;

        
        private String actionType;

        
        private String actionDisplay;

        
        private String content;

        
        private Long operatorId;

        
        private String operatorName;

        
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime operateTime;
    }
}

