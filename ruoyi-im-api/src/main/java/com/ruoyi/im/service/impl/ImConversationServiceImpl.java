package com.ruoyi.im.service.impl;

import com.ruoyi.im.constant.ImErrorCode;
import com.ruoyi.im.domain.*;
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

    private final ImConversationMapper imConversationMapper;
    private final ImConversationMemberMapper imConversationMemberMapper;
    private final ImUserMapper imUserMapper;
    private final ImMessageMapper imMessageMapper;
    private final ImRedisUtil imRedisUtil;
    private final ImGroupMapper imGroupMapper;
    private final com.ruoyi.im.util.MessageEncryptionUtil encryptionUtil;

    /**
     * 构造器注入依赖
     */
    public ImConversationServiceImpl(ImConversationMapper imConversationMapper,
            ImConversationMemberMapper imConversationMemberMapper,
            ImUserMapper imUserMapper,
            ImMessageMapper imMessageMapper,
            ImRedisUtil imRedisUtil,
            ImGroupMapper imGroupMapper,
            com.ruoyi.im.util.MessageEncryptionUtil encryptionUtil) {
        this.imConversationMapper = imConversationMapper;
        this.imConversationMemberMapper = imConversationMemberMapper;
        this.imUserMapper = imUserMapper;
        this.imMessageMapper = imMessageMapper;
        this.imRedisUtil = imRedisUtil;
        this.imGroupMapper = imGroupMapper;
        this.encryptionUtil = encryptionUtil;
    }

    @Override
    public List<ImConversationVO> getUserConversations(Long userId) {
        return getUserConversationsOptimized(userId);
    }

    /**
     * 优化版本的会话列表查询 - 使用批量查询避免N+1问题
     *
     * @param userId 用户ID
     * @return 会话列表
     */
    private List<ImConversationVO> getUserConversationsOptimized(Long userId) {
        String cacheKey = "conversation:list:" + userId;

        // 尝试从缓存获取
        @SuppressWarnings("unchecked")
        List<ImConversationVO> cachedList = (List<ImConversationVO>) imRedisUtil.get(cacheKey);
        if (cachedList != null) {
            return cachedList;
        }

        // 批量查询：一次性获取会话和成员信息
        List<ImConversationVO> conversations = imConversationMapper.selectUserConversationsWithMembers(userId);
        if (conversations == null || conversations.isEmpty()) {
            return new java.util.ArrayList<>();
        }

        // 收集所有需要批量查询的ID
        java.util.Set<Long> conversationIds = new java.util.HashSet<>();
        java.util.Set<Long> userIds = new java.util.HashSet<>();
        java.util.Set<Long> groupIds = new java.util.HashSet<>();

        for (ImConversationVO vo : conversations) {
            conversationIds.add(vo.getId());
            if ("PRIVATE".equalsIgnoreCase(vo.getType()) || "SINGLE".equalsIgnoreCase(vo.getType())) {
                userIds.add(getPeerUserId(vo.getId(), userId));
            } else if ("GROUP".equalsIgnoreCase(vo.getType())) {
                if (vo.getTargetId() != null) {
                    groupIds.add(vo.getTargetId());
                }
            }
        }

        // 批量查询：一次性获取所有需要的数据（3个查询替代N个查询）
        java.util.Map<Long, ImMessage> lastMessageMap = new java.util.HashMap<>();
        if (!conversationIds.isEmpty()) {
            List<ImMessage> lastMessages = imMessageMapper
                    .selectLastMessagesByConversationIds(new java.util.ArrayList<>(conversationIds));
            for (ImMessage msg : lastMessages) {
                lastMessageMap.put(msg.getConversationId(), msg);
            }
        }

        java.util.Map<Long, ImUser> userMap = new java.util.HashMap<>();
        if (!userIds.isEmpty()) {
            List<ImUser> users = imUserMapper.selectImUserListByIds(new java.util.ArrayList<>(userIds));
            for (ImUser user : users) {
                userMap.put(user.getId(), user);
            }
        }

        java.util.Map<Long, ImGroup> groupMap = new java.util.HashMap<>();
        if (!groupIds.isEmpty()) {
            List<ImGroup> groups = imGroupMapper.selectGroupsByIds(new java.util.ArrayList<>(groupIds));
            for (ImGroup group : groups) {
                groupMap.put(group.getId(), group);
            }
        }

        // 组装VO数据
        List<ImConversationVO> voList = new ArrayList<>();
        java.util.Map<Long, ImConversationVO> privateConversationMap = new java.util.HashMap<>();

        for (ImConversationVO vo : conversations) {
            // 设置最后消息
            ImMessage lastMessage = lastMessageMap.get(vo.getId());
            if (lastMessage != null) {
                com.ruoyi.im.vo.message.ImMessageVO messageVO = new com.ruoyi.im.vo.message.ImMessageVO();
                BeanUtils.copyProperties(lastMessage, messageVO);
                // 解密消息内容
                if (lastMessage.getContent() != null) {
                    try {
                        messageVO.setContent(encryptionUtil.decryptMessage(lastMessage.getContent()));
                    } catch (Exception e) {
                        log.warn("消息解密失败: messageId={}, error={}", lastMessage.getId(), e.getMessage());
                        messageVO.setContent("[加密消息]");
                    }
                }
                vo.setLastMessage(messageVO);
                vo.setLastMessageTime(lastMessage.getCreateTime());
            }

            // 设置会话相关信息
            if ("PRIVATE".equalsIgnoreCase(vo.getType()) || "SINGLE".equalsIgnoreCase(vo.getType())) {
                // 私聊会话，从Map中获取对方用户信息（已批量查询）
                Long peerUserId = getPeerUserId(vo.getId(), userId);
                if (peerUserId != null) {
                    ImUser peerUser = userMap.get(peerUserId);
                    if (peerUser != null) {
                        String peerName = peerUser.getNickname() != null ? peerUser.getNickname()
                                : peerUser.getUsername();
                        String peerAvatar = peerUser.getAvatar();
                        boolean peerOnline = imRedisUtil.isOnlineUser(peerUserId);
                        vo.setPeerName(peerName);
                        vo.setPeerAvatar(peerAvatar);
                        vo.setPeerOnline(peerOnline);
                        vo.setName(peerName);
                        vo.setAvatar(peerAvatar);

                        // 私聊会话去重
                        ImConversationVO existing = privateConversationMap.get(peerUserId);
                        if (existing == null) {
                            privateConversationMap.put(peerUserId, vo);
                        } else {
                            // 比较最后消息时间，保留较新的
                            if (vo.getLastMessageTime() != null &&
                                    (existing.getLastMessageTime() == null ||
                                            vo.getLastMessageTime().isAfter(existing.getLastMessageTime()))) {
                                privateConversationMap.put(peerUserId, vo);
                            }
                        }
                    }
                }
            } else if ("GROUP".equalsIgnoreCase(vo.getType())) {
                // 群聊会话，从Map中获取群组信息（已批量查询）
                Long groupId = vo.getTargetId();
                if (groupId != null) {
                    ImGroup group = groupMap.get(groupId);
                    if (group != null) {
                        String groupName = group.getName();
                        String groupAvatar = group.getAvatar();
                        if (groupAvatar == null || groupAvatar.isEmpty()) {
                            groupAvatar = "/avatar/group_default.png";
                        }
                        vo.setPeerName(groupName);
                        vo.setPeerAvatar(groupAvatar);
                        vo.setName(groupName);
                        vo.setAvatar(groupAvatar);
                    } else {
                        // 群组信息获取失败，使用默认值
                        String groupName = vo.getName();
                        if (groupName == null || groupName.isEmpty()) {
                            groupName = "群聊";
                        }
                        vo.setPeerName(groupName);
                        vo.setPeerAvatar("/avatar/group_default.png");
                        vo.setName(groupName);
                        vo.setAvatar("/avatar/group_default.png");
                    }
                }
                // 群聊会话直接添加到结果列表
                voList.add(vo);
            }
        }

        // 将去重后的私聊会话添加到结果列表
        voList.addAll(privateConversationMap.values());

        // 按最后更新时间排序
        voList.sort((a, b) -> {
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

    @Override
    public List<Long> getConversationMemberIds(Long conversationId) {
        // 获取会话所有成员
        List<ImConversationMember> members = imConversationMemberMapper.selectByConversationId(conversationId);
        List<Long> memberIds = new ArrayList<>();
        
        if (members != null) {
            for (ImConversationMember member : members) {
                if (member.getUserId() != null) {
                    memberIds.add(member.getUserId());
                }
            }
        }
        
        return memberIds;
    }

    @Override
    public Integer getUnreadCount(Long conversationId, Long userId) {
        ImConversationMember member = imConversationMemberMapper.selectByConversationIdAndUserId(conversationId, userId);
        if (member != null) {
            return member.getUnreadCount() != null ? member.getUnreadCount() : 0;
        }
        return 0;
    }

    @Override
    public List<java.util.Map<String, Object>> getReadStatus(Long conversationId, Long messageId) {
        // 实现消息已读状态查询
        List<java.util.Map<String, Object>> result = new ArrayList<>();
        // 这里可以根据实际业务逻辑实现，暂时返回空列表
        return result;
    }
}