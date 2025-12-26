package com.ruoyi.im.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.im.domain.ImMessage;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 消息Service接口
 * 
 * @author ruoyi
 * @date 2024-01-01
 */
public interface IImMessageService extends IService<ImMessage> {

    /**
     * 发送消息
     * 
     * @param message 消息信息
     * @return 是否成功
     */
    boolean sendMessage(ImMessage message);

    /**
     * 撤回消息
     * 
     * @param messageId 消息ID
     * @param userId 操作用户ID
     * @return 是否成功
     */
    boolean revokeMessage(Long messageId, Long userId);

    /**
     * 批量撤回消息
     * 
     * @param messageIds 消息ID列表
     * @param userId 操作用户ID
     * @return 撤回数量
     */
    int revokeMessageBatch(List<Long> messageIds, Long userId);

    /**
     * 查询会话消息列表（分页）
     * 
     * @param conversationId 会话ID
     * @param userId 查询用户ID
     * @param lastMessageId 最后消息ID（用于分页）
     * @param limit 限制数量
     * @return 消息列表
     */
    List<ImMessage> selectConversationMessages(Long conversationId, Long userId, Long lastMessageId, int limit);

    /**
     * 获取消息详细信息
     * 
     * @param messageId 消息ID
     * @param userId 查询用户ID
     * @return 消息信息
     */
    ImMessage getMessageDetail(Long messageId, Long userId);

    /**
     * 统计会话消息数量
     * 
     * @param conversationId 会话ID
     * @return 消息数量
     */
    int countConversationMessages(Long conversationId);

    /**
     * 统计用户未读消息数量
     * 
     * @param conversationId 会话ID
     * @param userId 用户ID
     * @param lastReadMessageId 最后已读消息ID
     * @return 未读消息数量
     */
    int countUnreadMessages(Long conversationId, Long userId, Long lastReadMessageId);

    /**
     * 获取会话最后一条消息
     * 
     * @param conversationId 会话ID
     * @return 最后一条消息
     */
    ImMessage getLastMessage(Long conversationId);

    /**
     * 搜索消息
     * 
     * @param conversationId 会话ID（可选）
     * @param userId 搜索用户ID
     * @param keyword 搜索关键词
     * @param messageType 消息类型（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @return 消息列表
     */
    List<ImMessage> searchMessages(Long conversationId, Long userId, String keyword, String messageType, Date startTime, Date endTime);

    /**
     * 查询用户发送的消息
     * 
     * @param senderId 发送者ID
     * @param messageType 消息类型（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @return 消息列表
     */
    List<ImMessage> selectUserSentMessages(Long senderId, String messageType, Date startTime, Date endTime);

    /**
     * 批量更新消息状态
     * 
     * @param messageIds 消息ID列表
     * @param status 状态
     * @return 更新数量
     */
    int updateMessageStatusBatch(List<Long> messageIds, String status);

    /**
     * 查询敏感消息列表
     * 
     * @param sensitiveLevel 敏感级别（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @param limit 限制数量
     * @return 敏感消息列表
     */
    List<ImMessage> selectSensitiveMessages(String sensitiveLevel, Date startTime, Date endTime, Integer limit);

    /**
     * 查询文件消息列表
     * 
     * @param conversationId 会话ID（可选）
     * @param limit 限制数量
     * @return 文件消息列表
     */
    List<ImMessage> selectFileMessages(Long conversationId, Integer limit);

    /**
     * 统计用户发送消息数量
     * 
     * @param senderId 发送者ID
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @return 消息数量
     */
    int countUserSentMessages(Long senderId, Date startTime, Date endTime);

    /**
     * 清理过期消息
     * 
     * @param days 过期天数
     * @return 清理数量
     */
    int cleanExpiredMessages(int days);

    /**
     * 获取消息统计信息
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计信息
     */
    Map<String, Object> getMessageStatistics(Date startTime, Date endTime);

    /**
     * 删除会话的所有消息
     * 
     * @param conversationId 会话ID
     * @return 删除数量
     */
    int deleteConversationAllMessages(Long conversationId);

    /**
     * 删除用户发送的所有消息
     * 
     * @param senderId 发送者ID
     * @return 删除数量
     */
    int deleteUserAllMessages(Long senderId);

    /**
     * 检查消息是否可以撤回
     * 
     * @param messageId 消息ID
     * @param userId 操作用户ID
     * @return 是否可以撤回
     */
    boolean canRevokeMessage(Long messageId, Long userId);

    /**
     * 标记消息为已读
     * 
     * @param conversationId 会话ID
     * @param userId 用户ID
     * @param messageId 消息ID
     * @return 是否成功
     */
    boolean markMessageAsRead(Long conversationId, Long userId, Long messageId);
}