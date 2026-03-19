package com.ruoyi.im.dto.group;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 群组文件上传请求DTO
 *
 * @author ruoyi
 */
@Data

public class ImGroupFileUploadRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    
    @NotNull(message = "群组ID不能为空")
    private Long groupId;

    
    @NotNull(message = "文件ID不能为空")
    private Long fileId;

    
    private String fileName;

    
    private String category = "default";

    
    private String permission = "ALL";
}

