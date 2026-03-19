package com.ruoyi.im.dto.announcement;

import com.ruoyi.im.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 公告查询请求
 *
 * @author ruoyi
 */
@Data
@EqualsAndHashCode(callSuper = true)

public class ImAnnouncementQueryRequest extends PageRequest {

    
    private String keyword;

    
    private String announcementType;

    
    private Integer priority;

    
    private String status;

    
    private Long publisherId;

    
    private Long departmentId;

    
    private Boolean pinnedOnly = false;

    
    private Boolean activeOnly = true;

    
    private Boolean myPublishedOnly = false;

    
    private String orderBy = "publishTime";

    
    private String orderDirection = "DESC";
}

