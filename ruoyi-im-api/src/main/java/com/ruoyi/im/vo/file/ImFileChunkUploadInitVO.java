package com.ruoyi.im.vo.file;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 分片上传初始化响应VO
 *
 * @author ruoyi
 */
@Data

public class ImFileChunkUploadInitVO implements Serializable {

    private static final long serialVersionUID = 1L;

    
    private String uploadId;

    
    private Integer totalChunks;

    
    private Integer chunkSize;

    
    private List<Integer> uploadedChunks;

    
    private Boolean needUpload;

    
    private String fileUrl;

    
    private String status;

    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expireTime;
}

