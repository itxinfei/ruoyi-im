package com.ruoyi.im.dto.announcement;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 公告更新请求
 *
 * @author ruoyi
 */
@Data

public class ImAnnouncementUpdateRequest {

    
    private Long id;

    
    private String title;

    
    private String content;

    
    private Integer priority;

    
    private LocalDateTime expiryTime;

    
    private Boolean isPinned;

    
    private Boolean allowComment;

    
    private String remark;
}

