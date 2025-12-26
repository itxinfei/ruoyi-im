package com.ruoyi.im.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.im.domain.ImConversationMember;

import java.util.List;

/**
 * 会话成员Service接口
 * 
 * @author ruoyi
 * @date 2024-01-01
 */
public interface IImConversationMemberService extends IService<ImConversationMember> {

    /**
     * 添加会话成员
     * 
     * @param conversationId 会话ID
     * @param userIds 用户ID列表
     * @return 添加数量
     */
    int addConversationMembers(Long conversationId, List<Long> userIds);

    /**
     * 移除会话成员
     * 
     * @param conversationId 会话ID
     * @param userIds 用户ID列表
     * @return 移除数量
     */
    int removeConversationMembers(Long conversationId, List<Long> userIds);

    /**
     * 查询会话成员列表
     * 
     * @param conversationId 会话ID
     * @return 成员列表
     */
    List<ImConversationMember> selectConversationMembers(Long conversationId);

    /**
     * 检查用户是否为会话成员
     * 
     * @param conversationId 会话ID
     * @param userId 用户ID
     * @return 是否为成员
     */
    boolean isConversationMember(Long conversationId, Long userId);

    /**
     * 获取会话成员信息
     * 
     * @param conversationId 会话ID
     * @param userId 用户ID
     * @return 成员信息
     */
    ImConversationMember getConversationMember(Long conversationId, Long userId);

    /**
     * 统计会话成员数量
     * 
     * @param conversationId 会话ID
     * @return 成员数量
     */
    int countConversationMembers(Long conversationId);

    /**
     * 更新用户最后已读消息
     * 
     * @param conversationId 会话ID
     * @param userId 用户ID
     * @param lastReadMessageId 最后已读消息ID
     * @return 是否成功
     */
    boolean updateLastReadMessage(Long conversationId, Long userId, Long lastReadMessageId);

    /**
     * 设置/取消会话置顶
     * 
     * @param conversationId 会话ID
     * @param userId 用户ID
     * @param pinned 是否置顶
     * @return 是否成功
     */
    boolean setPinnedConversation(Long conversationId, Long userId, boolean pinned);

    /**
     * 设置/取消会话免打扰
     * 
     * @param conversationId 会话ID
     * @param userId 用户ID
     * @param muted 是否免打扰
     * @return 是否成功
     */
    boolean setMutedConversation(Long conversationId, Long userId, boolean muted);

    /**
     * 查询用户的会话ID列表
     * 
     * @param userId 用户ID
     * @return 会话ID列表
     */
    List<Long> selectUserConversationIds(Long userId);

    /**
     * 统计用户未读消息总数
     * 
     * @param userId 用户ID
     * @return 未读消息数
     */
    int countUserUnreadMessages(Long userId);

    /**
     * 查询用户置顶的会话列表
     * 
     * @param userId 用户ID
     * @return 置顶会话列表
     */
    List<ImConversationMember> selectPinnedConversations(Long userId);

    /**
     * 批量更新会话成员的最后已读消息
     * 
     * @param conversationId 会话ID
     * @param lastReadMessageId 最后已读消息ID
     * @param excludeUserIds 排除的用户ID列表
     * @return 更新数量
     */
    int updateLastReadMessageBatch(Long conversationId, Long lastReadMessageId, List<Long> excludeUserIds);

    /**
     * 删除用户在所有会话中的成员关系
     * 
     * @param userId 用户ID
     * @return 删除数量
     */
    int deleteUserAllMemberships(Long userId);

    /**
     * 删除会话的所有成员
     * 
     * @param conversationId 会话ID
     * @return 删除数量
     */
    int deleteConversationAllMembers(Long conversationId);

    /**
     * 检查会话是否被用户免打扰
     * 
     * @param conversationId 会话ID
     * @param userId 用户ID
     * @return 是否免打扰
     */
    boolean isConversationMuted(Long conversationId, Long userId);

    /**
     * 检查会话是否被置顶
     * 
     * @param conversationId 会话ID
     * @param userId 用户ID
     * @return 是否置顶
     */
    boolean isConversationPinned(Long conversationId, Long userId);

    /**
     * 根据ID更新会话成员
     * 
     * @param imConversationMember 会话成员信息
     * @return 是否成功
     */
    boolean updateById(ImConversationMember imConversationMember);

    /**
     * 根据ID列表删除会话成员
     * 
     * @param ids ID列表
     * @return 是否成功
     */
    boolean removeByIds(List<Long> ids);

    /**
     * 根据ID获取会话成员
     * 
     * @param id 会话成员ID
     * @return 会话成员信息
     */
    ImConversationMember getById(Long id);

    /**
     * 保存会话成员
     * 
     * @param imConversationMember 会话成员信息
     * @return 是否成功
     */
    boolean save(ImConversationMember imConversationMember);

    /**
     * 查询所有会话成员列表
     * 
     * @return 会话成员列表
     */
    List<ImConversationMember> list();
}