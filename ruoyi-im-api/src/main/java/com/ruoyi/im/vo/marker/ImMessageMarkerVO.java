package com.ruoyi.im.vo.marker;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 消息标记VO
 *
 * @author ruoyi
 */
@Data

public class ImMessageMarkerVO implements Serializable {

    private static final long serialVersionUID = 1L;

    
    private Long id;

    
    private Long messageId;

    
    private Long conversationId;

    
    private String markerType;

    
    private String color;

    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime remindTime;

    
    private String todoStatus;

    
    private String remark;

    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime doneTime;

    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    // ==================== 扩展字段 ====================

    
    private String messagePreview;

    
    private Long senderId;

    
    private String senderName;

    
    private String conversationName;

    
    private Boolean isExpired;
}

