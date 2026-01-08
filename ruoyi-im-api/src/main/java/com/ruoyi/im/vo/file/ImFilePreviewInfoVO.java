package com.ruoyi.im.vo.file;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 文件预览信息VO
 *
 * @author ruoyi
 */
@Data
@Schema(description = "文件预览信息")
public class ImFilePreviewInfoVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "文件ID")
    private Long fileId;

    @Schema(description = "文件名")
    private String fileName;

    @Schema(description = "文件类型")
    private String fileType;

    @Schema(description = "文件扩展名")
    private String fileExtension;

    @Schema(description = "文件大小（字节）")
    private Long fileSize;

    @Schema(description = "预览类型：direct直接显示、image图片、pdf文档、office办公软件、video视频、audio音频")
    private String previewType;

    @Schema(description = "预览URL")
    private String previewUrl;

    @Schema(description = "缩略图URL")
    private String thumbnailUrl;

    @Schema(description = "下载URL")
    private String downloadUrl;

    @Schema(description = "是否支持在线预览")
    private Boolean canPreview;

    @Schema(description = "支持的预览格式列表")
    private List<String> supportedFormats;

    @Schema(description = "文件页数（PDF/PPT等）")
    private Integer pageCount;

    @Schema(description = "视频时长（秒）")
    private Long duration;

    @Schema(description = "图片宽度")
    private Integer width;

    @Schema(description = "图片高度")
    private Integer height;
}
