package com.ruoyi.im.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.im.mapper.ImConversationMemberMapper;
import com.ruoyi.im.domain.ImConversationMember;
import com.ruoyi.im.service.ImConversationMemberService;

/**
 * 会话成员Service业务层处理
 * 
 * @author ruoyi
 */
@Service
public class ImConversationMemberServiceImpl extends BaseServiceImpl<ImConversationMember, ImConversationMemberMapper> implements ImConversationMemberService {
    @Autowired
    private ImConversationMemberMapper imConversationMemberMapper;

    /**
     * 查询会话成员
     * 
     * @param id 会话成员ID
     * @return 会话成员
     */
    @Override
    public ImConversationMember selectImConversationMemberById(Long id) {
        return imConversationMemberMapper.selectImConversationMemberById(id);
    }

    /**
     * 查询会话成员列表
     * 
     * @param imConversationMember 会话成员
     * @return 会话成员
     */
    @Override
    public List<ImConversationMember> selectImConversationMemberList(ImConversationMember imConversationMember) {
        return imConversationMemberMapper.selectImConversationMemberList(imConversationMember);
    }

    /**
     * 新增会话成员
     * 
     * @param imConversationMember 会话成员
     * @return 结果
     */
    @Override
    public int insertImConversationMember(ImConversationMember imConversationMember) {
        return imConversationMemberMapper.insertImConversationMember(imConversationMember);
    }

    /**
     * 修改会话成员
     * 
     * @param imConversationMember 会话成员
     * @return 结果
     */
    @Override
    public int updateImConversationMember(ImConversationMember imConversationMember) {
        return imConversationMemberMapper.updateImConversationMember(imConversationMember);
    }

    /**
     * 批量删除会话成员
     * 
     * @param ids 需要删除的会话成员ID
     * @return 结果
     */
    @Override
    public int deleteImConversationMemberByIds(Long[] ids) {
        return imConversationMemberMapper.deleteImConversationMemberByIds(ids);
    }

    /**
     * 删除会话成员信息
     * 
     * @param id 会话成员ID
     * @return 结果
     */
    @Override
    public int deleteImConversationMemberById(Long id) {
        return imConversationMemberMapper.deleteImConversationMemberById(id);
    }
    
    /**
     * 根据会话ID查询会话成员列表
     * 
     * @param conversationId 会话ID
     * @return 会话成员集合
     */
    @Override
    public List<ImConversationMember> selectImConversationMemberListByConversationId(Long conversationId) {
        return imConversationMemberMapper.selectImConversationMemberListByConversationId(conversationId);
    }
    
    /**
     * 根据会话ID和用户ID查询会话成员
     * 
     * @param conversationId 会话ID
     * @param userId 用户ID
     * @return 会话成员
     */
    @Override
    public ImConversationMember selectImConversationMemberByConversationIdAndUserId(Long conversationId, Long userId) {
        return imConversationMemberMapper.selectImConversationMemberByConversationIdAndUserId(conversationId, userId);
    }
    
    /**
     * 根据用户ID查询会话成员列表
     * 
     * @param userId 用户ID
     * @return 会话成员集合
     */
    @Override
    public List<ImConversationMember> selectImConversationMemberListByUserId(Long userId) {
        return imConversationMemberMapper.selectImConversationMemberListByUserId(userId);
    }
    
    /**
     * 添加会话成员
     * 
     * @param conversationId 会话ID
     * @param userIds 用户ID列表
     * @return 结果
     */
    @Override
    public int addConversationMembers(Long conversationId, List<Long> userIds) {
        int result = 0;
        for (Long userId : userIds) {
            // 检查用户是否已经是会话成员
            ImConversationMember existingMember = selectImConversationMemberByConversationIdAndUserId(conversationId, userId);
            if (existingMember == null) {
                ImConversationMember member = new ImConversationMember();
                member.setConversationId(conversationId);
                member.setUserId(userId);
                member.setUnreadCount(0);
                
                result += insertImConversationMember(member);
            }
        }
        return result;
    }
    
    /**
     * 移除会话成员
     * 
     * @param conversationId 会话ID
     * @param userId 用户ID
     * @return 结果
     */
    @Override
    public int removeConversationMember(Long conversationId, Long userId) {
        ImConversationMember member = selectImConversationMemberByConversationIdAndUserId(conversationId, userId);
        if (member != null) {
            return deleteImConversationMemberById(member.getId());
        }
        return 0;
    }
    
    /**
     * 更新会话成员未读数
     * 
     * @param conversationId 会话ID
     * @param userId 用户ID
     * @param unreadCount 未读数
     * @return 结果
     */
    @Override
    public int updateUnreadCount(Long conversationId, Long userId, Integer unreadCount) {
        ImConversationMember member = selectImConversationMemberByConversationIdAndUserId(conversationId, userId);
        if (member != null) {
            member.setUnreadCount(unreadCount);
            return updateImConversationMember(member);
        }
        return 0;
    }
    
    /**
     * 标记会话成员消息已读
     * 
     * @param conversationId 会话ID
     * @param userId 用户ID
     * @param messageId 消息ID
     * @return 结果
     */
    @Override
    public int markMessageAsRead(Long conversationId, Long userId, Long messageId) {
        ImConversationMember member = selectImConversationMemberByConversationIdAndUserId(conversationId, userId);
        if (member != null) {
            member.setLastReadMessageId(messageId);
            // 将未读数设置为0
            member.setUnreadCount(0);
            return updateImConversationMember(member);
        }
        return 0;
    }
    
    /**
     * 设置会话成员置顶状态
     * 
     * @param conversationId 会话ID
     * @param userId 用户ID
     * @param isPinned 是否置顶
     * @return 结果
     */
    @Override
    public int setConversationPinned(Long conversationId, Long userId, Boolean isPinned) {
        ImConversationMember member = selectImConversationMemberByConversationIdAndUserId(conversationId, userId);
        if (member != null) {
            member.setIsPinned(isPinned);
            return updateImConversationMember(member);
        }
        return 0;
    }
    
    /**
     * 设置会话成员免打扰状态
     * 
     * @param conversationId 会话ID
     * @param userId 用户ID
     * @param isMuted 是否免打扰
     * @return 结果
     */
    @Override
    public int setConversationMuted(Long conversationId, Long userId, Boolean isMuted) {
        ImConversationMember member = selectImConversationMemberByConversationIdAndUserId(conversationId, userId);
        if (member != null) {
            member.setIsMuted(isMuted);
            return updateImConversationMember(member);
        }
        return 0;
    }
}