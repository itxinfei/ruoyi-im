package com.ruoyi.im.vo.favorite;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 收藏消息VO
 *
 * @author ruoyi
 */
@Data

public class FavoriteMessageVO implements Serializable {

    private static final long serialVersionUID = 1L;

    
    private Long id;

    
    private Long messageId;

    
    private Long conversationId;

    
    private String conversationName;

    
    private String messageContent;

    
    private Long senderId;

    
    private String senderName;

    
    private String senderAvatar;

    
    private String messageType;

    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime messageTime;

    
    private String remark;

    
    private String tags;

    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}

