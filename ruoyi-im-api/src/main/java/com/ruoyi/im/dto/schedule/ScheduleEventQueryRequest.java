package com.ruoyi.im.dto.schedule;

import com.ruoyi.im.dto.BasePageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 查询日程事件请求
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "查询日程事件请求")
public class ScheduleEventQueryRequest extends BasePageRequest {

    @Schema(description = "开始时间（查询范围）")
    private LocalDateTime startTime;

    @Schema(description = "结束时间（查询范围）")
    private LocalDateTime endTime;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "关键字搜索")
    private String keyword;

    @Schema(description = "是否只查询我参与的")
    private Boolean onlyParticipated = false;
}
