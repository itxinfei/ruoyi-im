package com.ruoyi.im.vo.file;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.time.LocalDateTime;

/**
 * 文件信息VO
 * 
 * @author ruoyi
 * @since 2024-01-05
 */
@ApiModel(description = "文件信息")
public class ImFileVO {

    @ApiModelProperty(value = "文件ID", example = "1")
    private Long id;

    @ApiModelProperty(value = "文件名称", example = "avatar.jpg")
    private String fileName;

    @ApiModelProperty(value = "原始文件名", example = "我的头像.jpg")
    private String originalName;

    @ApiModelProperty(value = "文件路径", example = "/files/1704451200000_1.jpg")
    private String filePath;

    @ApiModelProperty(value = "文件URL", example = "http://localhost:8080/files/1704451200000_1.jpg")
    private String fileUrl;

    @ApiModelProperty(value = "文件大小（字节）", example = "102400")
    private Long fileSize;

    @ApiModelProperty(value = "文件大小描述", example = "100KB")
    private String fileSizeDesc;

    @ApiModelProperty(value = "文件类型", example = "image")
    private String fileType;

    @ApiModelProperty(value = "文件类型描述", example = "图片")
    private String fileTypeDesc;

    @ApiModelProperty(value = "文件扩展名", example = ".jpg")
    private String fileExt;

    @ApiModelProperty(value = "文件分类", example = "avatar")
    private String category;

    @ApiModelProperty(value = "文件标签", example = "头像,个人资料")
    private String tags;

    @ApiModelProperty(value = "文件描述", example = "用户头像图片")
    private String description;

    @ApiModelProperty(value = "上传者用户ID", example = "1")
    private Long uploaderId;

    @ApiModelProperty(value = "上传者用户名", example = "admin")
    private String uploaderUsername;

    @ApiModelProperty(value = "上传者昵称", example = "管理员")
    private String uploaderNickname;

    @ApiModelProperty(value = "文件状态", example = "ACTIVE")
    private String status;

    @ApiModelProperty(value = "文件状态描述", example = "正常")
    private String statusDesc;

    @ApiModelProperty(value = "下载次数", example = "5")
    private Integer downloadCount;

    @ApiModelProperty(value = "是否可下载", example = "true")
    private Boolean canDownload;

    @ApiModelProperty(value = "是否图片", example = "true")
    private Boolean isImage;

    @ApiModelProperty(value = "上传时间", example = "2024-01-05 10:30:00")
    private LocalDateTime uploadTime;

    @ApiModelProperty(value = "创建时间", example = "2024-01-05 10:30:00")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间", example = "2024-01-05 10:30:00")
    private LocalDateTime updateTime;

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getFileSizeDesc() {
        return fileSizeDesc;
    }

    public void setFileSizeDesc(String fileSizeDesc) {
        this.fileSizeDesc = fileSizeDesc;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getFileTypeDesc() {
        return fileTypeDesc;
    }

    public void setFileTypeDesc(String fileTypeDesc) {
        this.fileTypeDesc = fileTypeDesc;
    }

    public String getFileExt() {
        return fileExt;
    }

    public void setFileExt(String fileExt) {
        this.fileExt = fileExt;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getUploaderId() {
        return uploaderId;
    }

    public void setUploaderId(Long uploaderId) {
        this.uploaderId = uploaderId;
    }

    public String getUploaderUsername() {
        return uploaderUsername;
    }

    public void setUploaderUsername(String uploaderUsername) {
        this.uploaderUsername = uploaderUsername;
    }

    public String getUploaderNickname() {
        return uploaderNickname;
    }

    public void setUploaderNickname(String uploaderNickname) {
        this.uploaderNickname = uploaderNickname;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatusDesc() {
        return statusDesc;
    }

    public void setStatusDesc(String statusDesc) {
        this.statusDesc = statusDesc;
    }

    public Integer getDownloadCount() {
        return downloadCount;
    }

    public void setDownloadCount(Integer downloadCount) {
        this.downloadCount = downloadCount;
    }

    public Boolean getCanDownload() {
        return canDownload;
    }

    public void setCanDownload(Boolean canDownload) {
        this.canDownload = canDownload;
    }

    public Boolean getIsImage() {
        return isImage;
    }

    public void setIsImage(Boolean isImage) {
        this.isImage = isImage;
    }

    public LocalDateTime getUploadTime() {
        return uploadTime;
    }

    public void setUploadTime(LocalDateTime uploadTime) {
        this.uploadTime = uploadTime;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
}