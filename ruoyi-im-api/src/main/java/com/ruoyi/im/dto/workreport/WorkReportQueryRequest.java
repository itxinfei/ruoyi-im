package com.ruoyi.im.dto.workreport;

import com.ruoyi.im.dto.BasePageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 查询工作日志请求
 */
@Data
@EqualsAndHashCode(callSuper = true)

public class WorkReportQueryRequest extends BasePageRequest {

    
    private String reportType;

    
    private LocalDate startDate;

    
    private LocalDate endDate;

    
    private Long userId;

    
    private String status;

    
    private String keyword;
}

