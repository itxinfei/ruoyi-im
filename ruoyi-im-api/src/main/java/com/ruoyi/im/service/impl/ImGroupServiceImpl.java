package com.ruoyi.im.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
import com.ruoyi.im.vo.group.ImGroupMemberVO;
import com.ruoyi.im.vo.group.ImGroupVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 群组服务实现
 *
 * @author ruoyi
 */
@Service
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
            throw new BusinessException("用户不存在");
        }

        ImGroup group = new ImGroup();
        group.setName(request.getName());
        group.setOwnerId(userId);
        group.setAvatar(request.getAvatar());
        group.setDescription(request.getDescription());
        group.setType(request.getType() != null ? request.getType() : "PRIVATE");
        group.setMaxMembers(request.getMemberLimit() != null ? request.getMemberLimit() : 500);
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
            throw new BusinessException("群组不存在");
        }

        ImGroupMember operator = imGroupMemberMapper.selectImGroupMemberByGroupIdAndUserId(groupId, userId);
        if (operator == null || (!"OWNER".equals(operator.getRole()) && !"ADMIN".equals(operator.getRole()))) {
            throw new BusinessException("无权限修改群组信息");
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
    public void dismissGroup(Long groupId, Long userId) {
        ImGroup group = imGroupMapper.selectImGroupById(groupId);
        if (group == null) {
            throw new BusinessException("群组不存在");
        }

        if (!group.getOwnerId().equals(userId)) {
            throw new BusinessException("只有群主可以解散群组");
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
                throw new BusinessException("群组不存在");
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
        List<ImGroupVO> voList = new ArrayList<>();

        ImGroupMember query = new ImGroupMember();
        query.setUserId(userId);
        List<ImGroupMember> memberList = imGroupMemberMapper.selectImGroupMemberList(query);

        for (ImGroupMember member : memberList) {
            // 这里可以考虑优化，先不走缓存，或者批量获取
            ImGroup group = imGroupMapper.selectImGroupById(member.getGroupId());
            if (group != null && group.getIsDeleted() == 0) {
                ImGroupVO vo = new ImGroupVO();
                BeanUtils.copyProperties(group, vo);
                vo.setMyRole(member.getRole());
                vo.setIsMuted(member.getMuteEndTime() != null && member.getMuteEndTime().isAfter(LocalDateTime.now()));
                voList.add(vo);
            }
        }

        return voList;
    }

    @Override
    public List<ImGroupMemberVO> getGroupMembers(Long groupId, Long userId) {
        List<ImGroupMemberVO> voList = new ArrayList<>();

        List<ImGroupMember> memberList = imGroupMemberMapper.selectImGroupMemberListByGroupId(groupId);

        for (ImGroupMember member : memberList) {
            ImGroupMemberVO vo = new ImGroupMemberVO();
            BeanUtils.copyProperties(member, vo);

            ImUser user = imUserMapper.selectImUserById(member.getUserId());
            if (user != null) {
                vo.setUserName(user.getNickname());
                vo.setUserAvatar(user.getAvatar());
            }

            vo.setIsMuted(member.getMuteEndTime() != null && member.getMuteEndTime().isAfter(LocalDateTime.now()));
            voList.add(vo);
        }

        return voList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addMembers(Long groupId, List<Long> userIds, Long operatorId) {
        ImGroup group = imGroupMapper.selectImGroupById(groupId);
        if (group == null) {
            throw new BusinessException("群组不存在");
        }

        ImGroupMember operator = imGroupMemberMapper.selectImGroupMemberByGroupIdAndUserId(groupId, operatorId);
        if (operator == null || (!"OWNER".equals(operator.getRole()) && !"ADMIN".equals(operator.getRole()))) {
            throw new BusinessException("无权限添加成员");
        }

        if (group.getMemberCount() + userIds.size() > group.getMaxMembers()) {
            throw new BusinessException("群组成员数量已达上限");
        }

        // 查找群组对应的会话
        ImConversation conversation = imConversationMapper.selectByTypeAndTarget("GROUP", groupId, null);

        int addedCount = 0;
        for (Long userId : userIds) {
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

        group.setMemberCount(group.getMemberCount() + addedCount);
        group.setUpdateTime(LocalDateTime.now());
        imGroupMapper.updateImGroup(group);

        // 清除缓存
        imRedisUtil.evictGroupInfo(groupId);
    }

    @Override
    public void removeMembers(Long groupId, List<Long> userIds, Long operatorId) {
        ImGroup group = imGroupMapper.selectImGroupById(groupId);
        if (group == null) {
            throw new BusinessException("群组不存在");
        }

        ImGroupMember operator = imGroupMemberMapper.selectImGroupMemberByGroupIdAndUserId(groupId, operatorId);
        if (operator == null || (!"OWNER".equals(operator.getRole()) && !"ADMIN".equals(operator.getRole()))) {
            throw new BusinessException("无权限移除成员");
        }

        for (Long userId : userIds) {
            if (userId.equals(group.getOwnerId())) {
                throw new BusinessException("不能移除群主");
            }

            ImGroupMember member = imGroupMemberMapper.selectImGroupMemberByGroupIdAndUserId(groupId, userId);
            if (member != null) {
                if ("OWNER".equals(member.getRole())) {
                    throw new BusinessException("不能移除群主");
                }
                if ("ADMIN".equals(member.getRole()) && !"OWNER".equals(operator.getRole())) {
                    throw new BusinessException("管理员不能移除其他管理员");
                }
                imGroupMemberMapper.deleteImGroupMemberById(member.getId());
            }
        }

        group.setMemberCount(group.getMemberCount() - userIds.size());
        group.setUpdateTime(LocalDateTime.now());
        imGroupMapper.updateImGroup(group);

        // 清除缓存
        imRedisUtil.evictGroupInfo(groupId);
    }

    @Override
    public void quitGroup(Long groupId, Long userId) {
        ImGroup group = imGroupMapper.selectImGroupById(groupId);
        if (group == null) {
            throw new BusinessException("群组不存在");
        }

        if (group.getOwnerId().equals(userId)) {
            throw new BusinessException("群主不能退出群组，请先转让群主或解散群组");
        }

        ImGroupMember member = imGroupMemberMapper.selectImGroupMemberByGroupIdAndUserId(groupId, userId);
        if (member == null) {
            throw new BusinessException("您不在该群组中");
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
            throw new BusinessException("群组不存在");
        }

        if (!group.getOwnerId().equals(operatorId)) {
            throw new BusinessException("只有群主可以设置管理员");
        }

        ImGroupMember member = imGroupMemberMapper.selectImGroupMemberByGroupIdAndUserId(groupId, userId);
        if (member == null) {
            throw new BusinessException("用户不在群组中");
        }

        if (userId.equals(operatorId)) {
            throw new BusinessException("不能修改自己的角色");
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
            throw new BusinessException("群组不存在");
        }

        ImGroupMember operator = imGroupMemberMapper.selectImGroupMemberByGroupIdAndUserId(groupId, operatorId);
        if (operator == null || (!"OWNER".equals(operator.getRole()) && !"ADMIN".equals(operator.getRole()))) {
            throw new BusinessException("无权限禁言成员");
        }

        ImGroupMember member = imGroupMemberMapper.selectImGroupMemberByGroupIdAndUserId(groupId, userId);
        if (member == null) {
            throw new BusinessException("用户不在群组中");
        }

        if ("OWNER".equals(member.getRole())) {
            throw new BusinessException("不能禁言群主");
        }

        if ("ADMIN".equals(member.getRole()) && !"OWNER".equals(operator.getRole())) {
            throw new BusinessException("管理员不能禁言其他管理员");
        }

        if (duration == null || duration <= 0) {
            member.setMuteEndTime(null);
        } else {
            member.setMuteEndTime(LocalDateTime.now().plusMinutes(duration));
        }
        member.setUpdateTime(LocalDateTime.now());
        imGroupMemberMapper.updateImGroupMember(member);

        // 不需要清除groupInfo
    }

    @Override
    public void transferOwner(Long groupId, Long newOwnerId, Long operatorId) {
        ImGroup group = imGroupMapper.selectImGroupById(groupId);
        if (group == null) {
            throw new BusinessException("群组不存在");
        }

        if (!group.getOwnerId().equals(operatorId)) {
            throw new BusinessException("只有群主可以转让群主");
        }

        if (newOwnerId.equals(operatorId)) {
            throw new BusinessException("不能转让给自己");
        }

        ImGroupMember newOwner = imGroupMemberMapper.selectImGroupMemberByGroupIdAndUserId(groupId, newOwnerId);
        if (newOwner == null) {
            throw new BusinessException("新群主不在群组中");
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
        // ... (unchanged)
        ImConversationMember existing = imConversationMemberMapper.selectByConversationIdAndUserId(conversationId,
                userId);
        if (existing != null) {
            // 用户已存在，恢复状态（如果被删除）
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
