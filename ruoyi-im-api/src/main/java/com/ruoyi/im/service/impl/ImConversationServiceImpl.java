package com.ruoyi.im.service.impl;

import com.ruoyi.im.constant.ImErrorCode;
import com.ruoyi.im.domain.ImConversation;
import com.ruoyi.im.domain.ImConversationMember;
import com.ruoyi.im.domain.ImUser;
import com.ruoyi.im.dto.conversation.ImConversationCreateRequest;
import com.ruoyi.im.dto.conversation.ImConversationUpdateRequest;
import com.ruoyi.im.dto.conversation.ImGroupConversationCreateRequest;
import com.ruoyi.im.dto.conversation.ImPrivateConversationCreateRequest;
import com.ruoyi.im.exception.BusinessException;
import com.ruoyi.im.mapper.ImConversationMapper;
import com.ruoyi.im.mapper.ImConversationMemberMapper;
import com.ruoyi.im.mapper.ImMessageMapper;
import com.ruoyi.im.mapper.ImUserMapper;
import com.ruoyi.im.service.ImConversationService;
import com.ruoyi.im.utils.ImRedisUtil;
import com.ruoyi.im.vo.conversation.ImConversationVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 会话服务实现
 *
 * @author ruoyi
 */
@Service
public class ImConversationServiceImpl implements ImConversationService {

    @Autowired
    private ImConversationMapper imConversationMapper;

    @Autowired
    private ImConversationMemberMapper imConversationMemberMapper;

    @Autowired
    private ImUserMapper imUserMapper;

    @Autowired
    private ImMessageMapper imMessageMapper;

    @Autowired
    private ImRedisUtil imRedisUtil;

    @Override
    public List<ImConversationVO> getUserConversations(Long userId) {
        // 查询用户参与的所有会话
        List<ImConversationMember> memberList = imConversationMemberMapper.selectByUserId(userId);
        List<ImConversationVO> voList = new ArrayList<>();

        for (ImConversationMember member : memberList) {
            // 使用BaseMapper的selectById方法
            ImConversation conversation = imConversationMapper.selectById(member.getConversationId());
            if (conversation != null) {
                ImConversationVO vo = new ImConversationVO();
                BeanUtils.copyProperties(conversation, vo);
                vo.setUnreadCount(member.getUnreadCount());
                vo.setIsPinned(member.getIsPinned() != null && member.getIsPinned() == 1);
                vo.setIsMuted(member.getIsMuted() != null && member.getIsMuted() == 1);
                vo.setLastReadMessageId(member.getLastReadMessageId());

                // 设置会话相关信息
                if ("PRIVATE".equalsIgnoreCase(conversation.getType())) {
                    // 私聊会话，获取对方用户信息
                    Long peerUserId = getPeerUserId(conversation.getId(), userId);
                    if (peerUserId != null) {
                        ImUser peerUser = imUserMapper.selectImUserById(peerUserId);
                        if (peerUser != null) {
                            vo.setPeerName(peerUser.getNickname() != null ? peerUser.getNickname() : peerUser.getUsername());
                            vo.setPeerAvatar(peerUser.getAvatar());
                        }
                    }
                } else if ("GROUP".equalsIgnoreCase(conversation.getType())) {
                    // 群聊会话，获取群组信息
                    vo.setPeerName("群聊会话");
                    vo.setPeerAvatar("/avatar/group_default.png");
                }

                voList.add(vo);
            }
        }

        // 按最后更新时间排序
        voList.sort((a, b) -> b.getUpdateTime().compareTo(a.getUpdateTime()));
        return voList;
    }

