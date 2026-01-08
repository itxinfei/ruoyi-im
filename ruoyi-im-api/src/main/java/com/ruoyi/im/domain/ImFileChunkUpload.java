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
 * 分片上传记录实体
 *
 * @author ruoyi
 */
@TableName("im_file_chunk_upload")
@Data
public class ImFileChunkUpload implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 上传ID */
    @TableField("upload_id")
    private String uploadId;

    /** 原始文件名 */
    @TableField("file_name")
    private String fileName;

    /** 文件大小 */
    @TableField("file_size")
    private Long fileSize;

    /** 文件MD5值 */
    @TableField("file_md5")
    private String fileMd5;

    /** 分片大小 */
    @TableField("chunk_size")
    private Integer chunkSize;

    /** 总分片数 */
    @TableField("total_chunks")
    private Integer totalChunks;

    /** 已上传分片数 */
    @TableField("uploaded_chunks")
    private Integer uploadedChunks;

    /** 状态 */
    private String status;

    /** 上传用户ID */
    @TableField("user_id")
    private Long userId;

    /** 最终文件路径 */
    @TableField("file_path")
    private String filePath;

    /** 最终文件URL */
    @TableField("file_url")
    private String fileUrl;

    /** 错误信息 */
    @TableField("error_message")
    private String errorMessage;

    /** 过期时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("expire_time")
    private LocalDateTime expireTime;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
