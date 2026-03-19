package com.ruoyi.im.vo.message;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 消息编辑历史VO
 */
@Data

public class MessageEditHistoryVO implements Serializable {

    
    private Long id;

    
    private Long messageId;

    
    private String oldContent;

    
    private String newContent;

    
    private Long editorId;

    
    private String editorName;

    
    private String editorAvatar;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    
    private LocalDateTime editTime;
}

