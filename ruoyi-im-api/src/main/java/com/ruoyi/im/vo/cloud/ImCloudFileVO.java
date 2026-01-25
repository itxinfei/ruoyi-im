package com.ruoyi.im.vo.cloud;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 云盘文件VO
 *
 * @author ruoyi
 */
@Data
@Schema(description = "云盘文件")
public class ImCloudFileVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "文件ID")
    private Long id;

    @Schema(description = "文件名称")
    private String fileName;

    @Schema(description = "文件大小")
    private Long fileSize;

    @Schema(description = "格式化文件大小")
    private String fileSizeFormat;

    @Schema(description = "文件类型")
    private String fileType;

    @Schema(description = "文件扩展名")
    private String fileExt;

    @Schema(description = "MIME类型")
    private String mimeType;

    @Schema(description = "文件URL")
    private String fileUrl;

    @Schema(description = "预览URL")
    private String previewUrl;

    @Schema(description = "所在文件夹ID")
    private Long folderId;

    @Schema(description = "所在文件夹名称")
    private String folderName;

    @Schema(description = "上传者ID")
    private Long uploaderId;

    @Schema(description = "上传者名称")
    private String uploaderName;

    @Schema(description = "上传者头像")
    private String uploaderAvatar;

    @Schema(description = "文件标签")
    private String tags;

    @Schema(description = "文件描述")
    private String description;

    @Schema(description = "下载次数")
    private Integer downloadCount;

    @Schema(description = "预览权次数")
    private Integer previewCount;

    @Schema(description = "访问权限")
    private String accessPermission;

    @Schema(description = "是否可编辑")
    private Boolean canEdit;

    @Schema(description = "是否可删除")
    private Boolean canDelete;

    @Schema(description = "是否可下载")
    private Boolean canDownload;

    @Schema(description = "是否可分享")
    private Boolean canShare;

    @Schema(description = "最新版本号")
    private Integer latestVersion;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
