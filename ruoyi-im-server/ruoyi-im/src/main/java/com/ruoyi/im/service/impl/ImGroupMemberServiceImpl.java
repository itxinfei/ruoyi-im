package com.ruoyi.im.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.im.domain.ImGroupMember;
import com.ruoyi.im.mapper.ImGroupMemberMapper;
import com.ruoyi.im.service.IImGroupMemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 群组成员Service业务层处理
 * 
 * @author ruoyi
 * @date 2024-01-01
 */
@Service
public class ImGroupMemberServiceImpl extends ServiceImpl<ImGroupMemberMapper, ImGroupMember> implements IImGroupMemberService {

    @Autowired
    private ImGroupMemberMapper imGroupMemberMapper;

    /**
     * 添加群组成员
     * 
     * @param groupId 群组ID
     * @param userIds 用户ID列表
     * @param operatorId 操作者ID
     * @return 添加数量
     */
    @Override
    @Transactional
    public int addGroupMembers(Long groupId, List<Long> userIds, Long operatorId) {
        if (userIds == null || userIds.isEmpty()) {
            return 0;
        }
        List<ImGroupMember> members = new ArrayList<>(userIds.size());
        Date now = new Date();
        for (Long userId : userIds) {
            ImGroupMember member = new ImGroupMember();
            member.setGroupId(groupId);
            member.setUserId(userId);
            member.setRole("MEMBER");
            member.setJoinedTime(now);
            member.setCreateTime(now);
            members.add(member);
        }
        return imGroupMemberMapper.insertBatch(members);
    }

    /**
     * 移除群组成员
     */
    @Override
    @Transactional
    public int removeGroupMembers(Long groupId, List<Long> userIds, Long operatorId) {
        if (userIds == null || userIds.isEmpty()) {
            return 0;
        }
        return imGroupMemberMapper.deleteBatchByGroupIdAndUserIds(groupId, userIds);
    }

    /**
     * 用户主动退出群组
     */
    @Override
    @Transactional
    public boolean leaveGroup(Long groupId, Long userId) {
        QueryWrapper<ImGroupMember> wrapper = new QueryWrapper<>();
        wrapper.eq("group_id", groupId).eq("user_id", userId);
        return this.remove(wrapper);
    }

    /**
     * 查询群组成员列表
     */
    @Override
    public List<ImGroupMember> selectGroupMembers(Long groupId) {
        return imGroupMemberMapper.selectGroupMembersWithDetails(groupId);
    }

    /**
     * 查询群组管理员列表
     */
    @Override
    public List<ImGroupMember> selectGroupAdmins(Long groupId) {
        return imGroupMemberMapper.selectGroupAdmins(groupId);
    }

    /**
     * 设置/取消群组管理员
     */
    @Override
    @Transactional
    public boolean setGroupAdmin(Long groupId, Long userId, boolean isAdmin, Long operatorId) {
        return imGroupMemberMapper.updateMemberRole(groupId, userId, isAdmin ? "ADMIN" : "MEMBER") > 0;
    }

    /**
     * 禁言/解除禁言群组成员
     */
    @Override
    @Transactional
    public boolean muteGroupMember(Long groupId, Long userId, java.util.Date muteEndTime, Long operatorId) {
        Long muteUntil = muteEndTime != null ? muteEndTime.getTime() : null;
        return imGroupMemberMapper.updateMemberMute(groupId, userId, muteUntil) > 0;
    }

    /**
     * 更新群组成员昵称
     */
    @Override
    public boolean updateMemberNickname(Long groupId, Long userId, String nickname) {
        QueryWrapper<ImGroupMember> wrapper = new QueryWrapper<>();
        wrapper.eq("group_id", groupId).eq("user_id", userId);
        ImGroupMember member = new ImGroupMember();
        member.setNickname(nickname);
        member.setUpdateTime(new Date());
        return this.update(member, wrapper);
    }

    /**
     * 检查用户是否为群组成员
     */
    @Override
    public boolean isGroupMember(Long groupId, Long userId) {
        return imGroupMemberMapper.isGroupMember(groupId, userId);
    }

    /**
     * 获取群组成员信息
     */
    @Override
    public ImGroupMember getGroupMember(Long groupId, Long userId) {
        return imGroupMemberMapper.selectGroupMember(groupId, userId);
    }

    /**
     * 统计群组成员数量
     */
    @Override
    public int countGroupMembers(Long groupId) {
        return imGroupMemberMapper.countGroupMembers(groupId);
    }

    /**
     * 搜索群组成员
     */
    @Override
    public List<ImGroupMember> searchGroupMembers(Long groupId, String keyword) {
        return imGroupMemberMapper.searchGroupMembers(groupId, keyword);
    }

    /**
     * 查询用户加入的群组ID列表
     */
    @Override
    public List<Long> selectUserGroupIds(Long userId) {
        return imGroupMemberMapper.selectUserGroupIds(userId);
    }

    /**
     * 批量查询用户在多个群组中的成员信息
     */
    @Override
    public List<ImGroupMember> selectUserMemberships(Long userId, List<Long> groupIds) {
        if (groupIds == null || groupIds.isEmpty()) {
            return new ArrayList<>();
        }
        List<ImGroupMember> result = new ArrayList<>();
        for (Long groupId : groupIds) {
            ImGroupMember m = imGroupMemberMapper.selectGroupMember(groupId, userId);
            if (m != null) {
                result.add(m);
            }
        }
        return result;
    }

    /**
     * 删除用户在所有群组中的成员关系
     */
    @Override
    @Transactional
    public int deleteUserAllMemberships(Long userId) {
        return imGroupMemberMapper.deleteByUserId(userId);
    }

    /**
     * 删除群组的所有成员
     */
    @Override
    @Transactional
    public int deleteGroupAllMembers(Long groupId) {
        QueryWrapper<ImGroupMember> wrapper = new QueryWrapper<>();
        wrapper.eq("group_id", groupId);
        return this.baseMapper.delete(wrapper);
    }

    /**
     * 检查用户是否被禁言
     */
    @Override
    public boolean isMemberMuted(Long groupId, Long userId) {
        ImGroupMember member = imGroupMemberMapper.selectGroupMember(groupId, userId);
        return member != null && Boolean.TRUE.equals(member.getMuted());
    }
}