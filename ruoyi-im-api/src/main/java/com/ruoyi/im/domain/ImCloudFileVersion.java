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
 * 企业云盘文件版本实体
 *
 * @author ruoyi
 */
@TableName("im_cloud_file_version")
@Data

public class ImCloudFileVersion implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 版本ID
     */
    @TableId(type = IdType.AUTO)
    
    private Long id;

    /**
     * 云盘文件ID
     */
    
    @TableField("cloud_file_id")
    private Long cloudFileId;

    /**
     * 文件资产ID
     */
    
    @TableField("file_asset_id")
    private Long fileAssetId;

    /**
     * 版本号
     */
    
    @TableField("version_number")
    private Integer versionNumber;

    /**
     * 版本说明
     */
    
    @TableField("version_note")
    private String versionNote;

    /**
     * 文件大小
     */
    
    @TableField("file_size")
    private Long fileSize;

    /**
     * 操作者ID
     */
    
    @TableField("operator_id")
    private Long operatorId;

    /**
     * 操作者名称（冗余）
     */
    
    @TableField("operator_name")
    private String operatorName;

    /**
     * 创建时间
     */
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("create_time")
    private LocalDateTime createTime;
}

