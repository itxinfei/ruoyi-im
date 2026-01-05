package com.ruoyi.im.service;

import com.ruoyi.im.domain.ImConversationMember;
import java.util.List;

/**
 * 会话成员Service接口
 * 
 * @author ruoyi
 */
public interface ImConversationMemberService extends BaseService<ImConversationMember> {
    
    @Override
    ImConversationMember selectById(Long id);
    
    @Override
    List<ImConversationMember> selectList(ImConversationMember imConversationMember);
    
    @Override
    int insert(ImConversationMember imConversationMember);
    
    @Override
    int update(ImConversationMember imConversationMember);
    
    @Override
    int deleteByIds(Long[] ids);
    
    @Override
    int deleteById(Long id);
    
    /**
     * 根据会话ID查询会话成员列表
     * 
     * @param conversationId 会话ID
     * @return 会话成员集合
     */
    public List<ImConversationMember> selectImConversationMemberListByConversationId(Long conversationId);
    
    /**
     * 根据会话ID和用户ID查询会话成员
     * 
     * @param conversationId 会话ID
     * @param userId 用户ID
     * @return 会话成员
     */
    public ImConversationMember selectImConversationMemberByConversationIdAndUserId(Long conversationId, Long userId);
    
    /**
     * 根据用户ID查询会话成员列表
     * 
     * @param userId 用户ID
     * @return 会话成员集合
     */
    public List<ImConversationMember> selectImConversationMemberListByUserId(Long userId);
    
    /**
     * 添加会话成员
     * 
     * @param conversationId 会话ID
     * @param userIds 用户ID列表
     * @return 结果
     */
    public int addConversationMembers(Long conversationId, List<Long> userIds);
    
    /**
     * 移除会话成员
     * 
     * @param conversationId 会话ID
     * @param userId 用户ID
     * @return 结果
     */
    public int removeConversationMember(Long conversationId, Long userId);
    
    /**
     * 更新会话成员未读数
     * 
     * @param conversationId 会话ID
     * @param userId 用户ID
     * @param unreadCount 未读数
     * @return 结果
     */
    public int updateUnreadCount(Long conversationId, Long userId, Integer unreadCount);
    
    /**
     * 标记会话成员消息已读
     * 
     * @param conversationId 会话ID
     * @param userId 用户ID
     * @param messageId 消息ID
     * @return 结果
     */
    public int markMessageAsRead(Long conversationId, Long userId, Long messageId);
    
    /**
     * 设置会话成员置顶状态
     * 
     * @param conversationId 会话ID
     * @param userId 用户ID
     * @param isPinned 是否置顶
     * @return 结果
     */
    public int setConversationPinned(Long conversationId, Long userId, Boolean isPinned);
    
    /**
     * 设置会话成员免打扰状态
     * 
     * @param conversationId 会话ID
     * @param userId 用户ID
     * @param isMuted 是否免打扰
     * @return 结果
     */
    public int setConversationMuted(Long conversationId, Long userId, Boolean isMuted);
}