package com.ruoyi.im.vo.task;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "任务详情视图对象")
public class ImTaskDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "任务ID")
    private Long id;

    @Schema(description = "项目ID")
    private Long projectId;

    @Schema(description = "项目名称")
    private String projectName;

    @Schema(description = "任务编号")
    private String taskNumber;

    @Schema(description = "任务标题")
    private String title;

    @Schema(description = "任务描述")
    private String description;

    @Schema(description = "优先级（1=低 2=中 3=高 4=紧急）")
    private Integer priority;

    @Schema(description = "优先级显示名称")
    private String priorityDisplay;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "状态显示名称")
    private String statusDisplay;

    @Schema(description = "任务类型")
    private String taskType;

    @Schema(description = "任务类型显示名称")
    private String taskTypeDisplay;

    @Schema(description = "开始日期")
    private LocalDate startDate;

    @Schema(description = "截止日期")
    private LocalDate dueDate;

    @Schema(description = "预估工时（小时）")
    private Integer estimatedHours;

    @Schema(description = "实际工时（小时）")
    private Integer actualHours;

    @Schema(description = "完成进度（0-100）")
    private Integer completionPercent;

    @Schema(description = "父任务ID")
    private Long parentId;

    @Schema(description = "父任务标题")
    private String parentTitle;

    @Schema(description = "是否有子任务")
    private Boolean hasSubtask;

    @Schema(description = "子任务数量")
    private Integer subtaskCount;

    @Schema(description = "已完成子任务数量")
    private Integer completedSubtaskCount;

    @Schema(description = "附件数量")
    private Integer attachmentCount;

    @Schema(description = "子任务列表")
    private List<ImTaskVO> subtasks;

    @Schema(description = "标签")
    private List<String> tags;

    @Schema(description = "附件列表")
    private List<TaskAttachment> attachments;

    @Schema(description = "关注人列表")
    private List<TaskFollower> followers;

    @Schema(description = "评论数量")
    private Integer commentCount;

    @Schema(description = "评论列表")
    private List<TaskComment> comments;

    @Schema(description = "操作日志列表")
    private List<TaskLog> logs;

    @Schema(description = "提醒时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime remindTime;

    @Schema(description = "提醒类型")
    private String remindType;

    @Schema(description = "提醒类型显示名称")
    private String remindTypeDisplay;

    @Schema(description = "重复类型")
    private String repeatType;

    @Schema(description = "完成时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime completedTime;

    @Schema(description = "部门ID")
    private Long departmentId;

    @Schema(description = "部门名称")
    private String departmentName;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "是否逾期")
    private Boolean isOverdue;

    @Schema(description = "剩余天数")
    private Integer remainingDays;

    @Schema(description = "创建人ID")
    private Long creatorId;

    @Schema(description = "创建人姓名")
    private String creatorName;

    @Schema(description = "创建人头像")
    private String creatorAvatar;

    @Schema(description = "负责人ID")
    private Long assigneeId;

    @Schema(description = "负责人姓名")
    private String assigneeName;

    @Schema(description = "负责人头像")
    private String assigneeAvatar;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /**
     * 任务附件
     */
    @Data
    @Schema(description = "任务附件")
    public static class TaskAttachment implements Serializable {
        @Schema(description = "附件ID")
        private Long id;

        @Schema(description = "附件名称")
        private String name;

        @Schema(description = "附件URL")
        private String url;

        @Schema(description = "文件大小（字节）")
        private Long size;

        @Schema(description = "文件类型")
        private String fileType;

        @Schema(description = "上传人ID")
        private Long uploaderId;

        @Schema(description = "上传人姓名")
        private String uploaderName;

        @Schema(description = "上传时间")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime uploadTime;
    }

    /**
     * 任务关注人
     */
    @Data
    @Schema(description = "任务关注人")
    public static class TaskFollower implements Serializable {
        @Schema(description = "用户ID")
        private Long userId;

        @Schema(description = "用户姓名")
        private String userName;

        @Schema(description = "用户头像")
        private String userAvatar;

        @Schema(description = "关注时间")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime followTime;
    }

    /**
     * 任务评论
     */
    @Data
    @Schema(description = "任务评论")
    public static class TaskComment implements Serializable {
        @Schema(description = "评论ID")
        private Long id;

        @Schema(description = "评论内容")
        private String content;

        @Schema(description = "评论人ID")
        private Long commentatorId;

        @Schema(description = "评论人姓名")
        private String commentatorName;

        @Schema(description = "评论人头像")
        private String commentatorAvatar;

        @Schema(description = "评论时间")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime commentTime;

        @Schema(description = "回复的评论ID")
        private Long replyToId;

        @Schema(description = "回复的评论人姓名")
        private String replyToName;
    }

    /**
     * 任务操作日志
     */
    @Data
    @Schema(description = "任务操作日志")
    public static class TaskLog implements Serializable {
        @Schema(description = "日志ID")
        private Long id;

        @Schema(description = "操作类型")
        private String actionType;

        @Schema(description = "操作类型显示名称")
        private String actionDisplay;

        @Schema(description = "操作内容")
        private String content;

        @Schema(description = "操作人ID")
        private Long operatorId;

        @Schema(description = "操作人姓名")
        private String operatorName;

        @Schema(description = "操作时间")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime operateTime;
    }
}
