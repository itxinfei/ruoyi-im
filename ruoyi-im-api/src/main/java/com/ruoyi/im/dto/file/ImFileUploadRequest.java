package com.ruoyi.im.dto.file;

import lombok.Data;

import java.io.Serializable;

/**
 * 文件上传请求DTO
 *
 * @author ruoyi
 */
@Data

public class ImFileUploadRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    
    private String fileName;

    
    private Long fileSize;

    
    private String fileType;

    
    private String fileExtension;

    
    private Boolean compress;
}

