package com.ruoyi.im.vo.file;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 文件预览信息VO
 *
 * @author ruoyi
 */
@Data

public class ImFilePreviewInfoVO implements Serializable {

    private static final long serialVersionUID = 1L;

    
    private Long fileId;

    
    private String fileName;

    
    private String fileType;

    
    private String fileExtension;

    
    private Long fileSize;

    
    private String previewType;

    
    private String previewUrl;

    
    private String thumbnailUrl;

    
    private String downloadUrl;

    
    private Boolean canPreview;

    
    private List<String> supportedFormats;

    
    private Integer pageCount;

    
    private Long duration;

    
    private Integer width;

    
    private Integer height;
}

