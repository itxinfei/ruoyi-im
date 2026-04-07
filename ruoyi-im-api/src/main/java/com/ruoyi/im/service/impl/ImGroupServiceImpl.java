package com.ruoyi.im.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.im.constant.SystemConstants;
import com.ruoyi.im.domain.ImConversation;
import com.ruoyi.im.domain.ImConversationMember;
import com.ruoyi.im.domain.ImGroup;
import com.ruoyi.im.domain.ImGroupMember;
import com.ruoyi.im.domain.ImUser;
import com.ruoyi.im.dto.group.ImGroupCreateRequest;
import com.ruoyi.im.dto.group.ImGroupUpdateRequest;
import com.ruoyi.im.exception.BusinessException;
import com.ruoyi.im.mapper.ImConversationMapper;
import com.ruoyi.im.mapper.ImConversationMemberMapper;
import com.ruoyi.im.mapper.ImGroupMapper;
import com.ruoyi.im.mapper.ImGroupMemberMapper;
import com.ruoyi.im.mapper.ImUserMapper;
import com.ruoyi.im.service.ImGroupService;
import com.ruoyi.im.util.BeanCopyUtil;
import com.ruoyi.im.util.BusinessExceptionHelper;
import com.ruoyi.im.vo.group.ImGroupMemberVO;
import com.ruoyi.im.vo.group.ImGroupVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 群组服务实现
 *
 * @author ruoyi
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ImGroupServiceImpl implements ImGroupService {

    private final ImGroupMapper imGroupMapper;
    private final ImGroupMemberMapper imGroupMemberMapper;
    private final ImUserMapper imUserMapper;
    private final ImConversationMapper imConversationMapper;
    private final com.ruoyi.im.util.ImRedisUtil imRedisUtil;
    private final ImConversationMemberMapper imConversationMemberMapper;

    /**
     * 构造器注入依赖
     */
    public ImGroupServiceImpl(ImGroupMapper imGroupMapper,
                              ImGroupMemberMapper imGroupMemberMapper,
                              ImUserMapper imUserMapper,
                              ImConversationMapper imConversationMapper,
                              com.ruoyi.im.util.ImRedisUtil imRedisUtil,
                              ImConversationMemberMapper imConversationMemberMapper) {
        this.imGroupMapper = imGroupMapper;
        this.imGroupMemberMapper = imGroupMemberMapper;
        this.imUserMapper = imUserMapper;
        this.imConversationMapper = imConversationMapper;
        this.imRedisUtil = imRedisUtil;
        this.imConversationMemberMapper = imConversationMemberMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createGroup(ImGroupCreateRequest request, Long userId) {
        ImUser owner = imUserMapper.selectImUserById(userId);
        if (owner == null) {
            BusinessExceptionHelper.throwUserNotFound();
        }

        ImGroup group = new ImGroup();
        group.setName(request.getName());
        group.setOwnerId(userId);
        group.setAvatar(request.getAvatar());
        group.setDescription(request.getDescription());
        // 使用Optional优化参数默认值设置
        group.setType(Optional.ofNullable(request.getType()).orElse("PRIVATE"));
        group.setMaxMembers(Optional.ofNullable(request.getMemberLimit())
                .orElse(SystemConstants.DEFAULT_GROUP_MEMBER_LIMIT));
        group.setMemberCount(1);
        group.setStatus("NORMAL");
        group.setCreateTime(LocalDateTime.now());
        group.setUpdateTime(LocalDateTime.now());

        imGroupMapper.insertImGroup(group);

        // 创建群组对应的会话（确保群组在会话列表中显示）
        ImConversation conversation = new ImConversation();
        conversation.setType("GROUP");
        conversation.setTargetId(group.getId()); // 关联群组ID
        conversation.setName(group.getName());
        conversation.setAvatar(group.getAvatar());
        conversation.setLastMessageId(null);
        conversation.setCreateTime(LocalDateTime.now());
        conversation.setUpdateTime(LocalDateTime.now());
        imConversationMapper.insert(conversation);

        ImGroupMember ownerMember = new ImGroupMember();
        ownerMember.setGroupId(group.getId());
        ownerMember.setUserId(userId);
        ownerMember.setRole("OWNER");
        ownerMember.setJoinedTime(LocalDateTime.now());
        ownerMember.setCreateTime(LocalDateTime.now());
        ownerMember.setUpdateTime(LocalDateTime.now());
        imGroupMemberMapper.insertImGroupMember(ownerMember);

        // 添加群主到会话成员
        addConversationMember(conversation.getId(), userId);

        if (request.getMemberIds() != null && !request.getMemberIds().isEmpty()) {
            for (Long memberId : request.getMemberIds()) {
                ImGroupMember member = new ImGroupMember();
                member.setGroupId(group.getId());
                member.setUserId(memberId);
                member.setRole("MEMBER");
                member.setInviterId(userId);
                member.setJoinedTime(LocalDateTime.now());
                member.setCreateTime(LocalDateTime.now());
                member.setUpdateTime(LocalDateTime.now());
                imGroupMemberMapper.insertImGroupMember(member);

                // 添加成员到会话
                addConversationMember(conversation.getId(), memberId);
            }
            group.setMemberCount(group.getMemberCount() + request.getMemberIds().size());
            imGroupMapper.updateImGroup(group);
        }

        return group.getId();
    }

    @Override
    public void updateGroup(Long groupId, ImGroupUpdateRequest request, Long userId) {
        ImGroup group = imGroupMapper.selectImGroupById(groupId);
        if (group == null) {
            BusinessExceptionHelper.throwGroupNotFound();
        }

        ImGroupMember operator = imGroupMemberMapper.selectImGroupMemberByGroupIdAndUserId(groupId, userId);
        if (operator == null || (!"OWNER".equals(operator.getRole()) && !"ADMIN".equals(operator.getRole()))) {
            BusinessExceptionHelper.throwNoPermission("无权限修改群组信息");
        }

        if (request.getName() != null) {
            group.setName(request.getName());
        }
        if (request.getAvatar() != null) {
            group.setAvatar(request.getAvatar());
        }
        if (request.getNotice() != null) {
            group.setNotice(request.getNotice());
        }
        if (request.getDescription() != null) {
            group.setDescription(request.getDescription());
        }
        if (request.getMemberLimit() != null) {
            group.setMaxMembers(request.getMemberLimit());
        }
        group.setUpdateTime(LocalDateTime.now());
        imGroupMapper.updateImGroup(group);

        // 清除缓存
        imRedisUtil.evictGroupInfo(groupId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void dismissGroup(Long groupId, Long userId) {
        ImGroup group = imGroupMapper.selectImGroupById(groupId);
        if (group == null) {
            BusinessExceptionHelper.throwGroupNotFound();
        }

        if (!group.getOwnerId().equals(userId)) {
            BusinessExceptionHelper.throwOwnerOnly();
        }

        group.setIsDeleted(1);
        group.setDeletedTime(LocalDateTime.now());
        group.setUpdateTime(LocalDateTime.now());
        imGroupMapper.updateImGroup(group);

        imGroupMemberMapper.deleteImGroupMemberByGroupIds(new Long[] { groupId });

        // 清除缓存
        imRedisUtil.evictGroupInfo(groupId);
    }

    @Override
    public ImGroupVO getGroupById(Long groupId, Long userId) {
        // 使用缓存获取群组信息
        ImGroupVO vo = imRedisUtil.getOrLoadGroupInfo(groupId, ImGroupVO.class, () -> {
            ImGroup group = imGroupMapper.selectImGroupById(groupId);
            if (group == null) {
                BusinessExceptionHelper.throwGroupNotFound();
            }
            ImGroupVO groupVO = new ImGroupVO();
            BeanUtils.copyProperties(group, groupVO);

            ImUser owner = imUserMapper.selectImUserById(group.getOwnerId());
            if (owner != null) {
                groupVO.setOwnerName(owner.getNickname());
            }
            return groupVO;
        });

        // 个人角色信息不缓存，实时查询
        ImGroupMember myMember = imGroupMemberMapper.selectImGroupMemberByGroupIdAndUserId(groupId, userId);
        if (myMember != null) {
            vo.setMyRole(myMember.getRole());
            vo.setIsMuted(myMember.getMuteEndTime() != null && myMember.getMuteEndTime().isAfter(LocalDateTime.now()));
        }

        return vo;
    }

    @Override
    public List<ImGroupVO> getUserGroups(Long userId) {
        ImGroupMember query = new ImGroupMember();
        query.setUserId(userId);
        List<ImGroupMember> memberList = imGroupMemberMapper.selectImGroupMemberList(query);

        List<Long> groupIds = memberList.stream()
                .map(ImGroupMember::getGroupId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, ImGroup> groupMap = new HashMap<>();
        if (!groupIds.isEmpty()) {
            List<ImGroup> groups = imGroupMapper.selectBatchIds(groupIds);
            groups.forEach(group -> groupMap.put(group.getId(), group));
        }

        return memberList.stream()
                .map(member -> {
                    ImGroup group = groupMap.get(member.getGroupId());
                    if (group == null || group.getIsDeleted() != 0) {
                        return null;
                    }
                    return BeanCopyUtil.copyAndSet(group, ImGroupVO.class, vo -> {
                        vo.setMyRole(member.getRole());
                        vo.setIsMuted(member.getMuteEndTime() != null && member.getMuteEndTime().isAfter(LocalDateTime.now()));
                    });
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public List<ImGroupMemberVO> getGroupMembers(Long groupId, Long userId) {
        // 校验请求者是否为群组成员，防止IDOR
        ImGroupMember requester = imGroupMemberMapper.selectImGroupMemberByGroupIdAndUserId(groupId, userId);
        if (requester == null) {
            throw new BusinessException("无权查看群成员列表");
        }

        List<ImGroupMember> memberList = imGroupMemberMapper.selectImGroupMemberListByGroupId(groupId);

        List<Long> userIds = memberList.stream()
                .map(ImGroupMember::getUserId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, ImUser> userMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            List<ImUser> users = imUserMapper.selectImUserListByIds(userIds);
            users.forEach(user -> userMap.put(user.getId(), user));
        }

        return memberList.stream()
                .map(member -> {
                    ImUser user = userMap.get(member.getUserId());
                    return BeanCopyUtil.copyAndSet(member, ImGroupMemberVO.class, vo -> {
                        // 使用Optional优化用户信息设置
                        Optional.ofNullable(user).ifPresent(u -> {
                            vo.setUserName(u.getNickname());
                            vo.setUserAvatar(u.getAvatar());
                        });
                        vo.setIsMuted(Optional.ofNullable(member.getMuteEndTime())
                                .map(endTime -> endTime.isAfter(LocalDateTime.now()))
                                .orElse(false));
                    });
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addMembers(Long groupId, List<Long> userIds, Long operatorId) {
        ImGroup group = imGroupMapper.selectImGroupById(groupId);
        if (group == null) {
            BusinessExceptionHelper.throwGroupNotFound();
        }

        ImGroupMember operator = imGroupMemberMapper.selectImGroupMemberByGroupIdAndUserId(groupId, operatorId);
        if (operator == null || (!"OWNER".equals(operator.getRole()) && !"ADMIN".equals(operator.getRole()))) {
            BusinessExceptionHelper.throwNoPermission("无权限添加成员");
        }

        // 查找群组对应的会话
        ImConversation conversation = imConversationMapper.selectByTypeAndTarget("GROUP", groupId, null);

        int addedCount = 0;
        for (Long userId : userIds) {
            // 检查成员数量上限（每次添加前检查，防止超限）
            ImGroup currentGroup = imGroupMapper.selectImGroupById(groupId);
            if (currentGroup.getMemberCount() + addedCount >= currentGroup.getMaxMembers()) {
                break;
            }

            ImGroupMember existing = imGroupMemberMapper.selectImGroupMemberByGroupIdAndUserId(groupId, userId);
            if (existing != null) {
                continue;
            }

            ImGroupMember member = new ImGroupMember();
            member.setGroupId(groupId);
            member.setUserId(userId);
            member.setRole("MEMBER");
            member.setInviterId(operatorId);
            member.setJoinedTime(LocalDateTime.now());
            member.setCreateTime(LocalDateTime.now());
            member.setUpdateTime(LocalDateTime.now());
            imGroupMemberMapper.insertImGroupMember(member);
            addedCount++;

            // 添加成员到会话
            if (conversation != null) {
                addConversationMember(conversation.getId(), userId);
            }
        }

        if (addedCount > 0) {
            group = imGroupMapper.selectImGroupById(groupId);
            group.setMemberCount(group.getMemberCount() + addedCount);
            group.setUpdateTime(LocalDateTime.now());
            imGroupMapper.updateImGroup(group);

            // 清除缓存
            imRedisUtil.evictGroupInfo(groupId);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeMembers(Long groupId, List<Long> userIds, Long operatorId) {
        ImGroup group = imGroupMapper.selectImGroupById(groupId);
        if (group == null) {
            BusinessExceptionHelper.throwGroupNotFound();
        }

        ImGroupMember operator = imGroupMemberMapper.selectImGroupMemberByGroupIdAndUserId(groupId, operatorId);
        if (operator == null || (!"OWNER".equals(operator.getRole()) && !"ADMIN".equals(operator.getRole()))) {
            BusinessExceptionHelper.throwNoPermission("无权限移除成员");
        }

        int removedCount = 0;
        for (Long userId : userIds) {
            if (userId.equals(group.getOwnerId())) {
                BusinessExceptionHelper.throwCannotRemoveOwner();
            }

            ImGroupMember member = imGroupMemberMapper.selectImGroupMemberByGroupIdAndUserId(groupId, userId);
            if (member != null) {
                if ("OWNER".equals(member.getRole())) {
                    BusinessExceptionHelper.throwCannotRemoveOwner();
                }
                if ("ADMIN".equals(member.getRole()) && !"OWNER".equals(operator.getRole())) {
                            BusinessExceptionHelper.throwNotAllowed("管理员不能移除其他管理员");
                        }
                imGroupMemberMapper.deleteImGroupMemberById(member.getId());
                removedCount++;
            }
        }

        group.setMemberCount(group.getMemberCount() - removedCount);
        group.setUpdateTime(LocalDateTime.now());
        imGroupMapper.updateImGroup(group);

        // 清除缓存
        imRedisUtil.evictGroupInfo(groupId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void quitGroup(Long groupId, Long userId) {
        ImGroup group = imGroupMapper.selectImGroupById(groupId);
        if (group == null) {
            BusinessExceptionHelper.throwGroupNotFound();
        }

        if (group.getOwnerId().equals(userId)) {
            BusinessExceptionHelper.throwNotAllowed("群主不能退出群组，请先转让群主或解散群组");
        }

        ImGroupMember member = imGroupMemberMapper.selectImGroupMemberByGroupIdAndUserId(groupId, userId);
        if (member == null) {
            BusinessExceptionHelper.throwNotInGroup();
        }

        imGroupMemberMapper.deleteImGroupMemberById(member.getId());

        group.setMemberCount(group.getMemberCount() - 1);
        group.setUpdateTime(LocalDateTime.now());
        imGroupMapper.updateImGroup(group);

        // 清除缓存
        imRedisUtil.evictGroupInfo(groupId);
    }

    @Override
    public void setAdmin(Long groupId, Long userId, Boolean isAdmin, Long operatorId) {
        ImGroup group = imGroupMapper.selectImGroupById(groupId);
        if (group == null) {
            BusinessExceptionHelper.throwGroupNotFound();
        }

        if (!group.getOwnerId().equals(operatorId)) {
            BusinessExceptionHelper.throwOwnerOnly();
        }

        ImGroupMember member = imGroupMemberMapper.selectImGroupMemberByGroupIdAndUserId(groupId, userId);
        if (member == null) {
            BusinessExceptionHelper.throwNotInGroup();
        }

        if (userId.equals(operatorId)) {
            BusinessExceptionHelper.throwNotAllowed("不能修改自己的角色");
        }

        if (isAdmin) {
            member.setRole("ADMIN");
        } else {
            member.setRole("MEMBER");
        }
        member.setUpdateTime(LocalDateTime.now());
        imGroupMemberMapper.updateImGroupMember(member);

        // 虽然群组基本信息没变，但成员角色变了，可能影响展示，暂时不清除groupInfo，因为groupInfo主要是群组描述等。
        // 但是getGroupById不返回成员列表。
        // 所以这里不需要evictGroupInfo。
    }

    @Override
    public void muteMember(Long groupId, Long userId, Long duration, Long operatorId) {
        ImGroup group = imGroupMapper.selectImGroupById(groupId);
        if (group == null) {
            BusinessExceptionHelper.throwGroupNotFound();
        }

        ImGroupMember operator = imGroupMemberMapper.selectImGroupMemberByGroupIdAndUserId(groupId, operatorId);
        if (operator == null || (!"OWNER".equals(operator.getRole()) && !"ADMIN".equals(operator.getRole()))) {
            BusinessExceptionHelper.throwNoPermission("无权限禁言成员");
        }

        ImGroupMember member = imGroupMemberMapper.selectImGroupMemberByGroupIdAndUserId(groupId, userId);
        if (member == null) {
            BusinessExceptionHelper.throwNotInGroup();
        }

        if ("OWNER".equals(member.getRole())) {
            BusinessExceptionHelper.throwNotAllowed("不能禁言群主");
        }

        if ("ADMIN".equals(member.getRole()) && !"OWNER".equals(operator.getRole())) {
            BusinessExceptionHelper.throwNotAllowed("管理员不能禁言其他管理员");
        }

        // 使用Optional优化禁言时长处理
        member.setMuteEndTime(Optional.ofNullable(duration)
                .filter(d -> d > 0)
                .map(d -> LocalDateTime.now().plusMinutes(d))
                .orElse(null));
        member.setUpdateTime(LocalDateTime.now());
        imGroupMemberMapper.updateImGroupMember(member);

        // 不需要清除groupInfo
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void transferOwner(Long groupId, Long newOwnerId, Long operatorId) {
        ImGroup group = imGroupMapper.selectImGroupById(groupId);
        if (group == null) {
            BusinessExceptionHelper.throwGroupNotFound();
        }

        if (!group.getOwnerId().equals(operatorId)) {
            BusinessExceptionHelper.throwOwnerOnly();
        }

        if (newOwnerId.equals(operatorId)) {
            BusinessExceptionHelper.throwNotAllowed("不能转让给自己");
        }

        ImGroupMember newOwner = imGroupMemberMapper.selectImGroupMemberByGroupIdAndUserId(groupId, newOwnerId);
        if (newOwner == null) {
            BusinessExceptionHelper.throwNotAllowed("新群主不在群组中");
        }

        ImGroupMember oldOwner = imGroupMemberMapper.selectImGroupMemberByGroupIdAndUserId(groupId, operatorId);

        oldOwner.setRole("ADMIN");
        oldOwner.setUpdateTime(LocalDateTime.now());
        imGroupMemberMapper.updateImGroupMember(oldOwner);

        newOwner.setRole("OWNER");
        newOwner.setUpdateTime(LocalDateTime.now());
        imGroupMemberMapper.updateImGroupMember(newOwner);

        group.setOwnerId(newOwnerId);
        group.setUpdateTime(LocalDateTime.now());
        imGroupMapper.updateImGroup(group);

        // 清除缓存
        imRedisUtil.evictGroupInfo(groupId);
    }

    /**
     * 添加成员到会话
     *
     * @param conversationId 会话ID
     * @param userId         用户ID
     */
    private void addConversationMember(Long conversationId, Long userId) {
        ImConversationMember existing = imConversationMemberMapper.selectByConversationIdAndUserId(conversationId,
                userId);
        if (existing != null) {
            if (existing.getIsDeleted() != null && existing.getIsDeleted() == 1) {
                existing.setIsDeleted(0);
                existing.setUpdateTime(LocalDateTime.now());
                imConversationMemberMapper.updateById(existing);
            }
            return;
        }

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

    @Override
    public IPage<ImGroup> getGroupPage(Page<ImGroup> page, String keyword) {
        return imGroupMapper.selectGroupPage(page, keyword);
    }
}
