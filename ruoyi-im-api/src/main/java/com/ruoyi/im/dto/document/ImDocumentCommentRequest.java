package com.ruoyi.im.dto.document;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 文档评论请求
 *
 * @author ruoyi
 */
@Data
public class ImDocumentCommentRequest {

    /**
     * 文档ID
     */
    @NotNull(message = "文档ID不能为空")
    private Long documentId;

    /**
     * 评论内容
     */
    @NotBlank(message = "评论内容不能为空")
    private String content;

    /**
     * 父评论ID（回复）
     */
    private Long parentId;

    /**
     * 选中的文本
     */
    private String selectedText;

    /**
     * 选中文本的偏移量
     */
    private Integer selectionOffset;
}
