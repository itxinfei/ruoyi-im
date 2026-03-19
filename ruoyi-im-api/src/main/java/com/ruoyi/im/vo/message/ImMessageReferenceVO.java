package com.ruoyi.im.vo.message;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 消息引用VO
 * 用于消息转发、回复等场景的引用信息展示
 *
 * @author ruoyi
 */
@Data

public class ImMessageReferenceVO implements Serializable {

    private static final long serialVersionUID = 1L;

    
    private Long id;

    
    private Long originalMessageId;

    
    private String referenceType;

    
    private Long originalSenderId;

    
    private String originalSenderName;

    
    private String originalContentPreview;

    
    private String originalMessageType;

    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime referenceTime;
}

