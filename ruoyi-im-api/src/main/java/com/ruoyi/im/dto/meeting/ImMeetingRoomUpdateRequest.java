package com.ruoyi.im.dto.meeting;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 更新会议室请求
 *
 * @author ruoyi
 */
@Data
@Schema(description = "更新会议室请求")
public class ImMeetingRoomUpdateRequest {

    @NotNull(message = "会议室ID不能为空")
    @Schema(description = "会议室ID")
    private Long id;

    @Schema(description = "会议室名称")
    private String roomName;

    @Schema(description = "会议室编号")
    private String roomNumber;

    @Schema(description = "所属部门ID")
    private Long departmentId;

    @Schema(description = "位置")
    private String location;

    @Schema(description = "楼层")
    private Integer floor;

    @Schema(description = "容纳人数")
    private Integer capacity;

    @Schema(description = "是否有投影仪")
    private Boolean hasProjector;

    @Schema(description = "是否有白板")
    private Boolean hasWhiteboard;

    @Schema(description = "是否支持视频会议")
    private Boolean hasVideoConf;

    @Schema(description = "是否有电话")
    private Boolean hasPhone;

    @Schema(description = "设施列表")
    private List<String> facilities;

    @Schema(description = "会议室图片URL列表")
    private List<String> photos;

    @Schema(description = "状态（AVAILABLE可用 MAINTENANCE维护中 DISABLED停用）")
    private String status;

    @Schema(description = "是否可预订")
    private Boolean isBookable;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "备注")
    private String remark;
}
