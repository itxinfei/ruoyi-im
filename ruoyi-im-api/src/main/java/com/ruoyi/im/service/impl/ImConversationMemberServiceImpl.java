package com.ruoyi.im.service.impl;

import com.ruoyi.im.domain.ImConversationMember;
import com.ruoyi.im.dto.conversation.ImConversationMemberUpdateRequest;
import com.ruoyi.im.exception.BusinessException;
import com.ruoyi.im.mapper.ImConversationMemberMapper;
import com.ruoyi.im.service.ImConversationMemberService;
import com.ruoyi.im.util.BeanConvertUtil;
import com.ruoyi.im.vo.conversation.ImConversationMemberVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 会话成员服务实现
 *
 * 用于管理用户与会话的关系，包括会话列表、未读消息数、置顶、免打扰等功能
 *
 * @author ruoyi
 */
@Service
public class ImConversationMemberServiceImpl implements ImConversationMemberService {

    private final ImConversationMemberMapper conversationMemberMapper;

    public ImConversationMemberServiceImpl(ImConversationMemberMapper conversationMemberMapper) {
        this.conversationMemberMapper = conversationMemberMapper;
    }

    @Override
    public List<ImConversationMemberVO> getConversationMemberList(Long userId) {
        List<ImConversationMemberVO> voList = new ArrayList<>();
        List<ImConversationMember> memberList = conversationMemberMapper.selectByUserId(userId);
        for (ImConversationMember member : memberList) {
            ImConversationMemberVO vo = new ImConversationMemberVO();
            BeanConvertUtil.copyProperties(member, vo);
            voList.add(vo);
        }
        return voList;
    }

    @Override
    public ImConversationMemberVO getConversationMember(Long conversationId, Long userId) {
        ImConversationMember member = conversationMemberMapper.selectByConversationIdAndUserId(conversationId, userId);
        if (member == null) {
            throw new BusinessException("会话成员不存在");
        }
        ImConversationMemberVO vo = new ImConversationMemberVO();
        BeanConvertUtil.copyProperties(member, vo);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateConversationMember(Long conversationId, Long userId, ImConversationMemberUpdateRequest request) {
        ImConversationMember member = conversationMemberMapper.selectByConversationIdAndUserId(conversationId, userId);
        if (member == null) {
            throw new BusinessException("会话成员不存在");
        }
        if (request.getIsPinned() != null) {
            conversationMemberMapper.updatePinned(conversationId, userId, request.getIsPinned());
        }
        if (request.getIsMuted() != null) {
            conversationMemberMapper.updateMuted(conversationId, userId, request.getIsMuted());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteConversationMember(Long conversationId, Long userId) {
        ImConversationMember member = conversationMemberMapper.selectByConversationIdAndUserId(conversationId, userId);
        if (member == null) {
            throw new BusinessException("会话成员不存在");
        }
        if (!member.getUserId().equals(userId)) {
            throw new BusinessException("无权删除该会话成员");
        }
        conversationMemberMapper.markAsDeleted(conversationId, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clearUnread(Long conversationId, Long userId) {
        ImConversationMember member = conversationMemberMapper.selectByConversationIdAndUserId(conversationId, userId);
        if (member == null) {
            throw new BusinessException("会话成员不存在");
        }
        conversationMemberMapper.updateUnreadCount(conversationId, userId, 0);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void togglePin(Long conversationId, Long userId, Integer pinned) {
        ImConversationMember member = conversationMemberMapper.selectByConversationIdAndUserId(conversationId, userId);
        if (member == null) {
            throw new BusinessException("会话成员不存在");
        }
        conversationMemberMapper.updatePinned(conversationId, userId, pinned);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void toggleMute(Long conversationId, Long userId, Integer muted) {
        ImConversationMember member = conversationMemberMapper.selectByConversationIdAndUserId(conversationId, userId);
        if (member == null) {
            throw new BusinessException("会话成员不存在");
        }
        conversationMemberMapper.updateMuted(conversationId, userId, muted);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void incrementUnread(Long conversationId, Long userId, Integer increment) {
        ImConversationMember member = conversationMemberMapper.selectByConversationIdAndUserId(conversationId, userId);
        if (member == null) {
            throw new BusinessException("会话成员不存在");
        }
        Integer newUnreadCount = member.getUnreadCount() + increment;
        if (newUnreadCount < 0) {
            newUnreadCount = 0;
        }
        conversationMemberMapper.updateUnreadCount(conversationId, userId, newUnreadCount);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateLastReadMessageId(Long conversationId, Long userId, Long messageId) {
        ImConversationMember member = conversationMemberMapper.selectByConversationIdAndUserId(conversationId, userId);
        if (member == null) {
            throw new BusinessException("会话成员不存在");
        }
        conversationMemberMapper.updateLastReadMessageId(conversationId, userId, messageId);
    }

    @Override
    public List<Long> getUserConversationIds(Long userId) {
        List<ImConversationMember> memberList = conversationMemberMapper.selectByUserId(userId);
        List<Long> conversationIds = new ArrayList<>();
        for (ImConversationMember member : memberList) {
            conversationIds.add(member.getConversationId());
        }
        return conversationIds;
    }
}
