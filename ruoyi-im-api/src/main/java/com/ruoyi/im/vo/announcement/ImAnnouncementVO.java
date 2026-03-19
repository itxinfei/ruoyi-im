package com.ruoyi.im.vo.announcement;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 公告视图对象
 *
 * @author ruoyi
 */
@Data

public class ImAnnouncementVO implements Serializable {

    private static final long serialVersionUID = 1L;

    
    private Long id;

    
    private String title;

    
    private String announcementType;

    
    private String announcementTypeDisplay;

    
    private Integer priority;

    
    private String priorityDisplay;

    
    private String status;

    
    private String statusDisplay;

    
    private Long publisherId;

    
    private String publisherName;

    
    private String publisherAvatar;

    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime publishTime;

    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expiryTime;

    
    private Boolean isPinned;

    
    private Boolean allowComment;

    
    private Integer viewCount;

    
    private Integer likeCount;

    
    private Integer commentCount;

    
    private Integer readCount;

    
    private Integer totalTargetCount;

    
    private Integer readPercent;

    
    private Boolean isLiked;

    
    private Boolean isRead;

    
    private Boolean isExpired;
}

