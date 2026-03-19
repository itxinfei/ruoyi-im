package com.ruoyi.im.vo.schedule;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 日程参与人VO
 */
@Data

public class ScheduleParticipantVO implements Serializable {

    
    private Long id;

    
    private Long userId;

    
    private String userName;

    
    private String userAvatar;

    
    private String status;

    
    private String statusName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    
    private LocalDateTime responseTime;
}

