package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.dto.ai.ChatRequest;
import com.ruoyi.im.dto.ai.ChatResponse;
import com.ruoyi.im.dto.ai.SummaryRequest;
import com.ruoyi.im.dto.ai.SummaryResponse;
import com.ruoyi.im.service.ImAIService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * AI助手控制器
 *
 * @author ruoyi
 */
@Tag(name = "AI助手", description = "AI智能助手接口")
@RestController
@RequestMapping("/api/im/ai")
public class ImAIController {

    @Resource
    private ImAIService aiService;

    /**
     * AI聊天对话
     */
    @Operation(summary = "AI聊天", description = "与AI助手进行对话")
    @PostMapping("/chat")
    public Result<ChatResponse> chat(@Validated @RequestBody ChatRequest request) {
        ChatResponse response = aiService.chat(request);
        return Result.success(response);
    }

    /**
     * 生成文档摘要
     */
    @Operation(summary = "生成摘要", description = "使用AI生成文档摘要")
    @PostMapping("/summarize")
    public Result<SummaryResponse> summarize(@Validated @RequestBody SummaryRequest request) {
        SummaryResponse response = aiService.summarize(request);
        return Result.success(response);
    }

    /**
     * 清除对话上下文
     */
    @Operation(summary = "清除对话", description = "清除AI对话的上下文记忆")
    @DeleteMapping("/conversation/{conversationId}")
    public Result<Void> clearConversation(
            @PathVariable("conversationId") String conversationId,
            @RequestParam("userId") Long userId) {
        aiService.clearConversation(conversationId, userId);
        return Result.success();
    }

    /**
     * 获取支持的AI模型
     */
    @Operation(summary = "获取模型列表", description = "获取支持的AI模型列表")
    @GetMapping("/models")
    public Result<String[]> getSupportedModels() {
        String[] models = aiService.getSupportedModels();
        return Result.success(models);
    }
}
