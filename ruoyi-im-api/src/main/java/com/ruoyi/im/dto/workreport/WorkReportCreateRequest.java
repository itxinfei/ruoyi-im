package com.ruoyi.im.dto.workreport;

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

public class WorkReportCreateRequest implements Serializable {

    @NotBlank(message = "日志类型不能为空")
    
    private String reportType;

    @NotNull(message = "报告日期不能为空")
    
    private LocalDate reportDate;

    @NotBlank(message = "工作内容不能为空")
    
    private String workContent;

    
    private String completionStatus = "COMPLETED";

    
    private String tomorrowPlan;

    
    private String issues;

    
    private String attachments;

    
    private BigDecimal workHours;

    
    private String visibility = "DEPARTMENT";

    
    private Boolean isDraft = false;
}

