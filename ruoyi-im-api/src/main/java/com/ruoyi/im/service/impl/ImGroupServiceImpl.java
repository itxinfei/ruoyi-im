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
import com.ruoyi.im.util.BeanConvertUtil;
import com.ruoyi.im.vo.group.ImGroupMemberVO;
import com.ruoyi.im.vo.group.ImGroupVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ruoyi.im.constant.SystemConstants;
import com.ruoyi.im.constants.StatusConstants;

/**
 * 群组服务实现
 *
 * @author ruoyi
 */
@Service
public class ImGroupServiceImpl implements ImGroupService {

    private static final Logger logger = LoggerFactory.getLogger(ImGroupServiceImpl.class);

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
        group.setType(request.getType() != null ? request.getType() : StatusConstants.ConversationType.GROUP);
        group.setMaxMembers(request.getMemberLimit() != null ? request.getMemberLimit() : 500);
        group.setMemberCount(1);
        group.setStatus("NORMAL");
        group.setCreateTime(LocalDateTime.now());
        group.setUpdateTime(LocalDateTime.now());

        imGroupMapper.insertImGroup(group);

        // 创建群组对应的会话（确保群组在会话列表中显示）
        ImConversation conversation = new ImConversation();
        conversation.setType(StatusConstants.ConversationType.GROUP);
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
        ownerMember.setRole(StatusConstants.GroupMemberRole.OWNER);
        ownerMember.setJoinedTime(LocalDateTime.now());
        ownerMember.setCreateTime(LocalDateTime.now());
        ownerMember.setUpdateTime(LocalDateTime.now());
        imGroupMemberMapper.insertImGroupMember(ownerMember);

        // 添加群主到会话成员
        addConversationMember(conversation.getId(), userId);

        if (request.getMemberIds() != null && !request.getMemberIds().isEmpty()) {
            // 过滤掉创建者自己的 ID，避免重复插入
            List<Long> validMemberIds = new ArrayList<>();
            for (Long memberId : request.getMemberIds()) {
                if (!memberId.equals(userId)) { // 排除创建者
                    validMemberIds.add(memberId);
                }
            }

            // 添加有效成员
            for (Long memberId : validMemberIds) {
                ImGroupMember member = new ImGroupMember();
                member.setGroupId(group.getId());
                member.setUserId(memberId);
                member.setRole(StatusConstants.GroupMemberRole.MEMBER);
                member.setInviterId(userId);
                member.setJoinedTime(LocalDateTime.now());
                member.setCreateTime(LocalDateTime.now());
                member.setUpdateTime(LocalDateTime.now());
                imGroupMemberMapper.insertImGroupMember(member);

                // 添加成员到会话
                addConversationMember(conversation.getId(), memberId);
            }
            group.setMemberCount(group.getMemberCount() + validMemberIds.size());
            imGroupMapper.updateImGroup(group);
        }

