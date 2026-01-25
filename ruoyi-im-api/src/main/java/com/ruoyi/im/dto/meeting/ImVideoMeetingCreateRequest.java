package com.ruoyi.im.dto.meeting;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 视频会议创建请求DTO
 *
 * @author ruoyi
 */
@Data
@Schema(description = "视频会议创建请求")
public class ImVideoMeetingCreateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 会议标题
     */
    @NotBlank(message = "会议标题不能为空")
    @Schema(description = "会议标题")
    private String title;

    /**
     * 会议描述
     */
    @Schema(description = "会议描述")
    private String description;

    /**
     * 会议类型：MEETING会议, WEBINAR网络研讨会
     */
    @Schema(description = "会议类型")
    private String meetingType;

    /**
     * 预定开始时间
     */
    @Schema(description = "预定开始时间")
    private LocalDateTime scheduledStartTime;

    /**
     * 预定结束时间
     */
    @Schema(description = "预定结束时间")
    private LocalDateTime scheduledEndTime;

    /**
     * 会议时长（分钟）
     */
    @Schema(description = "会议时长（分钟）")
    private Integer duration;

    /**
     * 最大参与人数
     */
    @Schema(description = "最大参与人数", example = "9")
    private Integer maxParticipants;

    /**
     * 是否需要密码
     */
    @Schema(description = "是否需要密码")
    private Boolean requirePassword;

    /**
     * 会议密码
     */
    @Schema(description = "会议密码")
    private String meetingPassword;

    /**
     * 是否开启入会静音
     */
    @Schema(description = "是否开启入会静音")
    private Boolean muteOnJoin;

    /**
     * 是否允许屏幕共享
     */
    @Schema(description = "是否允许屏幕共享")
    private Boolean allowScreenShare;

    /**
     * 是否允许录制
     */
    @Schema(description = "是否允许录制")
    private Boolean allowRecord;

    /**
     * 被邀请的用户ID列表
     */
    @Schema(description = "被邀请的用户ID列表")
    private java.util.List<Long> invitedUserIds;
}
