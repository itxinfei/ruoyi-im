package com.ruoyi.im.vo.cloud;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 云盘文件VO
 *
 * @author ruoyi
 */
@Data

public class ImCloudFileVO implements Serializable {

    private static final long serialVersionUID = 1L;

    
    private Long id;

    
    private String fileName;

    
    private Long fileSize;

    
    private String fileSizeFormat;

    
    private String fileType;

    
    private String fileExt;

    
    private String mimeType;

    
    private String fileUrl;

    
    private String previewUrl;

    
    private Long folderId;

    
    private String folderName;

    
    private Long uploaderId;

    
    private String uploaderName;

    
    private String uploaderAvatar;

    
    private String tags;

    
    private String description;

    
    private Integer downloadCount;

    
    private Integer previewCount;

    
    private String accessPermission;

    
    private Boolean canEdit;

    
    private Boolean canDelete;

    
    private Boolean canDownload;

    
    private Boolean canShare;

    
    private Integer latestVersion;

    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}

