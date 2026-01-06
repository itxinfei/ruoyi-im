package com.ruoyi.im.dto.file;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 文件上传请求DTO
 *
 * @author ruoyi
 */
@Data
@Schema(description = "文件上传请求")
public class ImFileUploadRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "文件名")
    private String fileName;

    @Schema(description = "文件大小（字节）")
    private Long fileSize;

    @Schema(description = "文件类型（image/video/document/audio/other）")
    private String fileType;

    @Schema(description = "文件扩展名")
    private String fileExtension;

    @Schema(description = "是否压缩")
    private Boolean compress;
}
