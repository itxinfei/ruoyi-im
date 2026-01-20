package com.ruoyi.web.domain;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 数据备份记录实体
 *
 * <p>记录系统数据备份的历史信息，包括备份时间、文件路径、文件大小等</p>
 * <p>支持手动备份和自动定时备份两种方式</p>
 *
 * @author ruoyi
 */
public class ImBackup extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 备份ID，主键 */
    private Long id;

    /** 备份文件名称 */
    @Excel(name = "文件名称", width = 40)
    private String fileName;

    /** 备份文件路径 */
    private String filePath;

    /** 备份文件大小（字节） */
    @Excel(name = "文件大小", width = 15)
    private Long fileSize;

    /** 备份类型: MANUAL=手动备份, AUTO_DAILY=每日自动, AUTO_WEEKLY=每周自动 */
    @Excel(name = "备份类型", width = 15, readConverterExp = "MANUAL=手动备份,AUTO_DAILY=每日自动,AUTO_WEEKLY=每周自动")
    private String backupType;

    /** 备份状态: SUCCESS=成功, FAILED=失败, IN_PROGRESS=进行中 */
    @Excel(name = "备份状态", width = 10, readConverterExp = "SUCCESS=成功,FAILED=失败,IN_PROGRESS=进行中")
    private String status;

    /** 备份进度（百分比） */
    private Integer progress;

    /** 错误信息（失败时记录） */
    private String errorMessage;

    /** 备份完成时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "完成时间", width = 25)
    private LocalDateTime completedTime;

    /** 备份操作人 */
    @Excel(name = "操作人", width = 15)
    private String operator;

    /** 备份描述 */
    @Excel(name = "备份描述", width = 40)
    private String description;

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

    public String getBackupType() {
        return backupType;
    }

    public void setBackupType(String backupType) {
        this.backupType = backupType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getProgress() {
        return progress;
    }

    public void setProgress(Integer progress) {
        this.progress = progress;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public LocalDateTime getCompletedTime() {
        return completedTime;
    }

    public void setCompletedTime(LocalDateTime completedTime) {
        this.completedTime = completedTime;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