    @Override
    public ImConversationVO getConversationById(Long conversationId, Long userId) {
        // 检查用户是否属于该会话
        ImConversationMember member = imConversationMemberMapper.selectByConversationIdAndUserId(conversationId, userId);
        if (member == null) {
            throw new BusinessException(ImErrorCode.CONVERSATION_NOT_FOUND, "会话不存在或无权限访问");
        }

        // 使用BaseMapper的selectById方法
        ImConversation conversation = imConversationMapper.selectById(conversationId);
        if (conversation == null) {
            throw new BusinessException(ImErrorCode.CONVERSATION_NOT_FOUND, "会话不存在");
        }

        ImConversationVO vo = new ImConversationVO();
        BeanUtils.copyProperties(conversation, vo);
        vo.setUnreadCount(member.getUnreadCount());
        vo.setIsPinned(member.getIsPinned() != null && member.getIsPinned() == 1);
        vo.setIsMuted(member.getIsMuted() != null && member.getIsMuted() == 1);
        vo.setLastReadMessageId(member.getLastReadMessageId());

        // 设置会话相关信息
        if ("PRIVATE".equalsIgnoreCase(conversation.getType())) {
            // 私聊会话，获取对方用户信息
            Long peerUserId = getPeerUserId(conversationId, userId);
            if (peerUserId != null) {
                ImUser peerUser = imUserMapper.selectImUserById(peerUserId);
                if (peerUser != null) {
                    vo.setPeerName(peerUser.getNickname() != null ? peerUser.getNickname() : peerUser.getUsername());
                    vo.setPeerAvatar(peerUser.getAvatar());
                    vo.setPeerOnline(imRedisUtil.isOnlineUser(peerUserId));
                }
            }
        } else if ("GROUP".equalsIgnoreCase(conversation.getType())) {
            // 群聊会话，获取群组信息
            vo.setPeerName("群聊会话");
            vo.setPeerAvatar("/avatar/group_default.png");
        }

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createPrivateConversation(Long userId, ImPrivateConversationCreateRequest request) {
        // 检查对方用户是否存在
        ImUser peerUser = imUserMapper.selectImUserById(request.getPeerUserId());
        if (peerUser == null) {
            throw new BusinessException(ImErrorCode.USER_NOT_EXIST, "对方用户不存在");
        }

        // 检查是否已经存在私聊会话
        ImConversation existingConversation = imConversationMapper.selectByTypeAndTarget("PRIVATE", Math.min(userId, request.getPeerUserId()), Math.max(userId, request.getPeerUserId()));
        if (existingConversation != null) {
            return existingConversation.getId();
        }

        // 创建会话
        ImConversation conversation = new ImConversation();
        conversation.setType("PRIVATE");
        conversation.setTargetId(Math.min(userId, request.getPeerUserId()));
        conversation.setLastMessageId(null);
        conversation.setCreateTime(LocalDateTime.now());
        conversation.setUpdateTime(LocalDateTime.now());

        int result = imConversationMapper.insert(conversation); // 使用BaseMapper的insert方法
        if (result <= 0) {
            throw new BusinessException(ImErrorCode.CREATE_CONVERSATION_FAILED, "创建会话失败");
        }

        Long conversationId = conversation.getId();

        // 添加两个用户到会话成员
        addMemberToConversation(conversationId, userId);
        addMemberToConversation(conversationId, request.getPeerUserId());

        return conversationId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createGroupConversation(Long userId, ImGroupConversationCreateRequest request) {
        // 创建群聊会话
        ImConversation conversation = new ImConversation();
        conversation.setType("GROUP");
        conversation.setTargetId(null); // 群聊的targetId为null，通过会话成员表关联
        conversation.setLastMessageId(null);
        conversation.setCreateTime(LocalDateTime.now());
        conversation.setUpdateTime(LocalDateTime.now());

        int result = imConversationMapper.insert(conversation); // 使用BaseMapper的insert方法
        if (result <= 0) {
            throw new BusinessException(ImErrorCode.CREATE_CONVERSATION_FAILED, "创建群聊会话失败");
        }

        Long conversationId = conversation.getId();

        // 添加创建者到会话成员
        addMemberToConversation(conversationId, userId);

        // 添加其他成员到会话
        if (request.getMemberIds() != null) {
            for (Long memberId : request.getMemberIds()) {
                if (!memberId.equals(userId)) { // 避免重复添加创建者
                    // 检查用户是否存在
                    ImUser user = imUserMapper.selectImUserById(memberId);
                    if (user != null) {
                        addMemberToConversation(conversationId, memberId);
                    }
                }
            }
        }

        return conversationId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteConversation(Long userId, Long conversationId) {
        // 检查用户是否属于该会话
        ImConversationMember member = imConversationMemberMapper.selectByConversationIdAndUserId(conversationId, userId);
        if (member == null) {
            throw new BusinessException(ImErrorCode.CONVERSATION_NOT_FOUND, "会话不存在或无权限访问");
        }

        // 从会话成员表中删除该用户，相当于删除会话（软删除）
        imConversationMemberMapper.markAsDeleted(conversationId, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void pinConversation(Long userId, Long conversationId, Boolean isPinned) {
        // 检查用户是否属于该会话
        ImConversationMember member = imConversationMemberMapper.selectByConversationIdAndUserId(conversationId, userId);
        if (member == null) {
            throw new BusinessException(ImErrorCode.CONVERSATION_NOT_FOUND, "会话不存在或无权限访问");
        }

        imConversationMemberMapper.updatePinned(conversationId, userId, isPinned ? 1 : 0);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void muteConversation(Long userId, Long conversationId, Boolean isMuted) {
        // 检查用户是否属于该会话
        ImConversationMember member = imConversationMemberMapper.selectByConversationIdAndUserId(conversationId, userId);
        if (member == null) {
            throw new BusinessException(ImErrorCode.CONVERSATION_NOT_FOUND, "会话不存在或无权限访问");
        }

        imConversationMemberMapper.updateMuted(conversationId, userId, isMuted ? 1 : 0);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAsRead(Long userId, Long conversationId) {
        // 检查用户是否属于该会话
        ImConversationMember member = imConversationMemberMapper.selectByConversationIdAndUserId(conversationId, userId);
        if (member == null) {
            throw new BusinessException(ImErrorCode.CONVERSATION_NOT_FOUND, "会话不存在或无权限访问");
        }

        // 将未读消息数设为0
        imConversationMemberMapper.updateUnreadCount(conversationId, userId, 0);

        // 获取会话最后一条消息ID作为最后已读消息ID
        com.ruoyi.im.domain.ImMessage lastMessage = imMessageMapper.selectLastMessageByConversationId(conversationId);
        if (lastMessage != null) {
            imConversationMemberMapper.updateLastReadMessageId(conversationId, userId, lastMessage.getId());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clearUnreadCount(Long userId, Long conversationId) {
        // 检查用户是否属于该会话
        ImConversationMember member = imConversationMemberMapper.selectByConversationIdAndUserId(conversationId, userId);
        if (member == null) {
            throw new BusinessException(ImErrorCode.CONVERSATION_NOT_FOUND, "会话不存在或无权限访问");
        }

        // 将未读消息数设为0
        imConversationMemberMapper.updateUnreadCount(conversationId, userId, 0);
    }

    @Override
    public Integer getTotalUnreadCount(Long userId) {
        List<ImConversationMember> members = imConversationMemberMapper.selectByUserId(userId);
        int totalUnread = 0;
        for (ImConversationMember member : members) {
            totalUnread += member.getUnreadCount();
        }
        return totalUnread;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createConversation(ImConversationCreateRequest request, Long userId) {
        if ("PRIVATE".equals(request.getType())) {
            // 创建私聊会话
            ImPrivateConversationCreateRequest privateRequest = new ImPrivateConversationCreateRequest();
            privateRequest.setPeerUserId(request.getTargetId());
            return createPrivateConversation(userId, privateRequest);
        } else if ("GROUP".equals(request.getType())) {
            // 创建群聊会话
            ImGroupConversationCreateRequest groupRequest = new ImGroupConversationCreateRequest();
            groupRequest.setGroupName(request.getGroupName());
            groupRequest.setGroupAvatar(request.getGroupAvatar());
            groupRequest.setMemberIds(request.getMemberIds());
            return createGroupConversation(userId, groupRequest);
        } else {
            throw new BusinessException(ImErrorCode.INVALID_CONVERSATION_TYPE, "不支持的会话类型");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateConversation(Long id, ImConversationUpdateRequest request, Long userId) {
        // 检查用户是否属于该会话
        ImConversationMember member = imConversationMemberMapper.selectByConversationIdAndUserId(id, userId);
        if (member == null) {
            throw new BusinessException(ImErrorCode.CONVERSATION_NOT_FOUND, "会话不存在或无权限访问");
        }

        // 更新会话设置
        if (request.getIsPinned() != null) {
            imConversationMemberMapper.updatePinned(id, userId, request.getIsPinned() ? 1 : 0);
        }
        if (request.getIsMuted() != null) {
            imConversationMemberMapper.updateMuted(id, userId, request.getIsMuted() ? 1 : 0);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setPinned(Long id, Boolean pinned, Long userId) {
        pinConversation(userId, id, pinned);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setMuted(Long id, Boolean muted, Long userId) {
        muteConversation(userId, id, muted);
    }

    @Override
    public List<ImConversationVO> searchConversations(String keyword, Long userId) {
        // 搜索用户参与的会话，根据关键词匹配会话名称或最近消息内容
        List<ImConversationMember> memberList = imConversationMemberMapper.selectByUserId(userId);
        List<ImConversationVO> voList = new ArrayList<>();

        for (ImConversationMember member : memberList) {
            ImConversation conversation = imConversationMapper.selectById(member.getConversationId());
            if (conversation != null) {
                ImConversationVO vo = new ImConversationVO();
                BeanUtils.copyProperties(conversation, vo);
                vo.setUnreadCount(member.getUnreadCount());
                vo.setIsPinned(member.getIsPinned() != null && member.getIsPinned() == 1);
                vo.setIsMuted(member.getIsMuted() != null && member.getIsMuted() == 1);
                vo.setLastReadMessageId(member.getLastReadMessageId());

                // 设置会话相关信息
                if ("PRIVATE".equalsIgnoreCase(conversation.getType())) {
                    // 私聊会话，获取对方用户信息
                    Long peerUserId = getPeerUserId(conversation.getId(), userId);
                    if (peerUserId != null) {
                        ImUser peerUser = imUserMapper.selectImUserById(peerUserId);
                        if (peerUser != null) {
                            vo.setPeerName(peerUser.getNickname() != null ? peerUser.getNickname() : peerUser.getUsername());
                            vo.setPeerAvatar(peerUser.getAvatar());
                        }
                    }
                } else if ("GROUP".equalsIgnoreCase(conversation.getType())) {
                    // 群聊会话，获取群组信息
                    vo.setPeerName("群聊会话");
                    vo.setPeerAvatar("/avatar/group_default.png");
                }

                // 检查关键词匹配
                if (keyword == null || keyword.isEmpty() || 
                    (vo.getPeerName() != null && vo.getPeerName().contains(keyword))) {
                    voList.add(vo);
                }
            }
        }

        return voList;
    }

    /**
     * 添加成员到会话
     *
     * @param conversationId 会话ID
     * @param userId 用户ID
     */
    private void addMemberToConversation(Long conversationId, Long userId) {
        ImConversationMember member = new ImConversationMember();
        member.setConversationId(conversationId);
        member.setUserId(userId);
        member.setUnreadCount(0);
        member.setIsPinned(0);
        member.setIsMuted(0);
        member.setIsDeleted(0);
        member.setCreateTime(LocalDateTime.now());
        member.setUpdateTime(LocalDateTime.now());

        imConversationMemberMapper.insertImConversationMember(member);
    }

    /**
     * 获取会话中对方用户ID（仅适用于私聊）
     *
     * @param conversationId 会话ID
     * @param currentUserId 当前用户ID
     * @return 对方用户ID，如果不是私聊会话或找不到返回null
     */
    private Long getPeerUserId(Long conversationId, Long currentUserId) {
        List<ImConversationMember> members = imConversationMemberMapper.selectByConversationId(conversationId);
        for (ImConversationMember member : members) {
            if (!member.getUserId().equals(currentUserId)) {
                return member.getUserId();
            }
        }
        return null;
    }
}