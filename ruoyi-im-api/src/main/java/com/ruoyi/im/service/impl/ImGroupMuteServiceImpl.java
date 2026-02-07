package com.ruoyi.im.service.impl;

import com.ruoyi.im.constants.StatusConstants;
import com.ruoyi.im.domain.ImGroup;
import com.ruoyi.im.domain.ImGroupMember;
import com.ruoyi.im.exception.BusinessException;
import com.ruoyi.im.mapper.ImGroupMapper;
import com.ruoyi.im.mapper.ImGroupMemberMapper;
import com.ruoyi.im.service.ImGroupMuteService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 群组禁言服务实现
 *
 * @author ruoyi
 */
@Service
public class ImGroupMuteServiceImpl implements ImGroupMuteService {

    private final ImGroupMapper groupMapper;
    private final ImGroupMemberMapper groupMemberMapper;

    public ImGroupMuteServiceImpl(ImGroupMapper groupMapper, ImGroupMemberMapper groupMemberMapper) {
        this.groupMapper = groupMapper;
        this.groupMemberMapper = groupMemberMapper;
    }

    @Override
    @Transactional
    public void setAllMuted(Long groupId, Boolean allMuted, Long operatorId) {
        // 验证操作者权限：只有群主可以设置全员禁言
        ImGroup group = groupMapper.selectImGroupById(groupId);
        if (group == null) {
            throw new BusinessException("群组不存在");
        }

        ImGroupMember operator = groupMemberMapper.selectImGroupMemberByGroupIdAndUserId(groupId, operatorId);
        if (operator == null) {
            throw new BusinessException("您不是该群组成员");
        }

        if (!StatusConstants.GroupMemberRole.OWNER.equals(operator.getRole())) {
            throw new BusinessException("只有群主可以设置全员禁言");
        }

        // 设置全员禁言
        group.setAllMuted(allMuted ? 1 : 0);
        groupMapper.updateImGroup(group);
    }

    @Override
    @Transactional
    public void muteMember(Long groupId, Long userId, Integer muteDuration, Long operatorId) {
        // 验证操作者权限：只有群主和管理员可以禁言成员
        ImGroupMember operator = groupMemberMapper.selectImGroupMemberByGroupIdAndUserId(groupId, operatorId);
        if (operator == null) {
            throw new BusinessException("您不是该群组成员");
        }

        String operatorRole = operator.getRole();
        boolean isOwner = StatusConstants.GroupMemberRole.OWNER.equals(operatorRole);
        boolean isAdmin = StatusConstants.GroupMemberRole.ADMIN.equals(operatorRole);

        if (!isOwner && !isAdmin) {
            throw new BusinessException("只有群主和管理员可以禁言成员");
        }

        // 获取被禁言成员
        ImGroupMember member = groupMemberMapper.selectImGroupMemberByGroupIdAndUserId(groupId, userId);
        if (member == null) {
            throw new BusinessException("成员不存在");
        }

        // 管理员不能禁言群主和其他管理员
        String memberRole = member.getRole();
        if (isAdmin && ("OWNER".equals(memberRole) || "ADMIN".equals(memberRole))) {
            throw new BusinessException("管理员不能禁言群主和其他管理员");
        }

        // 计算禁言结束时间
        LocalDateTime muteEndTime = LocalDateTime.now().plusMinutes(muteDuration);
        member.setMuteEndTime(muteEndTime);
        groupMemberMapper.updateImGroupMember(member);
    }

    @Override
    @Transactional
    public void unmuteMember(Long groupId, Long userId, Long operatorId) {
        // 验证操作者权限：只有群主和管理员可以解除禁言
        ImGroupMember operator = groupMemberMapper.selectImGroupMemberByGroupIdAndUserId(groupId, operatorId);
        if (operator == null) {
            throw new BusinessException("您不是该群组成员");
        }

        String operatorRole = operator.getRole();
        boolean isOwner = StatusConstants.GroupMemberRole.OWNER.equals(operatorRole);
        boolean isAdmin = StatusConstants.GroupMemberRole.ADMIN.equals(operatorRole);

        if (!isOwner && !isAdmin) {
            throw new BusinessException("只有群主和管理员可以解除禁言");
        }

        // 获取被解除禁言成员
        ImGroupMember member = groupMemberMapper.selectImGroupMemberByGroupIdAndUserId(groupId, userId);
        if (member == null) {
            throw new BusinessException("成员不存在");
        }

        // 解除禁言
        member.setMuteEndTime(null);
        groupMemberMapper.updateImGroupMember(member);
    }

    @Override
    public boolean isUserMuted(Long groupId, Long userId) {
        // 检查个人禁言状态
        ImGroupMember member = groupMemberMapper.selectImGroupMemberByGroupIdAndUserId(groupId, userId);
        if (member == null) {
            return false;
        }

        // 管理员和群主不受禁言限制
        String role = member.getRole();
        if ("OWNER".equals(role) || "ADMIN".equals(role)) {
            return false;
        }

        // 检查是否在禁言期内
        LocalDateTime muteEndTime = member.getMuteEndTime();
        if (muteEndTime != null && muteEndTime.isAfter(LocalDateTime.now())) {
            return true;
        }

        // 检查是否全员禁言
        return isAllMuted(groupId);
    }

    @Override
    public LocalDateTime getMuteEndTime(Long groupId, Long userId) {
        ImGroupMember member = groupMemberMapper.selectImGroupMemberByGroupIdAndUserId(groupId, userId);
        if (member == null) {
            return null;
        }

        LocalDateTime muteEndTime = member.getMuteEndTime();
        if (muteEndTime != null && muteEndTime.isAfter(LocalDateTime.now())) {
            return muteEndTime;
        }

        return null;
    }

    @Override
    public boolean isAllMuted(Long groupId) {
        ImGroup group = groupMapper.selectImGroupById(groupId);
        return group != null && group.getAllMuted() != null && group.getAllMuted() == 1;
    }

    @Override
    @Transactional
    public void batchMuteMembers(Long groupId, List<Long> userIds, Integer muteDuration, Long operatorId) {
        // 验证操作者权限
        ImGroupMember operator = groupMemberMapper.selectImGroupMemberByGroupIdAndUserId(groupId, operatorId);
        if (operator == null) {
            throw new BusinessException("您不是该群组成员");
        }

        String operatorRole = operator.getRole();
        boolean isOwner = StatusConstants.GroupMemberRole.OWNER.equals(operatorRole);
        boolean isAdmin = StatusConstants.GroupMemberRole.ADMIN.equals(operatorRole);

        if (!isOwner && !isAdmin) {
            throw new BusinessException("只有群主和管理员可以禁言成员");
        }

        // 计算禁言结束时间
        LocalDateTime muteEndTime = LocalDateTime.now().plusMinutes(muteDuration);

        // 批量禁言
        for (Long userId : userIds) {
            ImGroupMember member = groupMemberMapper.selectImGroupMemberByGroupIdAndUserId(groupId, userId);
            if (member != null) {
                // 管理员不能禁言群主和其他管理员
                String memberRole = member.getRole();
                if (isAdmin && ("OWNER".equals(memberRole) || "ADMIN".equals(memberRole))) {
                    continue; // 跳过群主和管理员
                }
                member.setMuteEndTime(muteEndTime);
                groupMemberMapper.updateImGroupMember(member);
            }
        }
    }
}
