package com.ruoyi.im.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.im.domain.ImConversation;

import java.util.List;

/**
 * 会话Service接口
 * 
 * @author ruoyi
 * @date 2024-01-01
 */
public interface IImConversationService extends IService<ImConversation> {

    /**
     * 创建或获取私聊会话
     * 
     * @param userId1 用户1ID
     * @param userId2 用户2ID
     * @return 会话信息
     */
    ImConversation getOrCreatePrivateConversation(Long userId1, Long userId2);

    /**
     * 创建群聊会话
     * 
     * @param groupId 群组ID
     * @param creatorId 创建者ID
     * @return 会话信息
     */
    ImConversation createGroupConversation(Long groupId, Long creatorId);

    /**
     * 查询用户的会话列表
     * 
     * @param userId 用户ID
     * @return 会话列表
     */
    List<ImConversation> selectUserConversations(Long userId);

    /**
     * 更新会话最后一条消息信息
     * 
     * @param conversationId 会话ID
     * @param lastMessageId 最后消息ID
     * @param lastMessageContent 最后消息内容
     * @param lastMessageTime 最后消息时间
     * @return 是否成功
     */
    boolean updateLastMessage(Long conversationId, Long lastMessageId, String lastMessageContent, java.util.Date lastMessageTime);

    /**
     * 删除会话
     * 
     * @param conversationId 会话ID
     * @param userId 操作用户ID
     * @return 是否成功
     */
    boolean deleteConversation(Long conversationId, Long userId);

    /**
     * 批量删除会话
     * 
     * @param conversationIds 会话ID列表
     * @param userId 操作用户ID
     * @return 删除数量
     */
    int deleteConversationBatch(List<Long> conversationIds, Long userId);

    /**
     * 搜索会话
     * 
     * @param userId 用户ID
     * @param keyword 搜索关键词
     * @return 会话列表
     */
    List<ImConversation> searchConversations(Long userId, String keyword);

    /**
     * 获取会话详细信息
     * 
     * @param conversationId 会话ID
     * @param userId 查询用户ID
     * @return 会话信息
     */
    ImConversation getConversationDetail(Long conversationId, Long userId);

    /**
     * 根据类型和目标ID查询会话
     * 
     * @param type 会话类型
     * @param targetId 目标ID
     * @return 会话信息
     */
    ImConversation getConversationByTarget(String type, Long targetId);

    /**
     * 批量查询会话信息
     * 
     * @param conversationIds 会话ID列表
     * @return 会话列表
     */
    List<ImConversation> selectConversationsByIds(List<Long> conversationIds);

    /**
     * 查询活跃会话列表
     * 
     * @param limit 限制数量
     * @return 会话列表
     */
    List<ImConversation> selectActiveConversations(int limit);

    /**
     * 统计用户会话数量
     * 
     * @param userId 用户ID
     * @return 会话数量
     */
    int countUserConversations(Long userId);

    /**
     * 检查用户是否有权限访问会话
     * 
     * @param conversationId 会话ID
     * @param userId 用户ID
     * @return 是否有权限
     */
    boolean hasConversationAccess(Long conversationId, Long userId);

    /**
     * 更新会话状态
     * 
     * @param conversationId 会话ID
     * @param status 状态
     * @return 是否成功
     */
    boolean updateConversationStatus(Long conversationId, String status);

    /**
     * 删除用户的所有会话
     * 
     * @param userId 用户ID
     * @return 删除数量
     */
    int deleteUserAllConversations(Long userId);

    /**
     * 根据群组ID删除群聊会话
     * 
     * @param groupId 群组ID
     * @return 是否成功
     */
    boolean deleteGroupConversation(Long groupId);
}