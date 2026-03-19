package com.ruoyi.im.dto.ai;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 文档摘要请求DTO
 *
 * @author ruoyi
 */
@Data

public class SummaryRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 待摘要的文本内容
     */
    
    @NotBlank(message = "文本内容不能为空")
    private String content;

    /**
     * 摘要类型
     * brief: 简要摘要（一句话）
     * normal: 标准摘要（3-5句话）
     * detailed: 详细摘要（包含要点列表）
     */
    
    private String summaryType = "normal";

    /**
     * 摘要语言
     */
    
    private String language = "zh";
}

