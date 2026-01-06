package com.ruoyi.im.vo.file;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 文件统计VO
 *
 * @author ruoyi
 */
@Data
@Schema(description = "文件统计信息")
public class ImFileStatisticsVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "总文件数")
    private Long totalFiles;

    @Schema(description = "总存储大小（字节）")
    private Long totalStorage;

    @Schema(description = "今日上传数")
    private Integer todayUploads;

    @Schema(description = "今日下载数")
    private Integer todayDownloads;

    @Schema(description = "本周上传数")
    private Integer weekUploads;

    @Schema(description = "本周下载数")
    private Integer weekDownloads;

    @Schema(description = "本月上传数")
    private Integer monthUploads;

    @Schema(description = "本月下载数")
    private Integer monthDownloads;

    @Schema(description = "按类型分组的文件统计")
    private java.util.List<FileTypeStats> typeStats;

    @Data
    @Schema(description = "按文件类型统计")
    public static class FileTypeStats implements Serializable {

        private static final long serialVersionUID = 1L;

        @Schema(description = "文件类型: image, video, document, audio, other")
        private String fileType;

        @Schema(description = "文件数量")
        private Long count;

        @Schema(description = "总大小（字节）")
        private Long totalSize;
    }
}
