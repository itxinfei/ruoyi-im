package com.ruoyi.im.dto.task;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "任务创建请求")
public class ImTaskCreateRequest {

    @Schema(description = "项目ID")
    private Long projectId;

    @Schema(description = "任务标题", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "任务标题不能为空")
    private String title;

    @Schema(description = "任务描述")
    private String description;

    @Schema(description = "负责人ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "负责人不能为空")
    private Long assigneeId;

    @Schema(description = "优先级（1=低 2=中 3=高 4=紧急）")
    private Integer priority = 2;

    @Schema(description = "任务类型（PERSONAL个人 TEAM团队 PROJECT项目）")
    private String taskType = "PERSONAL";

    @Schema(description = "开始日期")
    private LocalDate startDate;

    @Schema(description = "截止日期")
    private LocalDate dueDate;

    @Schema(description = "预估工时（小时）")
    private Integer estimatedHours;

    @Schema(description = "父任务ID（用于创建子任务）")
    private Long parentId;

    @Schema(description = "标签列表")
    private List<String> tags;

    @Schema(description = "关注人ID列表")
    private List<Long> followerIds;

    @Schema(description = "提醒时间")
    private LocalDateTime remindTime;

    @Schema(description = "提醒类型（NONE无 EMAIL邮件 SMS短信 DING钉钉）")
    private String remindType = "NONE";

    @Schema(description = "重复类型（NONE无 DAILY每日 WEEKLY每周 MONTHLY每月）")
    private String repeatType = "NONE";

    @Schema(description = "部门ID")
    private Long departmentId;

    @Schema(description = "备注")
    private String remark;
}
