package com.ruoyi.im.dto.document;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 创建文档请求
 *
 * @author ruoyi
 */
@Data
public class ImDocumentCreateRequest {

    /**
     * 文档标题
     */
    @NotBlank(message = "文档标题不能为空")
    private String title;

    /**
     * 文档类型（doc=文档, sheet=表格, mind=思维导图）
     */
    @NotNull(message = "文档类型不能为空")
    private String docType;

    /**
     * 文档内容
     */
    private String content;

    /**
     * 所属文件夹ID
     */
    private Long folderId;

    /**
     * 父文档ID
     */
    private Long parentId;
}
