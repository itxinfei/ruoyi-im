package com.ruoyi.im.vo.ding;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * DING回执VO
 */
@Data

public class DingReceiptVO implements Serializable {

    
    private Long id;

    
    private Long receiverId;

    
    private String receiverName;

    
    private String receiverAvatar;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    
    private LocalDateTime readTime;

    
    private Integer confirmed;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    
    private LocalDateTime confirmTime;

    
    private String remark;
}

