package com.ruoyi.system.domain;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * IM文件资源对象 im_file_asset
 * 
 * @author ruoyi
 * @date 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ImFileAsset extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 文件ID */
    private Long id;

    /** 文件名 */
    @Excel(name = "文件名")
    private String fileName;

    /** 文件大小 */
    @Excel(name = "文件大小")
    private Long fileSize;

    /** 文件类型 */
    @Excel(name = "文件类型")
    private String fileType;

    /** 文件路径 */
    private String filePath;

    /** 上传者ID */
    @Excel(name = "上传者ID")
    private Long uploaderId;

    /** 上传时间 */
    @Excel(name = "上传时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private java.util.Date uploadTime;

    /** 文件状态 */
    @Excel(name = "文件状态")
    private String status;

    /** 创建时间 */
    @Excel(name = "创建时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private java.util.Date createTime;

    /** 更新时间 */
    @Excel(name = "更新时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private java.util.Date updateTime;
}
