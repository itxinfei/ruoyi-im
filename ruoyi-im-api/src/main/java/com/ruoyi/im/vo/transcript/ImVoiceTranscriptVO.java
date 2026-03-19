package com.ruoyi.im.vo.transcript;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 语音转文字VO
 *
 * @author ruoyi
 */
@Data

public class ImVoiceTranscriptVO implements Serializable {

    private static final long serialVersionUID = 1L;

    
    private Long id;

    
    private Long messageId;

    
    private String voiceUrl;

    
    private Integer duration;

    
    private String status;

    
    private String transcriptText;

    
    private String language;

    
    private Integer confidence;

    
    private String errorMsg;

    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime completeTime;
}

