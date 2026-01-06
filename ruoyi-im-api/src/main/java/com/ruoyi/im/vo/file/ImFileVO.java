package com.ruoyi.im.vo.file;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 文件信息VO
 *
 * @author ruoyi
 */
@Data
@Schema(description = "文件信息")
public class ImFileVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "文件ID")
    private Long fileId;

    @Schema(description = "文件名")
    private String fileName;

    @Schema(description = "文件大小（字节）")
    private Long fileSize;

    @Schema(description = "文件类型（image/video/document/audio/other）")
    private String fileType;

    @Schema(description = "文件扩展名")
    private String fileExtension;

    @Schema(description = "文件路径")
    private String filePath;

    @Schema(description = "文件URL")
    private String fileUrl;

    @Schema(description = "上传者ID")
    private Long uploaderId;

    @Schema(description = "上传者昵称")
    private String uploaderName;

    @Schema(description = "上传时间")
    private String uploadTime;

    @Schema(description = "下载次数")
    private Integer downloadCount;

    @Schema(description = "是否已删除")
    private Boolean deleted;
}
