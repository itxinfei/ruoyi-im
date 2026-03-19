package com.ruoyi.im.dto.ai;

import lombok.Data;

import java.io.Serializable;

/**
 * AI聊天响应VO
 *
 * @author ruoyi
 */
@Data

public class ChatResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * AI回复内容
     */
    
    private String content;

    /**
     * 会话ID（用于后续对话）
     */
    
    private String conversationId;

    /**
     * 使用的AI模型
     */
    
    private String model;

    /**
     * 消耗的token数
     */
    
    private Integer tokensUsed;

    /**
     * 处理耗时（毫秒）
     */
    
    private Long processingTime;
}

