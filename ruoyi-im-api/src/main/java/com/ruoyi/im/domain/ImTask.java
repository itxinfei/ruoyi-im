package com.ruoyi.im.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "任务")
public class ImTask implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "任务ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("project_id")
    @Schema(description = "项目ID（可选，用于项目任务管理）")
    private Long projectId;

    @TableField("task_number")
    @Schema(description = "任务编号")
    private String taskNumber;

    @TableField("title")
    @Schema(description = "任务标题")
    private String title;

    @TableField("description")
    @Schema(description = "任务描述")
    private String description;

    @TableField("creator_id")
    @Schema(description = "创建人ID")
    private Long creatorId;

    @TableField("assignee_id")
    @Schema(description = "负责人ID")
    private Long assigneeId;

    @TableField("priority")
    @Schema(description = "优先级（1=低 2=中 3=高 4=紧急）")
    private Integer priority;

    @TableField("status")
    @Schema(description = "状态（PENDING待办 IN_PROGRESS进行中 COMPLETED已完成 CANCELLED已取消 BLOCKED阻塞）")
    private String status;

    @TableField("task_type")
    @Schema(description = "任务类型（PERSONAL个人 TEAM团队 PROJECT项目）")
    private String taskType;

    @TableField("start_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "开始日期")
    private LocalDate startDate;

    @TableField("due_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "截止日期")
    private LocalDate dueDate;

    @TableField("estimated_hours")
    @Schema(description = "预估工时（小时）")
    private Integer estimatedHours;

    @TableField("actual_hours")
    @Schema(description = "实际工时（小时）")
    private Integer actualHours;

    @TableField("completion_percent")
    @Schema(description = "完成进度（0-100）")
    private Integer completionPercent;

    @TableField("parent_id")
    @Schema(description = "父任务ID（用于子任务）")
    private Long parentId;

    @TableField("has_subtask")
    @Schema(description = "是否有子任务")
    private Boolean hasSubtask;

    @TableField("attachments")
    @Schema(description = "附件（JSON格式存储附件列表）")
    private String attachments;

    @TableField("tags")
    @Schema(description = "标签（多个标签用逗号分隔）")
    private String tags;

    @TableField("followers")
    @Schema(description = "关注人ID列表（JSON数组）")
    private String followers;

    @TableField("remind_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "提醒时间")
    private LocalDateTime remindTime;

    @TableField("remind_type")
    @Schema(description = "提醒类型（NONE无 EMAIL邮件 SMS短信 DING钉钉）")
    private String remindType;

    @TableField("repeat_type")
    @Schema(description = "重复类型（NONE无 DAILY每日 WEEKLY每周 MONTHLY每月）")
    private String repeatType;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "完成时间")
    @TableField("completed_time")
    private LocalDateTime completedTime;

    @TableField("department_id")
    @Schema(description = "部门ID")
    private Long departmentId;

    @TableField("remark")
    @Schema(description = "备注")
    private String remark;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间")
    @TableField("create_time")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "更新时间")
    @TableField("update_time")
    private LocalDateTime updateTime;

    @TableField(exist = false)
    @Schema(description = "创建人姓名")
    private String creatorName;

    @TableField(exist = false)
    @Schema(description = "负责人姓名")
    private String assigneeName;

    @TableField(exist = false)
    @Schema(description = "负责人头像")
    private String assigneeAvatar;

    @TableField(exist = false)
    @Schema(description = "子任务数量")
    private Integer subtaskCount;

    @TableField(exist = false)
    @Schema(description = "已完成子任务数量")
    private Integer completedSubtaskCount;

    @TableField(exist = false)
    @Schema(description = "评论数量")
    private Integer commentCount;

    @TableField(exist = false)
    @Schema(description = "附件数量")
    private Integer attachmentCount;

    @TableField(exist = false)
    @Schema(description = "是否逾期")
    private Boolean isOverdue;

    @TableField(exist = false)
    @Schema(description = "剩余天数")
    private Integer remainingDays;
}
