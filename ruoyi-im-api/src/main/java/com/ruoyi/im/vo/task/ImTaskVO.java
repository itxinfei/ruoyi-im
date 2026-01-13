package com.ruoyi.im.vo.task;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "任务视图对象")
public class ImTaskVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "任务ID")
    private Long id;

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

    @Schema(description = "状态（PENDING待办 IN_PROGRESS进行中 COMPLETED已完成 CANCELLED已取消 BLOCKED阻塞）")
    private String status;

    @Schema(description = "状态显示名称")
    private String statusDisplay;

    @Schema(description = "任务类型（PERSONAL个人 TEAM团队 PROJECT项目）")
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

    @Schema(description = "是否有子任务")
    private Boolean hasSubtask;

    @Schema(description = "子任务数量")
    private Integer subtaskCount;

    @Schema(description = "已完成子任务数量")
    private Integer completedSubtaskCount;

    @Schema(description = "标签")
    private List<String> tags;

    @Schema(description = "评论数量")
    private Integer commentCount;

    @Schema(description = "附件数量")
    private Integer attachmentCount;

    @Schema(description = "是否逾期")
    private Boolean isOverdue;

    @Schema(description = "剩余天数")
    private Integer remainingDays;

    @Schema(description = "创建人ID")
    private Long creatorId;

    @Schema(description = "创建人姓名")
    private String creatorName;

    @Schema(description = "负责人ID")
    private Long assigneeId;

    @Schema(description = "负责人姓名")
    private String assigneeName;

    @Schema(description = "负责人头像")
    private String assigneeAvatar;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
