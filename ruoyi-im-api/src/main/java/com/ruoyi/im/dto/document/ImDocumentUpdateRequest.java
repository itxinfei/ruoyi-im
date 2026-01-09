package com.ruoyi.im.dto.document;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 更新文档请求
 *
 * @author ruoyi
 */
@Data
public class ImDocumentUpdateRequest {

    /**
     * 文档ID
     */
    @NotNull(message = "文档ID不能为空")
    private Long id;

    /**
     * 文档标题
     */
    private String title;

    /**
     * 文档内容
     */
    private String content;

    /**
     * 是否自动保存（不创建新版本）
     */
    private Boolean autoSave;

    /**
     * 修改说明
     */
    private String changeDescription;
}
