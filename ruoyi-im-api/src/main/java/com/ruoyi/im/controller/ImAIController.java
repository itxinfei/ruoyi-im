package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.dto.ai.ChatRequest;
import com.ruoyi.im.dto.ai.ChatResponse;
import com.ruoyi.im.dto.ai.SummaryRequest;
import com.ruoyi.im.dto.ai.SummaryResponse;
import com.ruoyi.im.service.ImAIService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * AI助手控制器
 *
 * @author ruoyi
 */

@RestController
@RequestMapping("/api/im/ai")
public class ImAIController {

    @Resource
    private ImAIService aiService;

    /**
     * AI聊天对话
     */
    
    @PostMapping("/chat")
    public Result<ChatResponse> chat(@Validated @RequestBody ChatRequest request) {
        ChatResponse response = aiService.chat(request);
        return Result.success(response);
    }

    /**
     * 生成文档摘要
     */
    
    @PostMapping("/summarize")
    public Result<SummaryResponse> summarize(@Validated @RequestBody SummaryRequest request) {
        SummaryResponse response = aiService.summarize(request);
        return Result.success(response);
    }

    /**
     * 清除对话上下文
     */
    
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
    
    @GetMapping("/models")
    public Result<String[]> getSupportedModels() {
        String[] models = aiService.getSupportedModels();
        return Result.success(models);
    }
}

