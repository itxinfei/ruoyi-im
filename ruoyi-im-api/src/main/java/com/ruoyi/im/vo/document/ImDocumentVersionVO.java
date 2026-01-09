package com.ruoyi.im.vo.document;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文档版本VO
 *
 * @author ruoyi
 */
@Data
public class ImDocumentVersionVO {

    /**
     * 版本ID
     */
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
     * 文件大小
     */
    private Long fileSize;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 是否可以恢复
     */
    private Boolean canRestore;
}
