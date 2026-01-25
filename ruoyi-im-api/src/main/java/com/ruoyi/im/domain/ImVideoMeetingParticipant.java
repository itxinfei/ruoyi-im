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
 * 视频会议参与者实体
 *
 * @author ruoyi
 */
@TableName("im_video_meeting_participant")
@Data
public class ImVideoMeetingParticipant implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 会议ID
     */
    @TableField("meeting_id")
    private Long meetingId;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 用户名称
     */
    @TableField("user_name")
    private String userName;

    /**
     * 用户头像
     */
    @TableField("user_avatar")
    private String userAvatar;

    /**
     * 角色：HOST主持者, PRESENTER演讲者, ATTENDEE参会者
     */
    @TableField("role")
    private String role;

    /**
     * 参与状态：INVITED已邀请, ACCEPTED已接受, JOINED已加入, LEFT已离开, REJECTED已拒绝
     */
    @TableField("status")
    private String status;

    /**
     * 加入时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("join_time")
    private LocalDateTime joinTime;

    /**
     * 离开时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("leave_time")
    private LocalDateTime leaveTime;

    /**
     * 是否静音
     */
    @TableField("is_muted")
    private Boolean isMuted;

    /**
     * 是否关闭视频
     */
    @TableField("is_video_off")
    private Boolean isVideoOff;

    /**
     * 是否正在共享屏幕
     */
    @TableField("is_sharing")
    private Boolean isSharing;

    /**
     * 设备类型
     */
    @TableField("device_type")
    private String deviceType;

    /**
     * IP地址
     */
    @TableField("ip_address")
    private String ipAddress;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("create_time")
    private LocalDateTime createTime;
}
