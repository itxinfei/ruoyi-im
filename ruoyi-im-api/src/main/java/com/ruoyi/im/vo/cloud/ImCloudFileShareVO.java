package com.ruoyi.im.vo.cloud;

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

public class ImCloudFileShareVO implements Serializable {

    private static final long serialVersionUID = 1L;

    
    private Long id;

    
    private String shareType;

    
    private Long resourceId;

    
    private String resourceName;

    
    private Long sharerId;

    
    private String sharerName;

    
    private String shareLink;

    
    private Boolean hasPassword;

    
    private Integer expireDays;

    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expireTime;

    
    private Boolean isExpired;

    
    private Boolean allowDownload;

    
    private Boolean allowPreview;

    
    private Integer viewCount;

    
    private Integer downloadCount;

    
    private String status;

    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}

