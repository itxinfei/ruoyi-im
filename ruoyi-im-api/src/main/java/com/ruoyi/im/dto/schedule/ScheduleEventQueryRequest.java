package com.ruoyi.im.dto.schedule;

import com.ruoyi.im.dto.BasePageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 查询日程事件请求
 */
@Data
@EqualsAndHashCode(callSuper = true)

public class ScheduleEventQueryRequest extends BasePageRequest {

    
    private LocalDateTime startTime;

    
    private LocalDateTime endTime;

    
    private String status;

    
    private String keyword;

    
    private Boolean onlyParticipated = false;
}

