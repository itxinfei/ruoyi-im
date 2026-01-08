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
 * 分片文件详情实体
 *
 * @author ruoyi
 */
@TableName("im_file_chunk_detail")
@Data
public class ImFileChunkDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 上传ID */
    @TableField("upload_id")
    private String uploadId;

    /** 分片序号 */
    @TableField("chunk_number")
    private Integer chunkNumber;

    /** 分片大小 */
    @TableField("chunk_size")
    private Integer chunkSize;

    /** 分片MD5值 */
    @TableField("chunk_md5")
    private String chunkMd5;

    /** 分片文件存储路径 */
    @TableField("chunk_path")
    private String chunkPath;

    /** 状态 */
    private String status;

    /** 重试次数 */
    @TableField("retry_count")
    private Integer retryCount;

    /** 错误信息 */
    @TableField("error_message")
    private String errorMessage;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
