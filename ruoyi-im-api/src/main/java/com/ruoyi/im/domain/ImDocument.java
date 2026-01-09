package com.ruoyi.im.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 在线文档实体
 *
 * @author ruoyi
 */
@Data
@TableName("im_document")
public class ImDocument implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 文档ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 文档标题
     */
    private String title;

    /**
     * 文档类型（doc=文档, sheet=表格, mind=思维导图）
     */
    private String docType;

    /**
     * 文档内容（HTML/JSON格式）
     */
    private String content;

    /**
     * 文档状态（draft=草稿, published=已发布, archived=已归档）
     */
    private String status;

    /**
     * 所有者ID
     */
    private Long ownerId;

    /**
     * 所属文件夹ID
     */
    private Long folderId;

    /**
     * 是否收藏
     */
    private Boolean isStarred;

    /**
     * 是否删除（回收站）
     */
    private Boolean isDeleted;

    /**
     * 删除时间
     */
    private LocalDateTime deletedTime;

    /**
     * 自动删除天数（回收站）
     */
    private Integer autoDeleteDays;

    /**
     * 最后修改者ID
     */
    private Long lastModifiedBy;

    /**
     * 最后修改时间
     */
    private LocalDateTime lastModifiedTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 版本号
     */
    private Integer version;

    /**
     * 父文档ID（用于文档关联）
     */
    private Long parentId;
}
