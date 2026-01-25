package com.ruoyi.im.dto.ai;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * AI聊天响应VO
 *
 * @author ruoyi
 */
@Data
@Schema(description = "AI聊天响应")
public class ChatResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * AI回复内容
     */
    @Schema(description = "AI回复内容")
    private String content;

    /**
     * 会话ID（用于后续对话）
     */
    @Schema(description = "会话ID")
    private String conversationId;

    /**
     * 使用的AI模型
     */
    @Schema(description = "使用的AI模型")
    private String model;

    /**
     * 消耗的token数
     */
    @Schema(description = "消耗的token数")
    private Integer tokensUsed;

    /**
     * 处理耗时（毫秒）
     */
    @Schema(description = "处理耗时")
    private Long processingTime;
}
