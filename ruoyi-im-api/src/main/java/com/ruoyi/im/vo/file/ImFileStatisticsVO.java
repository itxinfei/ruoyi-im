package com.ruoyi.im.vo.file;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 文件统计VO
 *
 * @author ruoyi
 */
@Data
@Schema(description = "文件统计信息")
public class ImFileStatisticsVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "文件总数")
    private Integer totalFiles;

    @Schema(description = "今日上传数")
    private Integer todayUploads;

    @Schema(description = "总存储大小（字节）")
    private Long totalStorageSize;

    @Schema(description = "总下载次数")
    private Integer totalDownloads;

    @Schema(description = "图片数量")
    private Integer imageCount;

    @Schema(description = "视频数量")
    private Integer videoCount;

    @Schema(description = "文档数量")
    private Integer documentCount;

    @Schema(description = "其他文件数量")
    private Integer otherCount;
}
