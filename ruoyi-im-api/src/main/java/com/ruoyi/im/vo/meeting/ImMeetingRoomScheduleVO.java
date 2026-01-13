package com.ruoyi.im.vo.meeting;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 会议室日程视图对象
 *
 * @author ruoyi
 */
@Data
@Schema(description = "会议室日程视图对象")
public class ImMeetingRoomScheduleVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "会议室ID")
    private Long roomId;

    @Schema(description = "会议室名称")
    private String roomName;

    @Schema(description = "位置")
    private String location;

    @Schema(description = "容纳人数")
    private Integer capacity;

    @Schema(description = "日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private String date;

    @Schema(description = "预订列表")
    private List<ScheduleItem> schedules;

    /**
     * 预订项
     */
    @Data
    @Schema(description = "预订项")
    public static class ScheduleItem implements Serializable {
        @Schema(description = "预订ID")
        private Long bookingId;

        @Schema(description = "会议主题")
        private String meetingTitle;

        @Schema(description = "会议类型")
        private String meetingType;

        @Schema(description = "预订人姓名")
        private String bookingUserName;

        @Schema(description = "开始时间")
        @JsonFormat(pattern = "HH:mm")
        private String startTime;

        @Schema(description = "结束时间")
        @JsonFormat(pattern = "HH:mm")
        private String endTime;

        @Schema(description = "状态")
        private String status;

        @Schema(description = "参会人数")
        private Integer attendeeCount;
    }
}
