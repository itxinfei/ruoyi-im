package com.ruoyi.im.dto.file;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 分片上传请求DTO
 *
 * @author ruoyi
 */
@Data

public class ImFileChunkUploadRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    
    @NotBlank(message = "上传ID不能为空")
    private String uploadId;

    
    @NotNull(message = "分片序号不能为空")
    @Min(value = 1, message = "分片序号必须从1开始")
    private Integer chunkNumber;

    
    private String chunkMd5;

    
    @NotNull(message = "分片文件不能为空")
    private MultipartFile file;
}

