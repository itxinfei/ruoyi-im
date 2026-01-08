package com.ruoyi.im.dto.workreport;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 创建工作日志请求
 */
@Data
@Schema(description = "创建工作日志请求")
public class WorkReportCreateRequest implements Serializable {

    @NotBlank(message = "日志类型不能为空")
    @Schema(description = "日志类型（DAILY日报 WEEKLY周报 MONTHLY月报）")
    private String reportType;

    @NotNull(message = "报告日期不能为空")
    @Schema(description = "报告日期")
    private LocalDate reportDate;

    @NotBlank(message = "工作内容不能为空")
    @Schema(description = "工作内容")
    private String workContent;

    @Schema(description = "完成状态（COMPLETED已完成 IN_PROGRESS进行中 PENDING待处理）")
    private String completionStatus = "COMPLETED";

    @Schema(description = "明日计划")
    private String tomorrowPlan;

    @Schema(description = "遇到的问题")
    private String issues;

    @Schema(description = "附件（多个附件用逗号分隔）")
    private String attachments;

    @Schema(description = "工作时长（小时）")
    private BigDecimal workHours;

    @Schema(description = "可见范围（PRIVATE私有 DEPARTMENT部门 PUBLIC公开）")
    private String visibility = "DEPARTMENT";

    @Schema(description = "是否为草稿")
    private Boolean isDraft = false;
}
