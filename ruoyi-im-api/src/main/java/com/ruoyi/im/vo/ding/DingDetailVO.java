package com.ruoyi.im.vo.ding;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DING详情VO
 */
@Data

public class DingDetailVO implements Serializable {

    
    private Long id;

    
    private Long senderId;

    
    private String senderName;

    
    private String senderAvatar;

    
    private String content;

    
    private String dingType;

    
    private Integer isUrgent;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    
    private LocalDateTime sendTime;

    
    private String status;

    
    private Integer totalCount;

    
    private Integer readCount;

    
    private Integer confirmedCount;

    
    private String attachment;

    
    private List<DingReceiptVO> receipts;
}

