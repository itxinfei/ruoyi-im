package com.ruoyi.im.dto.meeting;

import lombok.Data;

import java.io.Serializable;

/**
 * 视频会议更新请求DTO
 *
 * @author ruoyi
 */
@Data

public class ImVideoMeetingUpdateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 会议标题
     */
    
    private String title;

    /**
     * 会议描述
     */
    
    private String description;

    /**
     * 预定开始时间
     */
    
    private String scheduledStartTime;

    /**
     * 预定结束时间
     */
    
    private String scheduledEndTime;

    /**
     * 会议时长（分钟）
     */
    
    private Integer duration;

    /**
     * 最大参与人数
     */
    
    private Integer maxParticipants;

    /**
     * 是否需要密码
     */
    
    private Boolean requirePassword;

    /**
     * 会议密码
     */
    
    private String meetingPassword;

    /**
     * 是否开启入会静音
     */
    
    private Boolean muteOnJoin;

    /**
     * 是否允许屏幕共享
     */
    
    private Boolean allowScreenShare;

    /**
     * 是否允许录制
     */
    
    private Boolean allowRecord;
}

