package com.ruoyi.im.dto.meeting;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 会议室预订请求
 *
 * @author ruoyi
 */
@Data

public class ImMeetingBookingRequest {

    @NotNull(message = "会议室ID不能为空")
    
    private Long roomId;

    @NotBlank(message = "会议主题不能为空")
    
    private String meetingTitle;

    
    private String meetingType = "REGULAR";

    @NotNull(message = "开始时间不能为空")
    
    private LocalDateTime startTime;

    @NotNull(message = "结束时间不能为空")
    
    private LocalDateTime endTime;

    @NotNull(message = "参会人数不能为空")
    
    private Integer attendeeCount;

    
    private List<Long> attendees;

    
    private String agenda;

    
    private List<String> resources;

    
    private Boolean refreshments;

    
    private Boolean recording;

    
    private String remark;
}

