package com.ruoyi.im.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.im.domain.ImConversationMember;
import com.ruoyi.im.mapper.ImConversationMemberMapper;
import com.ruoyi.im.service.IImConversationMemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 会话成员Service业务层处理
 * 
 * @author ruoyi
 * @date 2024-01-01
 */
@Service
public class ImConversationMemberServiceImpl extends ServiceImpl<ImConversationMemberMapper, ImConversationMember> implements IImConversationMemberService {

    @Autowired
    private ImConversationMemberMapper imConversationMemberMapper;

    /**
     * 添加会话成员
     * 
     * @param conversationId 会话ID
     * @param userIds 用户ID列表
     * @return 添加数量
     */
    @Override
    @Transactional
    public int addConversationMembers(Long conversationId, List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return 0;
        }
        
        List<ImConversationMember> members = new ArrayList<>();
        Date now = new Date();
        
        for (Long userId : userIds) {
            // 检查是否已经是成员
            if (!isConversationMember(conversationId, userId)) {
                ImConversationMember member = new ImConversationMember();
                member.setConversationId(conversationId);
                member.setUserId(userId);
                member.setPinned(false);
                member.setMuted(false);
                member.setCreateTime(now);
                members.add(member);
            }
        }
        
        if (members.isEmpty()) {
            return 0;
        }
        
        return imConversationMemberMapper.insertBatch(members);
    }

    /**
     * 移除会话成员
     * 
     * @param conversationId 会话ID
     * @param userIds 用户ID列表
     * @return 移除数量
     */
    @Override
    @Transactional
    public int removeConversationMembers(Long conversationId, List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return 0;
        }
        
        return imConversationMemberMapper.deleteBatchByConversationIdAndUserIds(conversationId, userIds);
    }

    /**
     * 查询会话成员列表
     * 
     * @param conversationId 会话ID
     * @return 成员列表
     */
    @Override
    public List<ImConversationMember> selectConversationMembers(Long conversationId) {
        return imConversationMemberMapper.selectConversationMembersWithDetails(conversationId);
    }

    /**
     * 检查用户是否为会话成员
     * 
     * @param conversationId 会话ID
     * @param userId 用户ID
     * @return 是否为成员
     */
    @Override
    public boolean isConversationMember(Long conversationId, Long userId) {
        return imConversationMemberMapper.isConversationMember(conversationId, userId);
    }

    /**
     * 获取会话成员信息
     * 
     * @param conversationId 会话ID
     * @param userId 用户ID
     * @return 成员信息
     */
    @Override
    public ImConversationMember getConversationMember(Long conversationId, Long userId) {
        return imConversationMemberMapper.selectConversationMember(conversationId, userId);
    }

    /**
     * 统计会话成员数量
     * 
     * @param conversationId 会话ID
     * @return 成员数量
     */
    @Override
    public int countConversationMembers(Long conversationId) {
        return imConversationMemberMapper.countConversationMembers(conversationId);
    }

    /**
     * 更新用户最后已读消息
     * 
     * @param conversationId 会话ID
     * @param userId 用户ID
     * @param lastReadMessageId 最后已读消息ID
     * @return 是否成功
     */
    @Override
    public boolean updateLastReadMessage(Long conversationId, Long userId, Long lastReadMessageId) {
        return imConversationMemberMapper.updateLastReadMessage(conversationId, userId, lastReadMessageId) > 0;
    }

    /**
     * 设置/取消会话置顶
     * 
     * @param conversationId 会话ID
     * @param userId 用户ID
     * @param pinned 是否置顶
     * @return 是否成功
     */
    @Override
    public boolean setPinnedConversation(Long conversationId, Long userId, boolean pinned) {
        return imConversationMemberMapper.updatePinned(conversationId, userId, pinned) > 0;
    }

    /**
     * 设置/取消会话免打扰
     * 
     * @param conversationId 会话ID
     * @param userId 用户ID
     * @param muted 是否免打扰
     * @return 是否成功
     */
    @Override
    public boolean setMutedConversation(Long conversationId, Long userId, boolean muted) {
        return imConversationMemberMapper.updateMuted(conversationId, userId, muted) > 0;
    }

    /**
     * 查询用户的会话ID列表
     * 
     * @param userId 用户ID
     * @return 会话ID列表
     */
    @Override
    public List<Long> selectUserConversationIds(Long userId) {
        return imConversationMemberMapper.selectUserConversationIds(userId);
    }

    /**
     * 统计用户未读消息总数
     * 
     * @param userId 用户ID
     * @return 未读消息数
     */
    @Override
    public int countUserUnreadMessages(Long userId) {
        return imConversationMemberMapper.countUserUnreadMessages(userId);
    }

    /**
     * 查询用户置顶的会话列表
     * 
     * @param userId 用户ID
     * @return 置顶会话列表
     */
    @Override
    public List<ImConversationMember> selectPinnedConversations(Long userId) {
        return imConversationMemberMapper.selectPinnedConversations(userId);
    }

    /**
     * 批量更新会话成员的最后已读消息
     * 
     * @param conversationId 会话ID
     * @param lastReadMessageId 最后已读消息ID
     * @param excludeUserIds 排除的用户ID列表
     * @return 更新数量
     */
    @Override
    public int updateLastReadMessageBatch(Long conversationId, Long lastReadMessageId, List<Long> excludeUserIds) {
        // TODO: 需要在Mapper中实现此方法或使用其他方式实现批量更新
        return 0;
    }

    /**
     * 删除用户在所有会话中的成员关系
     * 
     * @param userId 用户ID
     * @return 删除数量
     */
    @Override
    public int deleteUserAllMemberships(Long userId) {
        return imConversationMemberMapper.deleteByUserId(userId);
    }

    /**
     * 删除会话的所有成员
     * 
     * @param conversationId 会话ID
     * @return 删除数量
     */
    @Override
    public int deleteConversationAllMembers(Long conversationId) {
        return imConversationMemberMapper.deleteByConversationId(conversationId);
    }

    /**
     * 检查会话是否被用户免打扰
     * 
     * @param conversationId 会话ID
     * @param userId 用户ID
     * @return 是否免打扰
     */
    @Override
    public boolean isConversationMuted(Long conversationId, Long userId) {
        ImConversationMember member = getConversationMember(conversationId, userId);
        return member != null && Boolean.TRUE.equals(member.getMuted());
    }

    /**
     * 检查会话是否被用户置顶
     * 
     * @param conversationId 会话ID
     * @param userId 用户ID
     * @return 是否置顶
     */
    @Override
    public boolean isConversationPinned(Long conversationId, Long userId) {
        ImConversationMember member = getConversationMember(conversationId, userId);
        return member != null && Boolean.TRUE.equals(member.getPinned());
    }

    @Override
    public boolean updateById(ImConversationMember imConversationMember) {
        return imConversationMemberMapper.updateById(imConversationMember) > 0;
    }

    @Override
    public boolean removeByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return false;
        }
        return imConversationMemberMapper.deleteBatchIds(ids) > 0;
    }

    @Override
    public ImConversationMember getById(Long id) {
        return imConversationMemberMapper.selectById(id);
    }

    @Override
    public boolean save(ImConversationMember imConversationMember) {
        return imConversationMemberMapper.insert(imConversationMember) > 0;
    }

    @Override
    public List<ImConversationMember> list() {
        return imConversationMemberMapper.selectList(null);
    }
}