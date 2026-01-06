package com.ruoyi.system.dto.statistics;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 数据统计查询请求DTO
 *
 * @author ruoyi
 */
@Data
@Schema(description = "数据统计查询请求")
public class ImStatisticsQueryRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "统计类型: user=用户统计, message=消息统计, group=群组统计, file=文件统计, system=系统统计")
    private String type;

    @Schema(description = "开始日期")
    private String startDate;

    @Schema(description = "结束日期")
    private String endDate;

    @Schema(description = "时间粒度: day=日, week=周, month=月")
    private String granularity;
}