package com.ruoyi.im.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
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
    @Schema(description = "文件ID")
    private Long id;

    @Schema(description = "文件名")
    @TableField("file_name")
    private String fileName;

    @Schema(description = "文件路径")
    @TableField("file_path")
    private String filePath;

    @Schema(description = "文件大小（字节）")
    @TableField("file_size")
    private Long fileSize;

    @Schema(description = "文件类型")
    @TableField("file_type")
    private String fileType;

    @Schema(description = "文件扩展名")
    @TableField("file_ext")
    private String fileExt;

    @Schema(description = "文件MD5值")
    private String md5;

    @Schema(description = "上传者ID")
    @TableField("uploader_id")
    private Long uploaderId;

    @Schema(description = "下载次数")
    @TableField("download_count")
    private Integer downloadCount;

    @Schema(description = "下载过期时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("download_expire_time")
    private LocalDateTime downloadExpireTime;

    @Schema(description = "状态：ACTIVE=正常, DELETED=已删除")
    private String status;

    @TableField("is_deleted")
    @Schema(description = "是否删除：0=正常 1=已删除")
    private Integer isDeleted;

    @Schema(description = "创建时间")
    @TableField("create_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
