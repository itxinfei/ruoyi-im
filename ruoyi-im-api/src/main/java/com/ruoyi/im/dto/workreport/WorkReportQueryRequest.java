package com.ruoyi.im.dto.workreport;

import com.ruoyi.im.dto.BasePageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 查询工作日志请求
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "查询工作日志请求")
public class WorkReportQueryRequest extends BasePageRequest {

    @Schema(description = "日志类型")
    private String reportType;

    @Schema(description = "开始日期")
    private LocalDate startDate;

    @Schema(description = "结束日期")
    private LocalDate endDate;

    @Schema(description = "提交人ID")
    private Long userId;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "关键字搜索")
    private String keyword;
}
