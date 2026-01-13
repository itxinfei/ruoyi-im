package com.ruoyi.im.dto.meeting;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 会议室预订请求
 *
 * @author ruoyi
 */
@Data
@Schema(description = "会议室预订请求")
public class ImMeetingBookingRequest {

    @NotNull(message = "会议室ID不能为空")
    @Schema(description = "会议室ID")
    private Long roomId;

    @NotBlank(message = "会议主题不能为空")
    @Schema(description = "会议主题")
    private String meetingTitle;

    @Schema(description = "会议类型（REGULAR常规 TRAINING培训 INTERVIEW面试 CLIENT客户）")
    private String meetingType = "REGULAR";

    @NotNull(message = "开始时间不能为空")
    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @NotNull(message = "结束时间不能为空")
    @Schema(description = "结束时间")
    private LocalDateTime endTime;

    @NotNull(message = "参会人数不能为空")
    @Schema(description = "参会人数")
    private Integer attendeeCount;

    @Schema(description = "参会人员ID列表")
    private List<Long> attendees;

    @Schema(description = "会议议程")
    private String agenda;

    @Schema(description = "所需资源列表")
    private List<String> resources;

    @Schema(description = "是否需要茶歇")
    private Boolean refreshments;

    @Schema(description = "是否需要录像")
    private Boolean recording;

    @Schema(description = "备注")
    private String remark;
}
