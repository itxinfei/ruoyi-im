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
 * 群组文件实体
 *
 * @author ruoyi
 */
@TableName("im_group_file")
@Data
public class ImGroupFile implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 文件ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 群组ID */
    @TableField("group_id")
    private Long groupId;

    /** 文件资产ID */
    @TableField("file_id")
    private Long fileId;

    /** 文件名称 */
    @TableField("file_name")
    private String fileName;

    /** 文件类型 */
    @TableField("file_type")
    private String fileType;

    /** 文件大小 */
    @TableField("file_size")
    private Long fileSize;

    /** 文件分类 */
    private String category;

    /** 下载权限 */
    private String permission;

    /** 上传者ID */
    @TableField("uploader_id")
    private Long uploaderId;

    /** 上传者名称 */
    @TableField("uploader_name")
    private String uploaderName;

    /** 下载次数 */
    @TableField("download_count")
    private Integer downloadCount;

    /** 状态：1=正常, 0=已删除 */
    private Integer status;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    // ==================== 非数据库字段 ====================

    /** 文件URL */
    @TableField(exist = false)
    private String fileUrl;

    /** 文件缩略图URL */
    @TableField(exist = false)
    private String thumbnailUrl;
}
