package com.ruoyi.im.service;

import java.util.List;
import java.util.Map;

/**
 * 消息管理服务接口（管理员操作）
 */
public interface ImMessageManagerService {

    /**
     * 管理员删除消息
     */
    void adminDeleteMessage(Long messageId);

    /**
     * 管理员批量删除消息
     */
    Map<String, Integer> adminBatchDeleteMessages(List<Long> messageIds);

    /**
     * 清空会话聊天记录
     */
    void clearConversationMessages(Long conversationId, Long userId);
}
