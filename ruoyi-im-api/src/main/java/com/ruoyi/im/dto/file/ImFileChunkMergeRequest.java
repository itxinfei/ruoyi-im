package com.ruoyi.im.dto.file;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 分片合并请求DTO
 *
 * @author ruoyi
 */
@Data

public class ImFileChunkMergeRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    
    @NotBlank(message = "上传ID不能为空")
    private String uploadId;

    
    private String fileMd5;
}

