package com.ruoyi.im.vo.schedule;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.im.vo.schedule.ScheduleParticipantVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 日程事件详情VO
 */
@Data
@Schema(description = "日程事件详情")
public class ScheduleEventDetailVO implements Serializable {

    @Schema(description = "日程ID")
    private Long id;

    @Schema(description = "创建人ID")
    private Long userId;

    @Schema(description = "创建人姓名")
    private String userName;

    @Schema(description = "创建人头像")
    private String userAvatar;

    @Schema(description = "日程标题")
    private String title;

    @Schema(description = "日程描述")
    private String description;

    @Schema(description = "地点")
    private String location;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "结束时间")
    private LocalDateTime endTime;

    @Schema(description = "是否全天")
    private Boolean isAllDay;

    @Schema(description = "重复类型")
    private String recurrenceType;

    @Schema(description = "重复类型名称")
    private String recurrenceTypeName;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "重复结束日期")
    private LocalDate recurrenceEndDate;

    @Schema(description = "重复间隔")
    private Integer recurrenceInterval;

    @Schema(description = "重复的星期几")
    private String recurrenceDays;

    @Schema(description = "显示颜色")
    private String color;

    @Schema(description = "可见范围")
    private String visibility;

    @Schema(description = "可见范围名称")
    private String visibilityName;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "状态名称")
    private String statusName;

    @Schema(description = "提醒时间（分钟）")
    private Integer reminderMinutes;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "参与人列表")
    private List<ScheduleParticipantVO> participants;

    @Schema(description = "参与人数")
    private Integer participantCount;

    @Schema(description = "是否已接受（对于参与人）")
    private Boolean isAccepted;

    @Schema(description = "参与状态（对于参与人）")
    private String participantStatus;
}
