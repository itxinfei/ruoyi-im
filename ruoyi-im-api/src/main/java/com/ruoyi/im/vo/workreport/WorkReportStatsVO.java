package com.ruoyi.im.vo.workreport;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 工作日志统计VO
 *
 * @author ruoyi
 */
@Data
@Schema(description = "工作日志统计")
public class WorkReportStatsVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "总数量")
    private Integer totalCount;

    @Schema(description = "日报数量")
    private Integer dailyCount;

    @Schema(description = "周报数量")
    private Integer weeklyCount;

    @Schema(description = "月报数量")
    private Integer monthlyCount;

    @Schema(description = "已提交数量")
    private Integer submittedCount;

    @Schema(description = "草稿数量")
    private Integer draftCount;

    @Schema(description = "已审批数量")
    private Integer approvedCount;
}
