package com.ruoyi.im.dto.schedule;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 创建日程事件请求
 */
@Data
@Schema(description = "创建日程事件请求")
public class ScheduleEventCreateRequest implements Serializable {

    @NotBlank(message = "日程标题不能为空")
    @Schema(description = "日程标题")
    private String title;

    @Schema(description = "日程描述")
    private String description;

    @Schema(description = "地点")
    private String location;

    @NotNull(message = "开始时间不能为空")
    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @NotNull(message = "结束时间不能为空")
    @Schema(description = "结束时间")
    private LocalDateTime endTime;

    @Schema(description = "是否全天")
    private Boolean isAllDay = false;

    @Schema(description = "重复类型（NONE无 DAILY每天 WEEKLY每周 MONTHLY每月）")
    private String recurrenceType = "NONE";

    @Schema(description = "重复结束日期")
    private LocalDate recurrenceEndDate;

    @Schema(description = "重复间隔")
    private Integer recurrenceInterval;

    @Schema(description = "重复的星期几（1-7，逗号分隔）")
    private String recurrenceDays;

    @Schema(description = "显示颜色")
    private String color;

    @Schema(description = "可见范围（PRIVATE私有 DEPARTMENT部门 PUBLIC公开）")
    private String visibility = "PRIVATE";

    @Schema(description = "提醒时间（分钟，事件开始前多少分钟提醒）")
    private Integer reminderMinutes;

    @Schema(description = "参与人ID列表")
    private List<Long> participantIds;
}
