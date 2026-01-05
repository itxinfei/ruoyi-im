package com.ruoyi.im.service;

import com.ruoyi.im.domain.ImMessage;

/**
 * 消息推送服务接口
 * 
 * 负责将消息实时推送给在线用户
 * 
 * @author ruoyi
 */
public interface ImMessagePushService {

    /**
     * 推送消息给指定用户
     * 
     * @param userId 用户ID
     * @param message 消息对象
     */
    void pushMessageToUser(Long userId, ImMessage message);

    /**
     * 推送消息给会话中的所有用户
     * 
     * @param conversationId 会话ID
     * @param message 消息对象
     */
    void pushMessageToConversation(Long conversationId, ImMessage message);

    /**
     * 推送消息给群组中的所有用户
     * 
     * @param groupId 群组ID
     * @param message 消息对象
     */
    void pushMessageToGroup(Long groupId, ImMessage message);

    /**
     * 推送在线状态变化
     * 
     * @param userId 用户ID
     * @param online 是否在线
     */
    void pushOnlineStatus(Long userId, boolean online);

    /**
     * 推送正在输入状态
     * 
     * @param conversationId 会话ID
     * @param userId 用户ID
     * @param isTyping 是否正在输入
     */
    void pushTypingStatus(Long conversationId, Long userId, boolean isTyping);

    /**
     * 推送消息已读回执
     * 
     * @param messageId 消息ID
     * @param userId 用户ID
     */
    void pushReadReceipt(Long messageId, Long userId);

    /**
     * 推送消息撤回通知
     * 
     * @param messageId 消息ID
     * @param conversationId 会话ID
     */
    void pushMessageRevoke(Long messageId, Long conversationId);

    /**
     * 推送消息反应
     * 
     * @param messageId 消息ID
     * @param reaction 反应表情
     * @param userId 用户ID
     * @param added 是否添加
     */
    void pushMessageReaction(Long messageId, String reaction, Long userId, boolean added);
}
