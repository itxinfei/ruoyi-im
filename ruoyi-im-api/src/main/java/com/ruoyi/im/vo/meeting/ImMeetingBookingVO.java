package com.ruoyi.im.vo.meeting;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 会议室预订视图对象
 *
 * @author ruoyi
 */
@Data
@Schema(description = "会议室预订视图对象")
public class ImMeetingBookingVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "预订ID")
    private Long id;

    @Schema(description = "会议室ID")
    private Long roomId;

    @Schema(description = "会议室名称")
    private String roomName;

    @Schema(description = "会议室位置")
    private String roomLocation;

    @Schema(description = "预订人ID")
    private Long bookingUserId;

    @Schema(description = "预订人姓名")
    private String bookingUserName;

    @Schema(description = "预订人头像")
    private String bookingUserAvatar;

    @Schema(description = "会议主题")
    private String meetingTitle;

    @Schema(description = "会议类型")
    private String meetingType;

    @Schema(description = "会议类型显示名称")
    private String meetingTypeDisplay;

    @Schema(description = "开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    @Schema(description = "参会人数")
    private Integer attendeeCount;

    @Schema(description = "参会人员列表")
    private List<Attendee> attendees;

    @Schema(description = "会议议程")
    private String agenda;

    @Schema(description = "所需资源")
    private List<String> resources;

    @Schema(description = "是否需要茶歇")
    private Boolean refreshments;

    @Schema(description = "是否需要录像")
    private Boolean recording;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "状态显示名称")
    private String statusDisplay;

    @Schema(description = "签到时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime checkInTime;

    @Schema(description = "签退时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime checkOutTime;

    @Schema(description = "是否已签到")
    private Boolean isCheckedIn;

    @Schema(description = "是否已签退")
    private Boolean isCheckedOut;

    @Schema(description = "是否已过期")
    private Boolean isExpired;

    @Schema(description = "会议反馈")
    private String feedback;

    @Schema(description = "评分")
    private Integer rating;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /**
     * 参会人员
     */
    @Data
    @Schema(description = "参会人员")
    public static class Attendee implements Serializable {
        @Schema(description = "用户ID")
        private Long userId;

        @Schema(description = "用户姓名")
        private String userName;

        @Schema(description = "用户头像")
        private String userAvatar;

        @Schema(description = "是否已签到")
        private Boolean isCheckedIn;

        @Schema(description = "签到时间")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime checkInTime;
    }
}
