package com.ruoyi.im.vo.file;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 文件分享VO
 *
 * @author ruoyi
 */
@Data

public class ImFileShareVO implements Serializable {

    private static final long serialVersionUID = 1L;

    
    private Long id;

    
    private String shareUrl;

    
    private String fileName;

    
    private Long fileSize;

    
    private String fileType;

    
    private Long sharerId;

    
    private String sharerName;

    
    private String sharerAvatar;

    
    private Integer permission;

    
    private Integer accessCount;

    
    private Integer downloadCount;

    
    private Boolean isExpired;

    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expireTime;
}

