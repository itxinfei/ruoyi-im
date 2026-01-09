package com.ruoyi.im.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

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
@Data
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

}
