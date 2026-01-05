package com.ruoyi.im.domain;

import java.time.LocalDateTime;

/**
 * 审计导出请求对象
 * 
 * @author ruoyi
 */
public class ImAuditExportRequest {
    private Long id;
    private Long userId;
    private String exportType;
    private String exportStatus;
    private String exportParams;
    private String exportUrl;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getExportType() {
        return exportType;
    }

    public void setExportType(String exportType) {
        this.exportType = exportType;
    }

    public String getExportStatus() {
        return exportStatus;
    }

    public void setExportStatus(String exportStatus) {
        this.exportStatus = exportStatus;
    }

    public String getExportParams() {
        return exportParams;
    }

    public void setExportParams(String exportParams) {
        this.exportParams = exportParams;
    }

    public String getExportUrl() {
        return exportUrl;
    }

    public void setExportUrl(String exportUrl) {
        this.exportUrl = exportUrl;
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
    
    // 添加一些常用的getter/setter方法以确保兼容性
    public void setStatus(String status) {
        this.exportStatus = status;
    }
    
    public String getStatus() {
        return this.exportStatus;
    }
}