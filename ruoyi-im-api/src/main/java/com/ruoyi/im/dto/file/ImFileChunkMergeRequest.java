package com.ruoyi.im.dto.file;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 分片合并请求DTO
 *
 * @author ruoyi
 */
@Data
@Schema(description = "分片合并请求")
public class ImFileChunkMergeRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "上传ID", required = true)
    @NotBlank(message = "上传ID不能为空")
    private String uploadId;

    @Schema(description = "文件MD5值（用于校验完整性）")
    private String fileMd5;
}
