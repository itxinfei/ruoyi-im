package com.ruoyi.im.vo.file;

import lombok.Data;

import java.io.Serializable;

/**
 * 文件信息VO
 *
 * @author ruoyi
 */
@Data

public class ImFileVO implements Serializable {

    private static final long serialVersionUID = 1L;

    
    private Long fileId;

    
    private String fileName;

    
    private Long fileSize;

    
    private String fileType;

    
    private String fileExtension;

    
    private String filePath;

    
    private String fileUrl;

    
    private Long uploaderId;

    
    private String uploaderName;

    
    private String uploadTime;

    
    private Integer downloadCount;

    
    private Boolean deleted;
}

