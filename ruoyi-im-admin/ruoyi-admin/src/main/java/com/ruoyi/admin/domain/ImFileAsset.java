package com.ruoyi.im.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 文件资产实体
 *
 * @author ruoyi
 */
@TableName("im_file_asset")
@Data
@Schema(description = "文件资产")
public class ImFileAsset implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @Schema(description = "文件资产ID")
    private Long id;

    @Schema(description = "文件ID")
    private Long fileId;

    @Schema(description = "文件名")
    private String fileName;

    @Schema(description = "原始文件名")
    private String originalName;

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

    @Schema(description = "MIME类型")
    private String mimeType;

    @Schema(description = "上传者ID")
    private Long uploaderId;

    @Schema(description = "上传时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime uploadTime;

    @Schema(description = "下载次数")
    private Integer downloadCount;

    @Schema(description = "状态（0-删除 1-正常）")
    private Integer status;

    @Schema(description = "是否已删除")
    private Boolean deleted;

    @Schema(description = "删除时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime deleteTime;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
