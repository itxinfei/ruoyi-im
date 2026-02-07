package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.dto.ai.ChatRequest;
import com.ruoyi.im.dto.ai.ChatResponse;
import com.ruoyi.im.dto.ai.SummaryRequest;
import com.ruoyi.im.dto.ai.SummaryResponse;
import com.ruoyi.im.service.ImAIService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * AI助手控制器
 * 提供AI对话、文档摘要生成、对话上下文管理等功能
 *
 * @author ruoyi
 */
@Tag(name = "AI助手", description = "AI智能助手接口")
@RestController
@RequestMapping("/api/im/ai")
public class ImAIController {

    private final ImAIService aiService;

    /**
     * 构造器注入依赖
     *
     * @param aiService AI服务
     */
    public ImAIController(ImAIService aiService) {
        this.aiService = aiService;
    }

    /**
     * AI聊天对话
     * 与AI助手进行多轮对话，支持上下文记忆
     *
     * @param request 对话请求，包含消息内容、会话ID、模型参数等
     * @return AI响应结果，包含AI回复内容和使用的token数量
     * @apiNote 对话上下文会根据conversationId进行记忆，支持多轮对话
     *          参数校验由Spring验证框架处理，校验失败返回400错误
     */
    @Operation(summary = "AI聊天", description = "与AI助手进行对话")
    @PostMapping("/chat")
    public Result<ChatResponse> chat(@Validated @RequestBody ChatRequest request) {
        ChatResponse response = aiService.chat(request);
        return Result.success(response);
    }

    /**
     * 生成文档摘要
     * 使用AI生成长文本摘要，提取关键信息
     *
     * @param request 摘要请求，包含文档内容和摘要长度要求
     * @return 摘要响应，包含生成的摘要内容
     * @apiNote 支持自定义摘要长度和语言
     */
    @Operation(summary = "生成摘要", description = "使用AI生成文档摘要")
    @PostMapping("/summarize")
    public Result<SummaryResponse> summarize(@Validated @RequestBody SummaryRequest request) {
        SummaryResponse response = aiService.summarize(request);
        return Result.success(response);
    }

    /**
     * 清除对话上下文
     * 清除指定会话的AI对话上下文记忆
     *
     * @param conversationId 会话ID
     * @param userId         用户ID
     * @return 操作结果
     * @apiNote 清除后该会话的上下文记忆将被清空，AI无法记住之前的对话内容
     */
    @Operation(summary = "清除对话", description = "清除AI对话的上下文记忆")
    @DeleteMapping("/conversation/{conversationId}")
    public Result<Void> clearConversation(
            @Parameter(description = "会话ID") @PathVariable("conversationId") String conversationId,
            @Parameter(description = "用户ID") @RequestParam("userId") Long userId) {
        aiService.clearConversation(conversationId, userId);
        return Result.success();
    }

    /**
     * 获取支持的AI模型列表
     * 查询系统当前支持的所有AI模型
     *
     * @return 支持的模型名称数组，如 ["gpt-4", "gpt-3.5-turbo"]
     * @apiNote 可用模型取决于系统配置和API密钥设置
     */
    @Operation(summary = "获取模型列表", description = "获取支持的AI模型列表")
    @GetMapping("/models")
    public Result<String[]> getSupportedModels() {
        String[] models = aiService.getSupportedModels();
        return Result.success(models);
    }
}
