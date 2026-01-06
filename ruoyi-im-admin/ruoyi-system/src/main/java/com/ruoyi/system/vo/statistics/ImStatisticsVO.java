package com.ruoyi.system.vo.statistics;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 数据统计VO
 *
 * @author ruoyi
 */
@Data
@Schema(description = "统计数据")
public class ImStatisticsVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "用户总数")
    private Integer totalUsers;

    @Schema(description = "活跃用户数")
    private Integer activeUsers;

    @Schema(description = "今日登录数")
    private Integer todayLogins;

    @Schema(description = "今日消息数")
    private Integer todayMessages;

    @Schema(description = "本周消息数")
    private Integer weekMessages;

    @Schema(description = "本月消息数")
    private Integer monthMessages;

    @Schema(description = "消息增长率（%）")
    private Double messageGrowth;

    @Schema(description = "群组总数")
    private Integer totalGroups;

    @Schema(description = "活跃群组数")
    private Integer activeGroups;

    @Schema(description = "文件总数")
    private Integer totalFiles;

    @Schema(description = "总存储大小（MB）")
    private Long totalStorageSize;
}