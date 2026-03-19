package com.ruoyi.im.vo.meeting;

import com.fasterxml.jackson.annotation.JsonFormat;
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

public class ImMeetingBookingVO implements Serializable {

    private static final long serialVersionUID = 1L;

    
    private Long id;

    
    private Long roomId;

    
    private String roomName;

    
    private String roomLocation;

    
    private Long bookingUserId;

    
    private String bookingUserName;

    
    private String bookingUserAvatar;

    
    private String meetingTitle;

    
    private String meetingType;

    
    private String meetingTypeDisplay;

    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    
    private Integer attendeeCount;

    
    private List<Attendee> attendees;

    
    private String agenda;

    
    private List<String> resources;

    
    private Boolean refreshments;

    
    private Boolean recording;

    
    private String status;

    
    private String statusDisplay;

    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime checkInTime;

    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime checkOutTime;

    
    private Boolean isCheckedIn;

    
    private Boolean isCheckedOut;

    
    private Boolean isExpired;

    
    private String feedback;

    
    private Integer rating;

    
    private String remark;

    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /**
     * 参会人员
     */
    @Data
    
    public static class Attendee implements Serializable {
        
        private Long userId;

        
        private String userName;

        
        private String userAvatar;

        
        private Boolean isCheckedIn;

        
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime checkInTime;
    }
}

