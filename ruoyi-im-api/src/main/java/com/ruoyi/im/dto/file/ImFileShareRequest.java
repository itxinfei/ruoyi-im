package com.ruoyi.im.dto.file;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 文件分享请求DTO
 *
 * @author ruoyi
 */
@Data

public class ImFileShareRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    
    @NotNull(message = "文件ID不能为空")
    private Long fileId;

    
    @NotBlank(message = "接收者不能为空")
    private String receiverIds;

    
    private Integer permission = 1;

    
    private String accessPassword;

    
    private Integer allowDownload = 1;

    
    private Integer allowPreview = 1;

    
    private Integer expireDays = 0;
}

