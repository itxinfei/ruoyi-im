package com.ruoyi.im.vo.file;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.time.LocalDateTime;

/**
 * 鏂囦欢淇℃伅VO
 * 
 * @author ruoyi
 * @since 2024-01-05
 */
@ApiModel(description = "鏂囦欢淇℃伅")
public class ImFileVO {

    @ApiModelProperty(value = "鏂囦欢ID", example = "1")
    private Long id;

    @ApiModelProperty(value = "鏂囦欢鍚嶇О", example = "avatar.jpg")
    private String fileName;

    @ApiModelProperty(value = "鍘熷鏂囦欢鍚?, example = "鎴戠殑澶村儚.jpg")
    private String originalName;

    @ApiModelProperty(value = "鏂囦欢璺緞", example = "/files/1704451200000_1.jpg")
    private String filePath;

    @ApiModelProperty(value = "鏂囦欢URL", example = "http://localhost:8080/files/1704451200000_1.jpg")
    private String fileUrl;

    @ApiModelProperty(value = "鏂囦欢澶у皬锛堝瓧鑺傦級", example = "102400")
    private Long fileSize;

    @ApiModelProperty(value = "鏂囦欢澶у皬鎻忚堪", example = "100KB")
    private String fileSizeDesc;

    @ApiModelProperty(value = "鏂囦欢绫诲瀷", example = "image")
    private String fileType;

    @ApiModelProperty(value = "鏂囦欢绫诲瀷鎻忚堪", example = "鍥剧墖")
    private String fileTypeDesc;

    @ApiModelProperty(value = "鏂囦欢鎵╁睍鍚?, example = ".jpg")
    private String fileExt;

    @ApiModelProperty(value = "鏂囦欢鍒嗙被", example = "avatar")
    private String category;

    @ApiModelProperty(value = "鏂囦欢鏍囩", example = "澶村儚,涓汉璧勬枡")
    private String tags;

    @ApiModelProperty(value = "鏂囦欢鎻忚堪", example = "鐢ㄦ埛澶村儚鍥剧墖")
    private String description;

    @ApiModelProperty(value = "涓婁紶鑰呯敤鎴稩D", example = "1")
    private Long uploaderId;

    @ApiModelProperty(value = "涓婁紶鑰呯敤鎴峰悕", example = "admin")
    private String uploaderUsername;

    @ApiModelProperty(value = "涓婁紶鑰呮樀绉?, example = "绠＄悊鍛?)
    private String uploaderNickname;

    @ApiModelProperty(value = "鏂囦欢鐘舵€?, example = "ACTIVE")
    private String status;

    @ApiModelProperty(value = "鏂囦欢鐘舵€佹弿杩?, example = "姝ｅ父")
    private String statusDesc;

    @ApiModelProperty(value = "涓嬭浇娆℃暟", example = "5")
    private Integer downloadCount;

    @ApiModelProperty(value = "鏄惁鍙笅杞?, example = "true")
    private Boolean canDownload;

    @ApiModelProperty(value = "鏄惁鍥剧墖", example = "true")
    private Boolean isImage;

    @ApiModelProperty(value = "涓婁紶鏃堕棿", example = "2024-01-05 10:30:00")
    private LocalDateTime uploadTime;

    @ApiModelProperty(value = "鍒涘缓鏃堕棿", example = "2024-01-05 10:30:00")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "鏇存柊鏃堕棿", example = "2024-01-05 10:30:00")
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
