package com.ruoyi.im.vo.group;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 群组公告VO
 *
 * @author ruoyi
 */
@Data

public class ImGroupAnnouncementVO implements Serializable {

    private static final long serialVersionUID = 1L;

    
    private Long id;

    
    private Long groupId;

    
    private String groupName;

    
    private Long senderId;

    
    private String senderName;

    
    private String senderAvatar;

    
    private String content;

    
    private Integer type;

    
    private String attachmentUrl;

    
    private Integer isPinned;

    
    private Integer status;

    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    
    private Boolean canEdit = false;

    
    private Boolean canDelete = false;

    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expireTime;
}

