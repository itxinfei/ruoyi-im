package com.ruoyi.im.vo.meeting;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 会议室日程视图对象
 *
 * @author ruoyi
 */
@Data

public class ImMeetingRoomScheduleVO implements Serializable {

    private static final long serialVersionUID = 1L;

    
    private Long roomId;

    
    private String roomName;

    
    private String location;

    
    private Integer capacity;

    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private String date;

    
    private List<ScheduleItem> schedules;

    /**
     * 预订项
     */
    @Data
    
    public static class ScheduleItem implements Serializable {
        
        private Long bookingId;

        
        private String meetingTitle;

        
        private String meetingType;

        
        private String bookingUserName;

        
        @JsonFormat(pattern = "HH:mm")
        private String startTime;

        
        @JsonFormat(pattern = "HH:mm")
        private String endTime;

        
        private String status;

        
        private Integer attendeeCount;
    }
}

