package com.ruoyi.im.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 群组文件实体
 *
 * 用于存储IM系统中的群组共享文件信息
 * 支持文件分类、权限控制、下载统计等功能
 *
 * @author ruoyi
 */
@TableName("im_group_file")
@Data
public class ImGroupFile implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 文件ID，主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 群组ID
     */
    private Long groupId;

    /**
     * 文件资产ID（关联im_file_asset表）
     */
    private Long fileId;

    /**
     * 文件名称
     */
    private String fileName;

    /**
     * 文件类型：image/video/audio/document/other
     */
    private String fileType;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * 文件分类：default/document/image/video/audio
     */
    private String category;

    /**
     * 下载权限：ALL=所有人, ADMIN=仅管理员
     */
    private String permission;

    /**
     * 上传者ID
     */
    private Long uploaderId;

    /**
     * 上传者名称（非数据库字段，冗余显示）
     */
    private String uploaderName;

    /**
     * 下载次数
     */
    private Integer downloadCount;

    /**
     * 状态：1=正常, 0=已删除
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 文件URL（非数据库字段，关联查询）
     */
    private String fileUrl;

    /**
     * 文件缩略图URL（非数据库字段，关联查询）
     */
    private String thumbnailUrl;
}
