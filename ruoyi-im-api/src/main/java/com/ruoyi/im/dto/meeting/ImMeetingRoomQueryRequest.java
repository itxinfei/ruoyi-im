package com.ruoyi.im.dto.meeting;

import com.ruoyi.im.common.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 会议室查询请求
 *
 * @author ruoyi
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "会议室查询请求")
public class ImMeetingRoomQueryRequest extends PageRequest {

    @Schema(description = "关键词（会议室名称、编号、位置）")
    private String keyword;

    @Schema(description = "所属部门ID")
    private Long departmentId;

    @Schema(description = "最小容纳人数")
    private Integer minCapacity;

    @Schema(description = "楼层")
    private Integer floor;

    @Schema(description = "状态（AVAILABLE可用 MAINTENANCE维护中 DISABLED停用）")
    private String status;

    @Schema(description = "是否有投影仪")
    private Boolean hasProjector;

    @Schema(description = "是否有白板")
    private Boolean hasWhiteboard;

    @Schema(description = "是否支持视频会议")
    private Boolean hasVideoConf;

    @Schema(description = "排序字段（capacity容量 createTime创建时间）")
    private String orderBy = "createTime";

    @Schema(description = "排序方向（asc升序 desc降序）")
    private String orderDirection = "desc";
}
