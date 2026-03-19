package com.ruoyi.im.dto.message;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 消息搜索请求DTO
 *
 * @author ruoyi
 */
@Data

public class ImMessageSearchRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    
    private Long conversationId;

    
    private String keyword;

    
    private String messageType;

    
    private Long senderId;

    
    private LocalDateTime startTime;

    
    private LocalDateTime endTime;

    
    private Integer pageNum = 1;

    
    private Integer pageSize = 20;

    
    private Boolean includeRevoked = false;

    
    private Boolean exactMatch = false;
}

