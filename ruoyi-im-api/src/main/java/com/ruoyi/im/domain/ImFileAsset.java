package com.ruoyi.im.domain;

import java.time.LocalDateTime;

/**
 * 鏂囦欢璧勬簮瀹炰綋
 * 
 * @author ruoyi
 */
public class ImFileAsset {
    /**
     * 鏂囦欢ID
     */
    private Long id;
    
    /**
     * 鏂囦欢鍚?     */
    private String fileName;
    
    /**
     * 鏂囦欢璺緞
     */
    private String filePath;
    
    /**
     * 鏂囦欢澶у皬锛堝瓧鑺傦級
     */
    private Long fileSize;
    
    /**
     * 鏂囦欢绫诲瀷
     */
    private String fileType;
    
    /**
     * 鏂囦欢鎵╁睍鍚?     */
    private String fileExt;
    
    /**
     * 鏂囦欢MD5鍊?     */
    private String md5;
    
    /**
     * 涓婁紶鑰呯敤鎴稩D
     */
    private Long uploaderId;
    
    /**
     * 涓嬭浇娆℃暟
     */
    private Integer downloadCount;
    
    /**
     * 涓嬭浇閾炬帴杩囨湡鏃堕棿
     */
    private LocalDateTime downloadExpireTime;
    
    /**
     * 鐘舵€侊紙ACTIVE姝ｅ父 DELETED宸插垹闄わ級
     */
    private String status;
    
    /**
     * 鏂囦欢鍒嗙被
     */
    private String category;

    /**
     * 鏂囦欢鎻忚堪
     */
    private String description;

    /**
     * 鏂囦欢鏍囩
     */
    private String tags;

    /**
     * 鍘熷鏂囦欢鍚?     */
    private String originalName;

    /**
     * 鏂囦欢URL
     */
    private String fileUrl;

    /**
     * 涓婁紶鏃堕棿
     */
    private LocalDateTime uploadTime;

    /**
     * 鏇存柊鏃堕棿
     */
    private LocalDateTime updateTime;

    /**
     * 鍒涘缓鏃堕棿
     */
    private LocalDateTime createTime;

    // Getters and Setters
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

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
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

    public String getFileExt() {
        return fileExt;
    }

    public void setFileExt(String fileExt) {
        this.fileExt = fileExt;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public Long getUploaderId() {
        return uploaderId;
    }

    public void setUploaderId(Long uploaderId) {
        this.uploaderId = uploaderId;
    }

    public Integer getDownloadCount() {
        return downloadCount;
    }

    public void setDownloadCount(Integer downloadCount) {
        this.downloadCount = downloadCount;
    }

    public LocalDateTime getDownloadExpireTime() {
        return downloadExpireTime;
    }

    public void setDownloadExpireTime(LocalDateTime downloadExpireTime) {
        this.downloadExpireTime = downloadExpireTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getOriginalName() {
        return originalName;
    }

    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public LocalDateTime getUploadTime() {
        return uploadTime;
    }

    public void setUploadTime(LocalDateTime uploadTime) {
        this.uploadTime = uploadTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
