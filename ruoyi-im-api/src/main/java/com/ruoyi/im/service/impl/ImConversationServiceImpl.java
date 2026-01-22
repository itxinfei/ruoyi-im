package com.ruoyi.im.service.impl;

import com.ruoyi.im.constant.ImErrorCode;
import com.ruoyi.im.domain.ImConversation;
import com.ruoyi.im.domain.ImConversationMember;
import com.ruoyi.im.domain.ImGroup;
import com.ruoyi.im.domain.ImUser;
import com.ruoyi.im.dto.conversation.ImConversationCreateRequest;
import com.ruoyi.im.dto.conversation.ImConversationUpdateRequest;
import com.ruoyi.im.dto.conversation.ImGroupConversationCreateRequest;
import com.ruoyi.im.dto.conversation.ImPrivateConversationCreateRequest;
import com.ruoyi.im.exception.BusinessException;
import com.ruoyi.im.mapper.ImConversationMapper;
import com.ruoyi.im.mapper.ImConversationMemberMapper;
import com.ruoyi.im.mapper.ImGroupMapper;
import com.ruoyi.im.mapper.ImMessageMapper;
import com.ruoyi.im.mapper.ImUserMapper;
import com.ruoyi.im.service.ImConversationService;
import com.ruoyi.im.util.ImRedisUtil;
import com.ruoyi.im.vo.conversation.ImConversationVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(ImConversationServiceImpl.class);

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

    @Autowired
    private ImGroupMapper imGroupMapper;

    @Autowired
    private com.ruoyi.im.util.MessageEncryptionUtil encryptionUtil;

    @Override
    public List<ImConversationVO> getUserConversations(Long userId) {
        String cacheKey = "conversation:list:" + userId;

        // 尝试从缓存获取
        @SuppressWarnings("unchecked")
        List<ImConversationVO> cachedList = (List<ImConversationVO>) imRedisUtil.get(cacheKey);
        if (cachedList != null) {
            return cachedList;
        }

        // 查询用户参与的所有会话
        List<ImConversationMember> memberList = imConversationMemberMapper.selectByUserId(userId);
        List<ImConversationVO> voList = new ArrayList<>();

        // 用于私聊会话去重的Map，key为对方用户ID，value为会话VO
        java.util.Map<Long, ImConversationVO> privateConversationMap = new java.util.HashMap<>();

        for (ImConversationMember member : memberList) {
            // 跳过已删除的成员记录
            if (member.getIsDeleted() != null && member.getIsDeleted() == 1) {
                continue;
            }

            // 使用BaseMapper的selectById方法
            ImConversation conversation = imConversationMapper.selectById(member.getConversationId());
            if (conversation == null || (conversation.getIsDeleted() != null && conversation.getIsDeleted() == 1)) {
                continue;
            }

            ImConversationVO vo = new ImConversationVO();
            BeanUtils.copyProperties(conversation, vo);
            vo.setUnreadCount(member.getUnreadCount());
            vo.setIsPinned(member.getIsPinned() != null && member.getIsPinned() == 1);
            vo.setIsMuted(member.getIsMuted() != null && member.getIsMuted() == 1);
            vo.setLastReadMessageId(member.getLastReadMessageId());

            // 查询并设置最后一条消息
            if (conversation.getLastMessageId() != null) {
                com.ruoyi.im.domain.ImMessage lastMessage = imMessageMapper
                        .selectImMessageById(conversation.getLastMessageId());
                if (lastMessage != null) {
                    com.ruoyi.im.vo.message.ImMessageVO messageVO = new com.ruoyi.im.vo.message.ImMessageVO();
                    BeanUtils.copyProperties(lastMessage, messageVO);
                    // 解密消息内容
                    if (lastMessage.getContent() != null) {
                        messageVO.setContent(encryptionUtil.decryptMessage(lastMessage.getContent()));
                    }
                    vo.setLastMessage(messageVO);
                    vo.setLastMessageTime(lastMessage.getCreateTime());
                }
            }

            // 设置会话相关信息
            // 兼容PRIVATE和SINGLE类型（历史数据可能使用SINGLE）
            if ("PRIVATE".equalsIgnoreCase(conversation.getType())
                    || "SINGLE".equalsIgnoreCase(conversation.getType())) {
                // 私聊会话，获取对方用户信息
                Long peerUserId = getPeerUserId(conversation.getId(), userId);
                if (peerUserId != null) {
                    ImUser peerUser = imUserMapper.selectImUserById(peerUserId);
                    if (peerUser != null) {
                        String peerName = peerUser.getNickname() != null ? peerUser.getNickname()
                                : peerUser.getUsername();
                        String peerAvatar = peerUser.getAvatar();
                        // 设置对方信息
                        vo.setPeerName(peerName);
                        vo.setPeerAvatar(peerAvatar);
                        // 同时设置name和avatar字段（前端兼容）
                        vo.setName(peerName);
                        vo.setAvatar(peerAvatar);
                    }

                    // 私聊会话去重：同一个对方用户只保留最后消息时间较新的会话
                    ImConversationVO existing = privateConversationMap.get(peerUserId);
                    if (existing == null) {
                        privateConversationMap.put(peerUserId, vo);
                    } else {
                        // 比较最后消息时间，保留较新的
                        java.time.LocalDateTime existingTime = existing.getLastMessageTime();
                        java.time.LocalDateTime newTime = vo.getLastMessageTime();
                        if (newTime != null && (existingTime == null || newTime.isAfter(existingTime))) {
                            privateConversationMap.put(peerUserId, vo);
                        }
                    }
                }
            } else if ("GROUP".equalsIgnoreCase(conversation.getType())) {
                // 群聊会话，从群组表获取信息
                ImGroup group = null;
                if (conversation.getTargetId() != null) {
                    group = imGroupMapper.selectImGroupById(conversation.getTargetId());
                }
                String groupAvatar;
                String groupName;
                if (group != null) {
                    groupName = group.getName();
                    groupAvatar = group.getAvatar();
                    if (groupAvatar == null || groupAvatar.isEmpty()) {
                        groupAvatar = "/avatar/group_default.png";
                    }
                } else {
                    groupName = conversation.getName();
                    if (groupName == null || groupName.isEmpty()) {
                        groupName = "群聊";
                    }
                    groupAvatar = conversation.getAvatar();
                    if (groupAvatar == null || groupAvatar.isEmpty()) {
                        groupAvatar = "/avatar/group_default.png";
                    }
                }
                // 设置群组信息
                vo.setPeerName(groupName);
                vo.setPeerAvatar(groupAvatar);
                // 同时设置name和avatar字段（前端兼容）
                vo.setName(groupName);
                vo.setAvatar(groupAvatar);

                // 群聊会话直接添加到结果列表
                voList.add(vo);
            }
        }

        // 将去重后的私聊会话添加到结果列表
        voList.addAll(privateConversationMap.values());

        // 按最后更新时间排序
        voList.sort((a, b) -> {
            // 优先使用最后消息时间排序
            if (a.getLastMessageTime() != null && b.getLastMessageTime() != null) {
                return b.getLastMessageTime().compareTo(a.getLastMessageTime());
            }
            return b.getUpdateTime().compareTo(a.getUpdateTime());
        });

        // 存入缓存，过期时间5分钟
        imRedisUtil.set(cacheKey, voList, 5, java.util.concurrent.TimeUnit.MINUTES);

        return voList;
    }

    private void clearConversationListCache(Long userId) {
        String cacheKey = "conversation:list:" + userId;
        imRedisUtil.delete(cacheKey);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteConversation(Long userId, Long conversationId) {
        // ... (unchanged part)
        // 检查用户是否属于该会话
        ImConversationMember member = imConversationMemberMapper.selectByConversationIdAndUserId(conversationId,
                userId);
        if (member == null) {
            throw new BusinessException(ImErrorCode.CONVERSATION_NOT_FOUND, "会话不存在或无权限访问");
        }

        // 从会话成员表中删除该用户，相当于删除会话（软删除）
        imConversationMemberMapper.markAsDeleted(conversationId, userId);

        // 清除缓存
        clearConversationListCache(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void pinConversation(Long userId, Long conversationId, Boolean isPinned) {
        // ... (unchanged part)
        // 检查用户是否属于该会话
        ImConversationMember member = imConversationMemberMapper.selectByConversationIdAndUserId(conversationId,
                userId);
        if (member == null) {
            throw new BusinessException(ImErrorCode.CONVERSATION_NOT_FOUND, "会话不存在或无权限访问");
        }

        imConversationMemberMapper.updatePinned(conversationId, userId, isPinned ? 1 : 0);

        // 清除缓存
        clearConversationListCache(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void muteConversation(Long userId, Long conversationId, Boolean isMuted) {
        // ... (unchanged part)
        // 检查用户是否属于该会话
        ImConversationMember member = imConversationMemberMapper.selectByConversationIdAndUserId(conversationId,
                userId);
        if (member == null) {
            throw new BusinessException(ImErrorCode.CONVERSATION_NOT_FOUND, "会话不存在或无权限访问");
        }

        imConversationMemberMapper.updateMuted(conversationId, userId, isMuted ? 1 : 0);

        // 清除缓存
        clearConversationListCache(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAsRead(Long userId, Long conversationId) {
        // ... (unchanged part)
        // 检查用户是否属于该会话
        ImConversationMember member = imConversationMemberMapper.selectByConversationIdAndUserId(conversationId,
                userId);
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

        // 清除缓存
        clearConversationListCache(userId);
    }

    @Override
    public ImConversationVO getConversationById(Long conversationId, Long userId) {
        // 检查用户是否属于该会话
        ImConversationMember member = imConversationMemberMapper.selectByConversationIdAndUserId(conversationId,
                userId);
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
    public void clearUnreadCount(Long userId, Long conversationId) {
        // ... (unchanged part)
        // 检查用户是否属于该会话
        ImConversationMember member = imConversationMemberMapper.selectByConversationIdAndUserId(conversationId,
                userId);
        if (member == null) {
            throw new BusinessException(ImErrorCode.CONVERSATION_NOT_FOUND, "会话不存在或无权限访问");
        }

        // 将未读消息数设为0
        imConversationMemberMapper.updateUnreadCount(conversationId, userId, 0);

        // 清除缓存
        clearConversationListCache(userId);
    }

    // ... (其他方法保持不变)

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createPrivateConversation(Long userId, ImPrivateConversationCreateRequest request) {
        // ... (保持现有逻辑，仅添加缓存清除)
        // 检查对方用户是否存在
        ImUser peerUser = imUserMapper.selectImUserById(request.getPeerUserId());
        if (peerUser == null) {
            throw new BusinessException(ImErrorCode.USER_NOT_EXIST, "对方用户不存在");
        }

        // 检查是否已经存在私聊会话
        ImConversation existingConversation = imConversationMapper.selectByTypeAndTarget("PRIVATE",
                Math.min(userId, request.getPeerUserId()), Math.max(userId, request.getPeerUserId()));
        if (existingConversation == null) {
            existingConversation = imConversationMapper.selectByTypeAndTarget("SINGLE",
                    Math.min(userId, request.getPeerUserId()), Math.max(userId, request.getPeerUserId()));
        }

        Long conversationId;
        if (existingConversation != null) {
            conversationId = existingConversation.getId();
            ensureConversationMember(conversationId, userId);
            ensureConversationMember(conversationId, request.getPeerUserId());
        } else {
            // 创建会话
            ImConversation conversation = new ImConversation();
            conversation.setType("PRIVATE");
            conversation.setTargetId(Math.min(userId, request.getPeerUserId()));
            conversation.setLastMessageId(null);
            conversation.setCreateTime(LocalDateTime.now());
            conversation.setUpdateTime(LocalDateTime.now());

            int result = imConversationMapper.insert(conversation);
            if (result <= 0) {
                throw new BusinessException(ImErrorCode.CREATE_CONVERSATION_FAILED, "创建会话失败");
            }

            conversationId = conversation.getId();

            // 添加两个用户到会话成员
            addMemberToConversation(conversationId, userId);
            addMemberToConversation(conversationId, request.getPeerUserId());
        }

        // 清除双方缓存
        clearConversationListCache(userId);
        clearConversationListCache(request.getPeerUserId());

        return conversationId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createGroupConversation(Long userId, ImGroupConversationCreateRequest request) {
        // 创建群聊会话
        ImConversation conversation = new ImConversation();
        conversation.setType("GROUP");
        conversation.setTargetId(null);
        conversation.setLastMessageId(null);
        conversation.setCreateTime(LocalDateTime.now());
        conversation.setUpdateTime(LocalDateTime.now());

        int result = imConversationMapper.insert(conversation);
        if (result <= 0) {
            throw new BusinessException(ImErrorCode.CREATE_CONVERSATION_FAILED, "创建群聊会话失败");
        }

        Long conversationId = conversation.getId();

        // 添加创建者到会话成员
        addMemberToConversation(conversationId, userId);

        // 清除创建者缓存
        clearConversationListCache(userId);

        // 添加其他成员到会话
        if (request.getMemberIds() != null) {
            for (Long memberId : request.getMemberIds()) {
                if (!memberId.equals(userId)) {
                    ImUser user = imUserMapper.selectImUserById(memberId);
                    if (user != null) {
                        addMemberToConversation(conversationId, memberId);
                        // 清除成员缓存
                        clearConversationListCache(memberId);
                    }
                }
            }
        }

        return conversationId;
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
        // 兼容PRIVATE和SINGLE类型
        if ("PRIVATE".equalsIgnoreCase(request.getType()) || "SINGLE".equalsIgnoreCase(request.getType())) {
            // 创建私聊会话
            ImPrivateConversationCreateRequest privateRequest = new ImPrivateConversationCreateRequest();
            privateRequest.setPeerUserId(request.getTargetId());
            return createPrivateConversation(userId, privateRequest);
        } else if ("GROUP".equalsIgnoreCase(request.getType())) {
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
                // 兼容PRIVATE和SINGLE类型（历史数据可能使用SINGLE）
                if ("PRIVATE".equalsIgnoreCase(conversation.getType())
                        || "SINGLE".equalsIgnoreCase(conversation.getType())) {
                    // 私聊会话，获取对方用户信息
                    Long peerUserId = getPeerUserId(conversation.getId(), userId);
                    if (peerUserId != null) {
                        ImUser peerUser = imUserMapper.selectImUserById(peerUserId);
                        if (peerUser != null) {
                            String peerName = peerUser.getNickname() != null ? peerUser.getNickname()
                                    : peerUser.getUsername();
                            String peerAvatar = peerUser.getAvatar();
                            // 设置对方信息
                            vo.setPeerName(peerName);
                            vo.setPeerAvatar(peerAvatar);
                            // 同时设置name和avatar字段（前端兼容）
                            vo.setName(peerName);
                            vo.setAvatar(peerAvatar);
                        }
                    }
                } else if ("GROUP".equalsIgnoreCase(conversation.getType())) {
                    // 群聊会话，从群组表获取信息
                    ImGroup group = null;
                    if (conversation.getTargetId() != null) {
                        group = imGroupMapper.selectImGroupById(conversation.getTargetId());
                    }
                    String groupAvatar;
                    String groupName;
                    if (group != null) {
                        groupName = group.getName();
                        groupAvatar = group.getAvatar();
                        if (groupAvatar == null || groupAvatar.isEmpty()) {
                            groupAvatar = "/avatar/group_default.png";
                        }
                    } else {
                        groupName = conversation.getName();
                        if (groupName == null || groupName.isEmpty()) {
                            groupName = "群聊";
                        }
                        groupAvatar = conversation.getAvatar();
                        if (groupAvatar == null || groupAvatar.isEmpty()) {
                            groupAvatar = "/avatar/group_default.png";
                        }
                    }
                    // 设置群组信息
                    vo.setPeerName(groupName);
                    vo.setPeerAvatar(groupAvatar);
                    // 同时设置name和avatar字段（前端兼容）
                    vo.setName(groupName);
                    vo.setAvatar(groupAvatar);
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
     * @param userId         用户ID
     */
    private void addMemberToConversation(Long conversationId, Long userId) {
        ImConversationMember member = new ImConversationMember();
        member.setConversationId(conversationId);
        member.setUserId(userId);
        member.setRole("MEMBER"); // 设置默认角色
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
     * @param currentUserId  当前用户ID
     * @return 对方用户ID，如果不是私聊会话或找不到返回null
     */
    private Long getPeerUserId(Long conversationId, Long currentUserId) {
        List<ImConversationMember> members = imConversationMemberMapper.selectByConversationId(conversationId);
        if (members == null || members.isEmpty()) {
            log.warn("会话无成员: conversationId={}", conversationId);
            return null;
        }

        // 处理只有一个成员的异常情况
        if (members.size() == 1) {
            ImConversationMember onlyMember = members.get(0);
            // 如果唯一成员是当前用户，说明对方用户还未加入会话
            if (onlyMember.getUserId().equals(currentUserId)) {
                log.warn("私聊会话只有当前用户一个成员: conversationId={}, userId={}", conversationId, currentUserId);
                return null;
            }
            // 如果唯一成员不是当前用户，直接返回该成员ID（处理异常数据）
            log.info("私聊会话只有一个成员，返回该成员作为对方用户: conversationId={}, peerUserId={}", conversationId,
                    onlyMember.getUserId());
            return onlyMember.getUserId();
        }

        // 正常情况：遍历成员找到对方用户ID
        for (ImConversationMember member : members) {
            if (!member.getUserId().equals(currentUserId)) {
                return member.getUserId();
            }
        }

        log.warn("私聊会话中找不到对方用户: conversationId={}, currentUserId={}, memberCount={}", conversationId, currentUserId,
                members.size());
        return null;
    }

    /**
     * 确保用户是会话成员，如果不存在则添加
     *
     * @param conversationId 会话ID
     * @param userId         用户ID
     */
    private void ensureConversationMember(Long conversationId, Long userId) {
        ImConversationMember existingMember = imConversationMemberMapper.selectByConversationIdAndUserId(conversationId,
                userId);
        if (existingMember == null) {
            // 用户不在会话中，添加为成员
            addMemberToConversation(conversationId, userId);
        } else if (existingMember.getIsDeleted() != null && existingMember.getIsDeleted() == 1) {
            // 用户已被标记为删除，恢复成员状态
            existingMember.setIsDeleted(0);
            existingMember.setUpdateTime(LocalDateTime.now());
            imConversationMemberMapper.updateById(existingMember);
        }
    }

    @Override
    public int getUserConversationCount(Long userId) {
        // 获取用户参与的会话数量（排除已删除的）
        List<ImConversationMember> members = imConversationMemberMapper.selectByUserId(userId);
        int count = 0;
        for (ImConversationMember member : members) {
            if (member.getIsDeleted() == null || member.getIsDeleted() == 0) {
                count++;
            }
        }
        return count;
    }
}