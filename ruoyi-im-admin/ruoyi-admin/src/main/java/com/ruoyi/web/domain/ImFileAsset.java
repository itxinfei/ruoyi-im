package com.ruoyi.web.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.time.LocalDateTime;


/**
 * 文件资产实体
 *
 * @author ruoyi
 * @date 2025-01-17
 */
@Schema(description = "文件资产")
public class ImFileAsset extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 文件状态：正常 */
    public static final String STATUS_ACTIVE = "ACTIVE";
    /** 文件状态：已删除 */
    public static final String STATUS_DELETED = "DELETED";

    /** 文件类型：图片 */
    public static final String TYPE_IMAGE = "image";
    /** 文件类型：视频 */
    public static final String TYPE_VIDEO = "video";
    /** 文件类型：文档 */
    public static final String TYPE_DOCUMENT = "document";
    /** 文件类型：音频 */
    public static final String TYPE_AUDIO = "audio";
    /** 文件类型：其他 */
    public static final String TYPE_OTHER = "other";

    /** 文件资产ID */
    @Schema(description = "文件资产ID")
    private Long id;

    /** 文件ID */
    @Schema(description = "文件ID")
    private Long fileId;

    /** 文件名 */
    @Schema(description = "文件名")
    private String fileName;

    /** 原始文件名 */
    @Schema(description = "原始文件名")
    private String originalName;

    /** 文件大小（字节） */
    @Schema(description = "文件大小（字节）")
    private Long fileSize;

    /** 文件类型（image/video/document/audio/other） */
    @Schema(description = "文件类型")
    private String fileType;

    /** 文件扩展名 */
    @Schema(description = "文件扩展名")
    private String fileExtension;

    /** 文件路径 */
    @Schema(description = "文件路径")
    private String filePath;

    /** 文件URL */
    @Schema(description = "文件URL")
    private String fileUrl;

    /** MIME类型 */
    @Schema(description = "MIME类型")
    private String mimeType;

    /** 上传者ID */
    @Schema(description = "上传者ID")
    private Long uploaderId;

    /** 上传者名称（关联查询，非数据库字段） */
    @Schema(description = "上传者名称")
    private String uploaderName;

    /** 上传时间（使用create_time） */
    @Schema(description = "上传时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime uploadTime;

    /** 下载次数 */
    @Schema(description = "下载次数")
    private Integer downloadCount;

    /** 状态（ACTIVE-正常 DELETED-已删除） */
    @Schema(description = "状态")
    private String status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getFileId() {
        return fileId;
    }

    public void setFileId(Long fileId) {
        this.fileId = fileId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getOriginalName() {
        return originalName;
    }

    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getFileExtension() {
        return fileExtension;
    }

    public void setFileExtension(String fileExtension) {
        this.fileExtension = fileExtension;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public Long getUploaderId() {
        return uploaderId;
    }

    public void setUploaderId(Long uploaderId) {
        this.uploaderId = uploaderId;
    }

    public String getUploaderName() {
        return uploaderName;
    }

    public void setUploaderName(String uploaderName) {
        this.uploaderName = uploaderName;
    }

    public LocalDateTime getUploadTime() {
        return uploadTime;
    }

    public void setUploadTime(LocalDateTime uploadTime) {
        this.uploadTime = uploadTime;
    }

    public Integer getDownloadCount() {
        return downloadCount;
    }

    public void setDownloadCount(Integer downloadCount) {
        this.downloadCount = downloadCount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * 判断文件是否已删除
     *
     * @return true-已删除，false-正常
     */
    public boolean isDeleted() {
        return STATUS_DELETED.equals(this.status);
    }

    /**
     * 判断文件是否正常
     *
     * @return true-正常，false-已删除
     */
    public boolean isActive() {
        return STATUS_ACTIVE.equals(this.status) && !Boolean.TRUE.equals(this.deleted);
    }

    /**
     * 获取格式化的文件大小（可读格式）
     *
     * @return 格式化后的文件大小，如 "1.5 MB"
     */
    public String getFormattedFileSize() {
        if (fileSize == null || fileSize <= 0) {
            return "0 B";
        }

        final String[] units = {"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(fileSize) / Math.log10(1024));
        return String.format("%.1f %s", fileSize / Math.pow(1024, digitGroups), units[digitGroups]);
    }

    /**
     * 根据文件扩展名判断是否为图片
     *
     * @return true-是图片，false-不是图片
     */
    public boolean isImage() {
        return TYPE_IMAGE.equals(this.fileType);
    }

    /**
     * 根据文件扩展名判断是否为视频
     *
     * @return true-是视频，false-不是视频
     */
    public boolean isVideo() {
        return TYPE_VIDEO.equals(this.fileType);
    }

}
