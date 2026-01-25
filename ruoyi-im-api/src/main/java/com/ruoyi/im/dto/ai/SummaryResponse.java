package com.ruoyi.im.dto.ai;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 文档摘要响应VO
 *
 * @author ruoyi
 */
@Data
@Schema(description = "文档摘要响应")
public class SummaryResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 摘要内容
     */
    @Schema(description = "摘要内容")
    private String summary;

    /**
     * 关键点列表
     */
    @Schema(description = "关键点列表")
    private List<String> keyPoints;

    /**
     * 原文长度
     */
    @Schema(description = "原文长度")
    private Integer originalLength;

    /**
     * 摘要长度
     */
    @Schema(description = "摘要长度")
    private Integer summaryLength;
}
