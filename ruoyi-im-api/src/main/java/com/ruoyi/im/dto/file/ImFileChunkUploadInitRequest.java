package com.ruoyi.im.dto.file;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "分片上传初始化请求")
public class ImFileChunkUploadInitRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "文件名", required = true)
    @NotBlank(message = "文件名不能为空")
    private String fileName;

    @Schema(description = "文件大小（字节）", required = true)
    @NotNull(message = "文件大小不能为空")
    private Long fileSize;

    @Schema(description = "文件MD5值")
    private String fileMd5;

    @Schema(description = "分片大小（字节），默认5MB", defaultValue = "5242880")
    @Min(value = 102400, message = "分片大小不能小于100KB")
    private Integer chunkSize = 5242880; // 默认5MB

    @Schema(description = "文件类型（IMAGE/VIDEO/DOCUMENT/OTHER）")
    private String fileType;

    @Schema(description = "会话ID（聊天文件上传时需要）")
    private Long conversationId;
}
