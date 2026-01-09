package com.ruoyi.im.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 文档评论实体
 *
 * @author ruoyi
 */
@Data
@TableName("im_document_comment")
public class ImDocumentComment implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 评论ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 文档ID
     */
    private Long documentId;

    /**
     * 评论者ID
     */
    private Long userId;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 父评论ID（用于回复）
     */
    private Long parentId;

    /**
     * 选中的文本（用于高亮评论）
     */
    private String selectedText;

    /**
     * 选中文本的偏移量
     */
    private Integer selectionOffset;

    /**
     * 是否已解决
     */
    private Boolean isResolved;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
