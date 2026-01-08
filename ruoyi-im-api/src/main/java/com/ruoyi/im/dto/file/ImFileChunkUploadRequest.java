package com.ruoyi.im.dto.file;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "分片上传请求")
public class ImFileChunkUploadRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "上传ID（由初始化接口返回）", required = true)
    @NotBlank(message = "上传ID不能为空")
    private String uploadId;

    @Schema(description = "分片序号（从1开始）", required = true)
    @NotNull(message = "分片序号不能为空")
    @Min(value = 1, message = "分片序号必须从1开始")
    private Integer chunkNumber;

    @Schema(description = "分片MD5值（用于校验）")
    private String chunkMd5;

    @Schema(description = "分片文件", required = true)
    @NotNull(message = "分片文件不能为空")
    private MultipartFile file;

    /**
     * 获取实际的文件对象
     * 用于Spring MVC绑定
     */
    public MultipartFile getFile() {
        return file;
    }

    public void setFile(MultipartFile file) {
        this.file = file;
    }
}
