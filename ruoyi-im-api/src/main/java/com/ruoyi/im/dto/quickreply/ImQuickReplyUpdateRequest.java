package com.ruoyi.im.dto.quickreply;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 更新快捷回复请求DTO
 *
 * @author ruoyi
 */
@Data
public class ImQuickReplyUpdateRequest {

    /** 快捷回复ID */
    @NotBlank(message = "快捷回复ID不能为空")
    private Long id;

    /** 回复内容 */
    private String content;

    /** 分类 */
    private String category;

    /** 标题 */
    private String title;

    /** 排序 */
    private Integer sortOrder;
}
