package com.ruoyi.im.vo.file;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "分片上传初始化响应")
public class ImFileChunkUploadInitVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "上传ID")
    private String uploadId;

    @Schema(description = "总分片数")
    private Integer totalChunks;

    @Schema(description = "分片大小（字节）")
    private Integer chunkSize;

    @Schema(description = "已上传的分片序号列表（断点续传时使用）")
    private List<Integer> uploadedChunks;

    @Schema(description = "是否需要上传所有分片（秒传时为false）")
    private Boolean needUpload;

    @Schema(description = "文件URL（秒传时有值）")
    private String fileUrl;

    @Schema(description = "状态：UPLOADING上传中、COMPLETED已完成（秒传时）")
    private String status;

    @Schema(description = "过期时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expireTime;
}
