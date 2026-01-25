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
 * 企业云盘文件实体
 *
 * @author ruoyi
 */
@TableName("im_cloud_file")
@Data
@Schema(description = "企业云盘文件")
public class ImCloudFile implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 文件ID
     */
    @TableId(type = IdType.AUTO)
    @Schema(description = "文件ID")
    private Long id;

    /**
     * 关联的文件资产ID
     */
    @Schema(description = "文件资产ID")
    @TableField("file_asset_id")
    private Long fileAssetId;

    /**
     * 所在文件夹ID
     */
    @Schema(description = "所在文件夹ID")
    @TableField("folder_id")
    private Long folderId;

    /**
     * 文件名称（可包含后缀）
     */
    @Schema(description = "文件名称")
    @TableField("file_name")
    private String fileName;

    /**
     * 文件大小（字节）
     */
    @Schema(description = "文件大小")
    @TableField("file_size")
    private Long fileSize;

    /**
     * 文件类型：document文档, image图片, video视频, audio音频, archive压缩包, other其他
     */
    @Schema(description = "文件类型")
    @TableField("file_type")
    private String fileType;

    /**
     * 文件扩展名
     */
    @Schema(description = "文件扩展名")
    @TableField("file_ext")
    private String fileExt;

    /**
     * MIME类型
     */
    @Schema(description = "MIME类型")
    @TableField("mime_type")
    private String mimeType;

    /**
     * 上传者ID
     */
    @Schema(description = "上传者ID")
    @TableField("uploader_id")
    private Long uploaderId;

    /**
     * 上传者名称（冗余）
     */
    @Schema(description = "上传者名称")
    @TableField("uploader_name")
    private String uploaderName;

    /**
     * 文件标签（逗号分隔）
     */
    @Schema(description = "文件标签")
    @TableField("tags")
    private String tags;

    /**
     * 文件描述
     */
    @Schema(description = "文件描述")
    @TableField("description")
    private String description;

    /**
     * 下载次数
     */
    @Schema(description = "下载次数")
    @TableField("download_count")
    private Integer downloadCount;

    /**
     * 预览权次数
     */
    @Schema(description = "预览权次数")
    @TableField("preview_count")
    private Integer previewCount;

    /**
     * 访问权限：PRIVATE私有, DEPARTMENT部门可见, PUBLIC公开
     */
    @Schema(description = "访问权限")
    @TableField("access_permission")
    private String accessPermission;

    /**
     * 是否已删除
     */
    @Schema(description = "是否已删除")
    @TableField("is_deleted")
    private Boolean isDeleted;

    /**
     * 删除时间
     */
    @Schema(description = "删除时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("delete_time")
    private LocalDateTime deleteTime;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("update_time")
    private LocalDateTime updateTime;
}
