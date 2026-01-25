package com.ruoyi.im.service;

import com.ruoyi.im.dto.ai.ChatRequest;
import com.ruoyi.im.dto.ai.ChatResponse;
import com.ruoyi.im.dto.ai.SummaryRequest;
import com.ruoyi.im.dto.ai.SummaryResponse;

/**
 * AI助手服务接口
 *
 * @author ruoyi
 */
public interface ImAIService {

    /**
     * AI聊天对话
     *
     * @param request 聊天请求
     * @return 聊天响应
     */
    ChatResponse chat(ChatRequest request);

    /**
     * 生成文档摘要
     *
     * @param request 摘要请求
     * @return 摘要响应
     */
    SummaryResponse summarize(SummaryRequest request);

    /**
     * 清除对话上下文
     *
     * @param conversationId 会话ID
     * @param userId         用户ID
     */
    void clearConversation(String conversationId, Long userId);

    /**
     * 获取支持的AI模型列表
     *
     * @return 模型列表
     */
    String[] getSupportedModels();
}
