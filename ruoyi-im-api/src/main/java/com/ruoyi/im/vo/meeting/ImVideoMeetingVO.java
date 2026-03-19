package com.ruoyi.im.vo.meeting;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 视频会议VO
 *
 * @author ruoyi
 */
@Data

public class ImVideoMeetingVO implements Serializable {

    private static final long serialVersionUID = 1L;

    
    private Long id;

    
    private String title;

    
    private String description;

    
    private Long hostId;

    
    private String hostName;

    
    private String hostAvatar;

    
    private String meetingType;

    
    private String status;

    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime scheduledStartTime;

    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime scheduledEndTime;

    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime actualStartTime;

    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime actualEndTime;

    
    private Integer duration;

    
    private Integer maxParticipants;

    
    private Integer currentParticipants;

    
    private Boolean requirePassword;

    
    private String meetingLink;

    
    private String roomId;

    
    private Boolean allowScreenShare;

    
    private Boolean allowRecord;

    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}