        return group.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateGroup(Long groupId, ImGroupUpdateRequest request, Long userId) {
        ImGroup group = imGroupMapper.selectImGroupById(groupId);
        if (group == null) {
            throw new BusinessException("群组不存在");
        }

        ImGroupMember operator = imGroupMemberMapper.selectImGroupMemberByGroupIdAndUserId(groupId, userId);
        if (operator == null || (!StatusConstants.GroupMemberRole.OWNER.equals(operator.getRole())
                && !StatusConstants.GroupMemberRole.ADMIN.equals(operator.getRole()))) {
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
    @Transactional(rollbackFor = Exception.class)
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
            BeanConvertUtil.copyProperties(group, groupVO);

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
            if (group != null && java.util.Objects.equals(group.getIsDeleted(), 0)) {
                ImGroupVO vo = new ImGroupVO();
                BeanConvertUtil.copyProperties(group, vo);
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
            BeanConvertUtil.copyProperties(member, vo);

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
        if (operator == null || (!StatusConstants.GroupMemberRole.OWNER.equals(operator.getRole())
                && !StatusConstants.GroupMemberRole.ADMIN.equals(operator.getRole()))) {
            throw new BusinessException("无权限添加成员");
        }

        if (group.getMemberCount() + userIds.size() > group.getMaxMembers()) {
            throw new BusinessException("群组成员数量已达上限");
        }

        // 查找群组对应的会话
        ImConversation conversation = imConversationMapper.selectByTypeAndTarget(
                StatusConstants.ConversationType.GROUP, groupId, null);

        int addedCount = 0;
        for (Long userId : userIds) {
            ImGroupMember existing = imGroupMemberMapper.selectImGroupMemberByGroupIdAndUserId(groupId, userId);
            if (existing != null) {
                continue;
            }

            ImGroupMember member = new ImGroupMember();
            member.setGroupId(groupId);
            member.setUserId(userId);
            member.setRole(StatusConstants.GroupMemberRole.MEMBER);
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
    @Transactional(rollbackFor = Exception.class)
    public void removeMembers(Long groupId, List<Long> userIds, Long operatorId) {
        ImGroup group = imGroupMapper.selectImGroupById(groupId);
        if (group == null) {
            throw new BusinessException("群组不存在");
        }

        ImGroupMember operator = imGroupMemberMapper.selectImGroupMemberByGroupIdAndUserId(groupId, operatorId);
        if (operator == null || (!StatusConstants.GroupMemberRole.OWNER.equals(operator.getRole())
                && !StatusConstants.GroupMemberRole.ADMIN.equals(operator.getRole()))) {
            throw new BusinessException("无权限移除成员");
        }

        for (Long userId : userIds) {
            if (userId.equals(group.getOwnerId())) {
                throw new BusinessException("不能移除群主");
            }

            ImGroupMember member = imGroupMemberMapper.selectImGroupMemberByGroupIdAndUserId(groupId, userId);
            if (member != null) {
                if (StatusConstants.GroupMemberRole.OWNER.equals(member.getRole())) {
                    throw new BusinessException("不能移除群主");
                }
                if (StatusConstants.GroupMemberRole.ADMIN.equals(member.getRole())
                        && !StatusConstants.GroupMemberRole.OWNER.equals(operator.getRole())) {
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
    @Transactional(rollbackFor = Exception.class)
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
    @Transactional(rollbackFor = Exception.class)
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
            member.setRole(StatusConstants.GroupMemberRole.ADMIN);
        } else {
            member.setRole(StatusConstants.GroupMemberRole.MEMBER);
        }
        member.setUpdateTime(LocalDateTime.now());
        imGroupMemberMapper.updateImGroupMember(member);

        // 虽然群组基本信息没变，但成员角色变了，可能影响展示，暂时不清除groupInfo，因为groupInfo主要是群组描述等。
        // 但是getGroupById不返回成员列表。
        // 所以这里不需要evictGroupInfo。
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void muteMember(Long groupId, Long userId, Long duration, Long operatorId) {
        ImGroup group = imGroupMapper.selectImGroupById(groupId);
        if (group == null) {
            throw new BusinessException("群组不存在");
        }

        ImGroupMember operator = imGroupMemberMapper.selectImGroupMemberByGroupIdAndUserId(groupId, operatorId);
        if (operator == null || (!StatusConstants.GroupMemberRole.OWNER.equals(operator.getRole())
                && !StatusConstants.GroupMemberRole.ADMIN.equals(operator.getRole()))) {
            throw new BusinessException("无权限禁言成员");
        }

        ImGroupMember member = imGroupMemberMapper.selectImGroupMemberByGroupIdAndUserId(groupId, userId);
        if (member == null) {
            throw new BusinessException("用户不在群组中");
        }

        if (StatusConstants.GroupMemberRole.OWNER.equals(member.getRole())) {
            throw new BusinessException("不能禁言群主");
        }

        if (StatusConstants.GroupMemberRole.ADMIN.equals(member.getRole())
                && !StatusConstants.GroupMemberRole.OWNER.equals(operator.getRole())) {
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
    @Transactional(rollbackFor = Exception.class)
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

        oldOwner.setRole(StatusConstants.GroupMemberRole.ADMIN);
        oldOwner.setUpdateTime(LocalDateTime.now());
        imGroupMemberMapper.updateImGroupMember(oldOwner);

        newOwner.setRole(StatusConstants.GroupMemberRole.OWNER);
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
        member.setRole(StatusConstants.GroupMemberRole.MEMBER);
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

    @Override
    public ImGroupVO adminGetGroupById(Long groupId) {
        ImGroup group = imGroupMapper.selectImGroupById(groupId);
        if (group == null) {
            return null;
        }

        ImGroupVO vo = new ImGroupVO();
        BeanConvertUtil.copyProperties(group, vo);

        // 设置成员数量
        Integer memberCount = imGroupMemberMapper.countMembersByGroupId(groupId);
        vo.setMemberCount(memberCount);

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adminDismissGroup(Long groupId) {
        ImGroup group = imGroupMapper.selectImGroupById(groupId);
        if (group == null) {
            throw new BusinessException("GROUP_NOT_EXIST", "群组不存在");
        }

        // 软删除群组
        group.setIsDeleted(SystemConstants.DELETED);
        group.setDeletedTime(LocalDateTime.now());
        group.setUpdateTime(LocalDateTime.now());
        imGroupMapper.updateImGroup(group);

        // 删除群组成员关系
        List<Long> groupIds = java.util.Collections.singletonList(groupId);
        imGroupMemberMapper.deleteByGroupIds(groupIds);

        // 清除缓存
        imRedisUtil.evictGroupInfo(groupId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public java.util.Map<String, Integer> adminBatchDismissGroups(List<Long> groupIds) {
        int successCount = 0;
        int failCount = 0;

        for (Long groupId : groupIds) {
            try {
                ImGroup group = imGroupMapper.selectImGroupById(groupId);
                if (group != null) {
                    group.setIsDeleted(SystemConstants.DELETED);
                    group.setDeletedTime(LocalDateTime.now());
                    group.setUpdateTime(LocalDateTime.now());
                    imGroupMapper.updateImGroup(group);
                    imRedisUtil.evictGroupInfo(groupId);
                    successCount++;
                } else {
                    failCount++;
                }
            } catch (Exception e) {
                logger.error("解散群组失败，groupId={}", groupId, e);
                failCount++;
            }
        }

        // 删除群组成员关系
        if (!groupIds.isEmpty()) {
            imGroupMemberMapper.deleteByGroupIds(groupIds);
        }

        java.util.Map<String, Integer> result = new java.util.HashMap<>();
        result.put("successCount", successCount);
        result.put("failCount", failCount);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adminUpdateGroup(Long groupId, ImGroupUpdateRequest request) {
        ImGroup group = imGroupMapper.selectImGroupById(groupId);
        if (group == null) {
            throw new BusinessException("GROUP_NOT_EXIST", "群组不存在");
        }

        BeanConvertUtil.copyProperties(request, group);
        group.setId(groupId);
        group.setUpdateTime(LocalDateTime.now());
        imGroupMapper.updateImGroup(group);

        // 清除缓存
        imRedisUtil.evictGroupInfo(groupId);
    }

    @Override
    public java.util.Map<String, Object> getGroupStats() {
        // 查询所有群组
        List<ImGroup> allGroups = imGroupMapper.selectImGroupList(new ImGroup());

        // 统计未删除的群组
        long total = allGroups.stream()
                .filter(g -> g.getIsDeleted() == null || g.getIsDeleted() == SystemConstants.NOT_DELETED)
                .count();

        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("total", total);

        return stats;
    }

    @Override
    public List<java.util.Map<String, Object>> adminGetGroupMembers(Long groupId) {
        List<java.util.Map<String, Object>> members = imGroupMemberMapper.selectMembersByGroupId(groupId);

        // 转换为适合前端显示的格式
        for (java.util.Map<String, Object> member : members) {
            String role = (String) member.get("role");
            if (StatusConstants.GroupMemberRole.OWNER.equals(role)) {
                member.put("roleDisplay", "群主");
            } else if (StatusConstants.GroupMemberRole.ADMIN.equals(role)) {
                member.put("roleDisplay", "管理员");
            } else {
                member.put("roleDisplay", "成员");
            }
        }

        return members;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adminRemoveMember(Long groupId, Long userId) {
        imGroupMemberMapper.deleteByGroupIdAndUserId(groupId, userId);
        // 清除缓存
        imRedisUtil.evictGroupInfo(groupId);
    }

    @Override
    public List<ImGroupVO> getCommonGroups(Long userId1, Long userId2) {
        // 查询两个用户共同加入的群组ID列表
        List<Long> commonGroupIds = imGroupMemberMapper.selectCommonGroupIds(userId1, userId2);

        if (commonGroupIds.isEmpty()) {
            return new ArrayList<>();
        }

        // 批量获取群组信息并转换为VO
        List<ImGroupVO> result = new ArrayList<>();
        for (Long groupId : commonGroupIds) {
            ImGroup group = imGroupMapper.selectImGroupById(groupId);
            if (group != null && java.util.Objects.equals(group.getIsDeleted(), 0)) {
                ImGroupVO vo = convertToVO(group);
                // 添加成员数量
                Integer memberCount = imGroupMemberMapper.countMembersByGroupId(groupId);
                vo.setMemberCount(memberCount != null ? memberCount : 0);
                result.add(vo);
            }
        }

        return result;
    }

    /**
     * 将群组实体转换为VO
     */
    private ImGroupVO convertToVO(ImGroup group) {
        ImGroupVO vo = new ImGroupVO();
        BeanConvertUtil.copyProperties(group, vo);
        return vo;
    }

    @Override
    public java.util.Map<String, String> getGroupQrcode(Long groupId, Long userId) {
        ImGroup group = imGroupMapper.selectImGroupById(groupId);
        if (group == null) {
            throw new BusinessException("群组不存在");
        }

        ImGroupMember member = imGroupMemberMapper.selectImGroupMemberByGroupIdAndUserId(groupId, userId);
        if (member == null) {
            throw new BusinessException("不是群成员");
        }

        // 检查是否已有有效的二维码
        String qrcodeUrl = group.getQrcodeUrl();
        java.time.LocalDateTime qrcodeExpireTime = group.getQrcodeExpireTime();

        // 如果二维码不存在或已过期，生成新的
        if (qrcodeUrl == null || qrcodeExpireTime == null ||
                java.time.LocalDateTime.now().isAfter(qrcodeExpireTime)) {
            return generateGroupQrcode(group);
        }

        java.util.Map<String, String> result = new java.util.HashMap<>();
        result.put("qrcodeUrl", qrcodeUrl);
        result.put("expireTime", qrcodeExpireTime != null ? qrcodeExpireTime.toString() : "");
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public java.util.Map<String, String> refreshGroupQrcode(Long groupId, Long userId) {
        ImGroup group = imGroupMapper.selectImGroupById(groupId);
        if (group == null) {
            throw new BusinessException("群组不存在");
        }

        ImGroupMember member = imGroupMemberMapper.selectImGroupMemberByGroupIdAndUserId(groupId, userId);
        if (member == null) {
            throw new BusinessException("不是群成员");
        }

        // 检查是否有权限（只有群主和管理员可以刷新）
        if (!StatusConstants.GroupMemberRole.OWNER.equals(member.getRole()) &&
                !StatusConstants.GroupMemberRole.ADMIN.equals(member.getRole())) {
            throw new BusinessException("无权限刷新二维码");
        }

        return generateGroupQrcode(group);
    }

    /**
     * 生成群二维码
     *
     * @param group 群组信息
     * @return 二维码信息
     */
    private java.util.Map<String, String> generateGroupQrcode(ImGroup group) {
        // 生成唯一的二维码标识
        String qrcodeId = java.util.UUID.randomUUID().toString().replace("-", "");
        // 设置二维码有效期（7天）
        java.time.LocalDateTime expireTime = java.time.LocalDateTime.now().plusDays(7);

        // 生成二维码内容（这里简化处理，实际应该调用二维码生成库）
        // 二维码内容格式: GROUP:groupId:qrcodeId
        String qrcodeContent = "GROUP:" + group.getId() + ":" + qrcodeId;

        // 这里应该调用二维码生成库（如 ZXing）生成图片并上传
        // 暂时返回一个占位URL，实际使用时需要实现真实的二维码生成逻辑
        String qrcodeUrl = "/api/im/group/qrcode/" + qrcodeId;

        // 更新群组二维码信息
        group.setQrcodeUrl(qrcodeUrl);
        group.setQrcodeExpireTime(expireTime);
        group.setUpdateTime(java.time.LocalDateTime.now());
        imGroupMapper.updateImGroup(group);

        // 清除缓存
        imRedisUtil.evictGroupInfo(group.getId());

        java.util.Map<String, String> result = new java.util.HashMap<>();
        result.put("qrcodeUrl", qrcodeUrl);
        result.put("expireTime", expireTime.toString());
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adminAddMembers(Long groupId, List<Long> userIds, String role) {
        if (userIds == null || userIds.isEmpty()) {
            return;
        }

        ImGroup group = imGroupMapper.selectImGroupById(groupId);
        if (group == null) {
            throw new RuntimeException("群组不存在");
        }

        for (Long userId : userIds) {
            // 检查是否已存在
            ImGroupMember existing = imGroupMemberMapper.selectImGroupMemberByGroupIdAndUserId(groupId, userId);
            if (existing == null) {
                ImGroupMember member = new ImGroupMember();
                member.setGroupId(groupId);
                member.setUserId(userId);
                member.setRole(role != null ? role : "MEMBER");
                member.setJoinedTime(LocalDateTime.now());
                member.setCreateTime(LocalDateTime.now());
                member.setUpdateTime(LocalDateTime.now());
                imGroupMemberMapper.insertImGroupMember(member);

                // 添加到会话成员
                ImConversation conversation = imConversationMapper.selectByTypeAndTarget(
                        "GROUP", groupId, null);
                if (conversation != null) {
                    addConversationMember(conversation.getId(), userId);
                }
            }
        }

        // 更新群组成员数量
        Integer memberCount = imGroupMemberMapper.countMembersByGroupId(groupId);
        group.setMemberCount(memberCount);
        group.setUpdateTime(LocalDateTime.now());
        imGroupMapper.updateImGroup(group);

        // 清除缓存
        imRedisUtil.evictGroupInfo(groupId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adminToggleGroupMute(Long groupId, Boolean muted) {
        ImGroup group = imGroupMapper.selectImGroupById(groupId);
        if (group == null) {
            throw new RuntimeException("群组不存在");
        }

        group.setAllMuted(muted ? 1 : 0);
        group.setUpdateTime(LocalDateTime.now());
        imGroupMapper.updateImGroup(group);

        // 清除缓存
        imRedisUtil.evictGroupInfo(groupId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adminBatchMuteMembers(Long groupId, List<Long> userIds, Integer duration) {
        if (userIds == null || userIds.isEmpty()) {
            return;
        }

        LocalDateTime muteEndTime = duration != null && duration > 0
                ? LocalDateTime.now().plusSeconds(duration)
                : null;

        for (Long userId : userIds) {
            ImGroupMember member = imGroupMemberMapper.selectImGroupMemberByGroupIdAndUserId(groupId, userId);
            if (member != null) {
                member.setMuteEndTime(muteEndTime);
                member.setUpdateTime(LocalDateTime.now());
                imGroupMemberMapper.updateImGroupMember(member);
            }
        }

        // 清除缓存
        imRedisUtil.evictGroupInfo(groupId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adminBatchUnmuteMembers(Long groupId, List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return;
        }

        for (Long userId : userIds) {
            ImGroupMember member = imGroupMemberMapper.selectImGroupMemberByGroupIdAndUserId(groupId, userId);
            if (member != null) {
                member.setMuteEndTime(null);
                member.setUpdateTime(LocalDateTime.now());
                imGroupMemberMapper.updateImGroupMember(member);
            }
        }

        // 清除缓存
        imRedisUtil.evictGroupInfo(groupId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adminTransferOwner(Long groupId, Long newOwnerId) {
        ImGroup group = imGroupMapper.selectImGroupById(groupId);
        if (group == null) {
            throw new RuntimeException("群组不存在");
        }

        ImGroupMember newOwner = imGroupMemberMapper.selectImGroupMemberByGroupIdAndUserId(groupId, newOwnerId);
        if (newOwner == null) {
            throw new RuntimeException("用户不在群组中");
        }

        ImGroupMember oldOwner = imGroupMemberMapper.selectImGroupMemberByGroupIdAndUserId(groupId, group.getOwnerId());
        if (oldOwner != null) {
            oldOwner.setRole("ADMIN");
            oldOwner.setUpdateTime(LocalDateTime.now());
            imGroupMemberMapper.updateImGroupMember(oldOwner);
        }

        newOwner.setRole("OWNER");
        newOwner.setUpdateTime(LocalDateTime.now());
        imGroupMemberMapper.updateImGroupMember(newOwner);

        group.setOwnerId(newOwnerId);
        group.setUpdateTime(LocalDateTime.now());
        imGroupMapper.updateImGroup(group);

        // 清除缓存
        imRedisUtil.evictGroupInfo(groupId);
    }
}
