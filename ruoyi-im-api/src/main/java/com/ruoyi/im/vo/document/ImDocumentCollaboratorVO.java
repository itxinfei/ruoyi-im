package com.ruoyi.im.vo.document;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 文档协作者VO
 *
 * @author ruoyi
 */
@Data

public class ImDocumentCollaboratorVO implements Serializable {

    private static final long serialVersionUID = 1L;

    
    private Long id;

    
    private Long documentId;

    
    private Long userId;

    
    private String userName;

    
    private String userAvatar;

    
    private String permission;

    
    private String onlineStatus;

    
    private String cursorPosition;

    
    private String selectionRange;

    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime joinTime;

    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastActiveTime;

    
    private Boolean canEdit;

    
    private Boolean canRemove;
}

