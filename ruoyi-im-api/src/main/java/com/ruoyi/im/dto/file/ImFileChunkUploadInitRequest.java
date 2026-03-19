package com.ruoyi.im.dto.file;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 分片上传初始化请求DTO
 *
 * @author ruoyi
 */
@Data

public class ImFileChunkUploadInitRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    
    @NotBlank(message = "文件名不能为空")
    private String fileName;

    
    @NotNull(message = "文件大小不能为空")
    private Long fileSize;

    
    private String fileMd5;

    
    @Min(value = 102400, message = "分片大小不能小于100KB")
    private Integer chunkSize = 5242880; // 默认5MB

    
    private String fileType;

    
    private Long conversationId;
}

