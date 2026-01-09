package com.ruoyi.im.dto.document;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 文档分享请求
 *
 * @author ruoyi
 */
@Data
public class ImDocumentShareRequest {

    /**
     * 文档ID
     */
    @NotNull(message = "文档ID不能为空")
    private Long documentId;

    /**
     * 分享给的用户ID列表
     */
    @NotEmpty(message = "请选择要分享的用户")
    private List<Long> userIds;

    /**
     * 权限（view=查看, edit=编辑）
     */
    private String permission = "view";

    /**
     * 是否允许复制
     */
    private Boolean allowCopy = true;

    /**
     * 是否允许下载
     */
    private Boolean allowDownload = true;

    /**
     * 过期天数（null表示永不过期）
     */
    private Integer expireDays;
}
