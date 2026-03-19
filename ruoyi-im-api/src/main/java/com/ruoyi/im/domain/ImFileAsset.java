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
 * 文件资产实体
 *
 * @author ruoyi
 */
@TableName("im_file_asset")
@Data

public class ImFileAsset implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    
    private Long id;

    
    @TableField("file_name")
    private String fileName;

    
    @TableField("file_path")
    private String filePath;

    
    @TableField("file_size")
    private Long fileSize;

    
    @TableField("file_type")
    private String fileType;

    
    @TableField("file_ext")
    private String fileExt;

    
    private String md5;

    
    @TableField("uploader_id")
    private Long uploaderId;

    
    @TableField("download_count")
    private Integer downloadCount;

    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("download_expire_time")
    private LocalDateTime downloadExpireTime;

    
    private String status;

    
    @TableField("create_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}

