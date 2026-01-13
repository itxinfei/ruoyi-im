package com.ruoyi.im.dto.task;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "任务更新请求")
public class ImTaskUpdateRequest {

    @Schema(description = "任务ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

    @Schema(description = "任务标题")
    private String title;

    @Schema(description = "任务描述")
    private String description;

    @Schema(description = "负责人ID")
    private Long assigneeId;

    @Schema(description = "优先级（1=低 2=中 3=高 4=紧急）")
    private Integer priority;

    @Schema(description = "状态（PENDING待办 IN_PROGRESS进行中 COMPLETED已完成 CANCELLED已取消 BLOCKED阻塞）")
    private String status;

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

    @Schema(description = "标签列表")
    private List<String> tags;

    @Schema(description = "关注人ID列表")
    private List<Long> followerIds;

    @Schema(description = "提醒时间")
    private LocalDateTime remindTime;

    @Schema(description = "提醒类型（NONE无 EMAIL邮件 SMS短信 DING钉钉）")
    private String remindType;

    @Schema(description = "重复类型")
    private String repeatType;

    @Schema(description = "备注")
    private String remark;
}
