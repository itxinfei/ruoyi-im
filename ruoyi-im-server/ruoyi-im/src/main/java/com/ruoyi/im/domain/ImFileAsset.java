package com.ruoyi.im.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 文件资产对象 im_file_asset
 * 
 * @author ruoyi
 * @date 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("im_file_asset")
public class ImFileAsset extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 文件ID */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 上传者用户ID */
    @Excel(name = "上传者用户ID")
    @TableField("uploader_id")
    private Long uploaderId;

    /** 文件名 */
    @Excel(name = "文件名")
    @TableField("filename")
    private String filename;

    /** 文件扩展名 */
    @Excel(name = "文件扩展名")
    @TableField("ext")
    private String ext;

    /** MIME类型 */
    @Excel(name = "MIME类型")
    @TableField("mime")
    private String mime;

    /** 文件大小（字节） */
    @Excel(name = "文件大小")
    @TableField("size")
    private Long size;

    /** 文件MD5值 */
    @Excel(name = "文件MD5值")
    @TableField("md5")
    private String md5;

    /** 存储路径 */
    @Excel(name = "存储路径")
    @TableField("storage_path")
    private String storagePath;

    /** 是否需要水印（0否 1是） */
    @Excel(name = "是否需要水印", readConverterExp = "0=否,1=是")
    @TableField("watermark")
    private Boolean watermark;

    /** 上传者用户名（非数据库字段） */
    @TableField(exist = false)
    private String uploaderUserName;

    /** 上传者昵称（非数据库字段） */
    @TableField(exist = false)
    private String uploaderNickName;

    /** 文件大小（格式化，非数据库字段） */
    @TableField(exist = false)
    private String sizeFormatted;

    /** 下载URL（非数据库字段） */
    @TableField(exist = false)
    private String downloadUrl;

    /** 预览URL（非数据库字段） */
    @TableField(exist = false)
    private String previewUrl;

    /** 是否可预览（非数据库字段） */
    @TableField(exist = false)
    private Boolean previewable;

    /**
     * 格式化文件大小
     * @return 格式化后的文件大小
     */
    public String getSizeFormatted() {
        if (size == null) {
            return "0 B";
        }
        
        String[] units = {"B", "KB", "MB", "GB", "TB"};
        double fileSize = size.doubleValue();
        int unitIndex = 0;
        
        while (fileSize >= 1024 && unitIndex < units.length - 1) {
            fileSize /= 1024;
            unitIndex++;
        }
        
        return String.format("%.2f %s", fileSize, units[unitIndex]);
    }

    /**
     * 检查文件是否可预览
     * @return 是否可预览
     */
    public Boolean getPreviewable() {
        if (ext == null) {
            return false;
        }
        
        String extension = ext.toLowerCase();
        // 图片文件可预览
        if (extension.matches("jpg|jpeg|png|gif|bmp|webp")) {
            return true;
        }
        // 文档文件可预览
        if (extension.matches("pdf|txt|doc|docx|xls|xlsx|ppt|pptx")) {
            return true;
        }
        
        return false;
    }
}