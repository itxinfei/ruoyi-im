package com.ruoyi.im.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 数据备份实体
 *
 * 用于记录系统数据备份的信息
 *
 * @author ruoyi
 */
@TableName("im_backup")
public class ImBackup implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 备份ID，主键，自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 备份文件名 */
    @TableField("file_name")
    private String fileName;

    /** 备份描述 */
    @TableField("description")
    private String description;

    /** 备份文件路径 */
    @TableField("file_path")
    private String filePath;

    /** 备份文件大小（字节） */
    @TableField("file_size")
    private Long fileSize;

    /** 备份类型：full=全量, incremental=增量, user=用户数据 */
    @TableField("backup_type")
    private String backupType;

    /** 备份状态：pending=待执行, in_progress=进行中, completed=已完成, failed=失败 */
    @TableField("status")
    private String status;

    /** 备份人ID */
    @TableField("creator_id")
    private Long creatorId;

    /** 备份人名称 */
    @TableField("creator_name")
    private String creatorName;

    /** 错误信息（失败时记录） */
    @TableField("error_message")
    private String errorMessage;

    /** 创建时间 */
    @TableField("create_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /** 完成时间 */
    @TableField("complete_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime completeTime;

    // ==================== Getters and Setters ====================

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public Long getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(Long creatorId) {
        this.creatorId = creatorId;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getCompleteTime() {
        return completeTime;
    }

    public void setCompleteTime(LocalDateTime completeTime) {
        this.completeTime = completeTime;
    }
}
