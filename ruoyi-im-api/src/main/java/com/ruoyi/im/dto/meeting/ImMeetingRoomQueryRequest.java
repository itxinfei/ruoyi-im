package com.ruoyi.im.dto.meeting;

import com.ruoyi.im.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 会议室查询请求
 *
 * @author ruoyi
 */
@Data
@EqualsAndHashCode(callSuper = true)

public class ImMeetingRoomQueryRequest extends PageRequest {

    
    private String keyword;

    
    private Long departmentId;

    
    private Integer minCapacity;

    
    private Integer floor;

    
    private String status;

    
    private Boolean hasProjector;

    
    private Boolean hasWhiteboard;

    
    private Boolean hasVideoConf;

    
    private String orderBy = "createTime";

    
    private String orderDirection = "desc";
}

