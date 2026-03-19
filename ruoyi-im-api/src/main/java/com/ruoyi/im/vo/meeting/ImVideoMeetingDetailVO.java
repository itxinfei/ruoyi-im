package com.ruoyi.im.vo.meeting;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 视频会议详情VO
 *
 * @author ruoyi
 */
@Data

public class ImVideoMeetingDetailVO implements Serializable {

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

    
    private String meetingPassword;

    
    private Boolean muteOnJoin;

    
    private Boolean allowScreenShare;

    
    private Boolean allowRecord;

    
    private String roomId;

    
    private String meetingLink;

    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    
    private Boolean isHost;

    
    private String participantStatus;

    
    private List<ParticipantInfo> participants;

    
    private Long screenSharerId;

    /**
     * 参与者信息
     */
    @Data
    public static class ParticipantInfo implements Serializable {
        
        private Long userId;

        
        private String userName;

        
        private String userAvatar;

        
        private String role;

        
        private String status;

        
        private Boolean isMuted;

        
        private Boolean isVideoOff;

        
        private Boolean isSharing;
    }
}

