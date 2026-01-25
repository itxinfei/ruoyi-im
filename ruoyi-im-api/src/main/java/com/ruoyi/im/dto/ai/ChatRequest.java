package com.ruoyi.im.dto.ai;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * AI聊天请求DTO
 *
 * @author ruoyi
 */
@Data
@Schema(description = "AI聊天请求")
public class ChatRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户发送的消息内容
     */
    @Schema(description = "消息内容", required = true)
    @NotBlank(message = "消息内容不能为空")
    private String content;

    /**
     * 会话ID（用于上下文记忆）
     * 首次对话为空，后续对话传递返回的conversationId
     */
    @Schema(description = "会话ID")
    private String conversationId;

    /**
     * 用户ID
     */
    @Schema(description = "用户ID", required = true)
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /**
     * AI模型（可选，默认使用系统配置）
     * 支持：gpt-3.5-turbo、gpt-4、qwen(通义千问)、wenxin(文心一言)等
     */
    @Schema(description = "AI模型")
    private String model;

    /**
     * 最大token数（控制回复长度）
     */
    @Schema(description = "最大token数")
    private Integer maxTokens;

    /**
     * 温度参数（0-1，控制回复的随机性）
     */
    @Schema(description = "温度参数")
    private Double temperature;
}
