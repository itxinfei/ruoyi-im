package com.ruoyi.im.vo.meeting;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 视频会议VO
 *
 * @author ruoyi
 */
@Data
@Schema(description = "视频会议信息")
public class ImVideoMeetingVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "会议ID")
    private Long id;

    @Schema(description = "会议标题")
    private String title;

    @Schema(description = "会议描述")
    private String description;

    @Schema(description = "发起人ID")
    private Long hostId;

    @Schema(description = "发起人名称")
    private String hostName;

    @Schema(description = "发起人头像")
    private String hostAvatar;

    @Schema(description = "会议类型：MEETING会议, WEBINAR网络研讨会")
    private String meetingType;

    @Schema(description = "会议状态：SCHEDULED预定, IN_PROGRESS进行中, ENDED已结束, CANCELLED已取消")
    private String status;

    @Schema(description = "预定开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime scheduledStartTime;

    @Schema(description = "预定结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime scheduledEndTime;

    @Schema(description = "实际开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime actualStartTime;

    @Schema(description = "实际结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime actualEndTime;

    @Schema(description = "会议时长（分钟）")
    private Integer duration;

    @Schema(description = "最大参与人数")
    private Integer maxParticipants;

    @Schema(description = "当前参与人数")
    private Integer currentParticipants;

    @Schema(description = "是否需要密码")
    private Boolean requirePassword;

    @Schema(description = "会议链接")
    private String meetingLink;

    @Schema(description = "房间ID")
    private String roomId;

    @Schema(description = "是否允许屏幕共享")
    private Boolean allowScreenShare;

    @Schema(description = "是否允许录制")
    private Boolean allowRecord;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
