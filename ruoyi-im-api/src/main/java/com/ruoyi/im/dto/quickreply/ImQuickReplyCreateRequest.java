package com.ruoyi.im.dto.quickreply;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 创建快捷回复请求DTO
 *
 * @author ruoyi
 */
@Data
public class ImQuickReplyCreateRequest {

    /** 回复内容 */
    @NotBlank(message = "回复内容不能为空")
    private String content;

    /** 分类：greeting问候语/ending结束语/common常用语/custom自定义 */
    private String category;

    /** 标题（可选） */
    private String title;

    /** 排序 */
    private Integer sortOrder;
}
