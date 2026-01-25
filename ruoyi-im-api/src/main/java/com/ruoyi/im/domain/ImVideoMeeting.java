package com.ruoyi.im.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 视频会议实体
 * 支持多人视频会议和屏幕共享
 *
 * @author ruoyi
 */
@TableName("im_video_meeting")
@Data
public class ImVideoMeeting implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 会议ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 会议标题
     */
    @TableField("title")
    private String title;

    /**
     * 会议描述
     */
    @TableField("description")
    private String description;

    /**
     * 发起人ID
     */
    @TableField("host_id")
    private Long hostId;

    /**
     * 发起人名称（冗余字段）
     */
    @TableField("host_name")
    private String hostName;

    /**
     * 会议类型：MEETING会议, WEBINAR网络研讨会
     */
    @TableField("meeting_type")
    private String meetingType;

    /**
     * 会议状态：SCHEDULED预定, IN_PROGRESS进行中, ENDED已结束, CANCELLED已取消
     */
    @TableField("status")
    private String status;

    /**
     * 预定开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("scheduled_start_time")
    private LocalDateTime scheduledStartTime;

    /**
     * 预定结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("scheduled_end_time")
    private LocalDateTime scheduledEndTime;

    /**
     * 实际开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("actual_start_time")
    private LocalDateTime actualStartTime;

    /**
     * 实际结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("actual_end_time")
    private LocalDateTime actualEndTime;

    /**
     * 会议时长（分钟）
     */
    @TableField("duration")
    private Integer duration;

    /**
     * 最大参与人数
     */
    @TableField("max_participants")
    private Integer maxParticipants;

    /**
     * 当前参与人数
     */
    @TableField("current_participants")
    private Integer currentParticipants;

    /**
     * 是否需要密码
     */
    @TableField("require_password")
    private Boolean requirePassword;

    /**
     * 会议密码
     */
    @TableField("meeting_password")
    private String meetingPassword;

    /**
     * 是否开启入会静音
     */
    @TableField("mute_on_join")
    private Boolean muteOnJoin;

    /**
     * 是否允许屏幕共享
     */
    @TableField("allow_screen_share")
    private Boolean allowScreenShare;

    /**
     * 是否允许录制
     */
    @TableField("allow_record")
    private Boolean allowRecord;

    /**
     * 录制文件路径
     */
    @TableField("record_file_path")
    private String recordFilePath;

    /**
     * 房间ID（用于WebRTC）
     */
    @TableField("room_id")
    private String roomId;

    /**
     * 会议链接
     */
    @TableField("meeting_link")
    private String meetingLink;

    /**
     * 是否已删除
     */
    @TableField("del_flag")
    private Integer delFlag;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("update_time")
    private LocalDateTime updateTime;
}
