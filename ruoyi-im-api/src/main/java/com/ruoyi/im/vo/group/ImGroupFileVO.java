package com.ruoyi.im.vo.group;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 群组文件视图对象
 *
 * @author ruoyi
 */
@Data
public class ImGroupFileVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 文件ID
     */
    private Long id;

    /**
     * 群组ID
     */
    private Long groupId;

    /**
     * 文件资产ID
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
     * 格式化文件大小
     */
    private String fileSizeFormat;

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
     * 上传者名称
     */
    private String uploaderName;

    /**
     * 下载次数
     */
    private Integer downloadCount;

    /**
     * 文件URL
     */
    private String fileUrl;

    /**
     * 缩略图URL
     */
    private String thumbnailUrl;

    /**
     * 是否可以删除（当前用户是否有权限）
     */
    private Boolean canDelete;

    /**
     * 是否可以下载（当前用户是否有权限）
     */
    private Boolean canDownload;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
