package com.ruoyi.im.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 文档版本实体
 *
 * @author ruoyi
 */
@Data
@TableName("im_document_version")
public class ImDocumentVersion implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 版本ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 文档ID
     */
    private Long documentId;

    /**
     * 版本号
     */
    private Integer version;

    /**
     * 文档内容
     */
    private String content;

    /**
     * 文档标题快照
     */
    private String titleSnapshot;

    /**
     * 修改者ID
     */
    private Long modifiedBy;

    /**
     * 修改者名称
     */
    private String modifiedByName;

    /**
     * 修改说明
     */
    private String changeDescription;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
