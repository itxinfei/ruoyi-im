package com.ruoyi.im.vo.meeting;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 会议室视图对象
 *
 * @author ruoyi
 */
@Data
@Schema(description = "会议室视图对象")
public class ImMeetingRoomVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "会议室ID")
    private Long id;

    @Schema(description = "会议室名称")
    private String roomName;

    @Schema(description = "会议室编号")
    private String roomNumber;

    @Schema(description = "所属部门ID")
    private Long departmentId;

    @Schema(description = "部门名称")
    private String departmentName;

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

    @Schema(description = "会议室图片")
    private List<String> photos;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "状态显示名称")
    private String statusDisplay;

    @Schema(description = "是否可预订")
    private Boolean isBookable;

    @Schema(description = "是否被占用")
    private Boolean isOccupied;

    @Schema(description = "当前预订数")
    private Integer bookingCount;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
