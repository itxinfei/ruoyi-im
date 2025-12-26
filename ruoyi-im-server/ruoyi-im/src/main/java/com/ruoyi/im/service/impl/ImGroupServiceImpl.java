package com.ruoyi.im.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.im.domain.ImGroup;
import com.ruoyi.im.domain.ImGroupMember;
import com.ruoyi.im.mapper.ImGroupMapper;
import com.ruoyi.im.mapper.ImGroupMemberMapper;
import com.ruoyi.im.service.IImGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 群组Service业务层处理
 * 
 * @author ruoyi
 * @date 2024-01-01
 */
@Service
public class ImGroupServiceImpl extends ServiceImpl<ImGroupMapper, ImGroup> implements IImGroupService {

    @Autowired
    private ImGroupMapper imGroupMapper;

    @Autowired
    private ImGroupMemberMapper imGroupMemberMapper;

    /**
     * 创建群组
     * 
     * @param group 群组信息
     * @param memberIds 初始成员ID列表
     * @return 是否成功
     */
    @Override
    @Transactional
    public boolean createGroup(ImGroup group, List<Long> memberIds) {
        // 设置群主
        group.setOwnerId(memberIds.get(0));
        group.setStatus("NORMAL");
        group.setMemberCount(memberIds.size());
        group.setCreateTime(new Date());
        
        // 保存群组
        boolean result = this.save(group);
        if (!result) {
            return false;
        }
        
        // 添加群组成员
        List<ImGroupMember> members = new ArrayList<>();
        for (int i = 0; i < memberIds.size(); i++) {
            ImGroupMember member = new ImGroupMember();
            member.setGroupId(group.getId());
            member.setUserId(memberIds.get(i));
            member.setRole(i == 0 ? "OWNER" : "MEMBER");
            member.setJoinedTime(new Date());
            member.setCreateTime(new Date());
            members.add(member);
        }
        
        return imGroupMemberMapper.insertBatch(members) > 0;
    }

    /**
     * 解散群组
     * 
     * @param groupId 群组ID
     * @param operatorId 操作者ID
     * @return 是否成功
     */
    @Override
    @Transactional
    public boolean dissolveGroup(Long groupId, Long operatorId) {
        // 检查权限（只有群主可以解散群组）
        if (!isGroupOwner(groupId, operatorId)) {
            return false;
        }
        
        // 更新群组状态
        ImGroup group = new ImGroup();
        group.setId(groupId);
        group.setStatus("DISMISSED");
        group.setUpdateTime(new Date());
        
        return this.updateById(group);
    }

    /**
     * 更新群组信息
     * 
     * @param group 群组信息
     * @param operatorId 操作者ID
     * @return 是否成功
     */
    @Override
    public boolean updateGroup(ImGroup group, Long operatorId) {
        // 检查权限（群主和管理员可以修改群组信息）
        if (!isGroupOwner(group.getId(), operatorId) && !isGroupAdmin(group.getId(), operatorId)) {
            return false;
        }
        
        group.setUpdateTime(new Date());
        return this.updateById(group);
    }

    /**
     * 查询用户加入的群组列表
     * 
     * @param userId 用户ID
     * @return 群组列表
     */
    @Override
    public List<ImGroup> selectUserGroups(Long userId) {
        return imGroupMapper.selectUserGroupsWithDetails(userId);
    }

    /**
     * 查询群组详细信息（包含成员信息）
     * 
     * @param groupId 群组ID
     * @param userId 查询用户ID（用于权限验证）
     * @return 群组信息
     */
    @Override
    public ImGroup getGroupDetail(Long groupId, Long userId) {
        // 检查用户是否为群组成员
        if (!imGroupMemberMapper.isGroupMember(groupId, userId)) {
            return null;
        }
        
        return imGroupMapper.selectGroupWithDetails(groupId);
    }

    /**
     * 搜索群组
     * 
     * @param keyword 搜索关键词
     * @param userId 搜索用户ID
     * @return 群组列表
     */
    @Override
    public List<ImGroup> searchGroups(String keyword, Long userId) {
        return imGroupMapper.searchGroups(keyword, userId);
    }

    /**
     * 检查用户是否为群主
     * 
     * @param groupId 群组ID
     * @param userId 用户ID
     * @return 是否为群主
     */
    @Override
    public boolean isGroupOwner(Long groupId, Long userId) {
        ImGroupMember member = imGroupMemberMapper.selectGroupMember(groupId, userId);
        return member != null && "OWNER".equals(member.getRole());
    }

    /**
     * 检查用户是否为群管理员
     * 
     * @param groupId 群组ID
     * @param userId 用户ID
     * @return 是否为管理员
     */
    @Override
    public boolean isGroupAdmin(Long groupId, Long userId) {
        ImGroupMember member = imGroupMemberMapper.selectGroupMember(groupId, userId);
        return member != null && ("ADMIN".equals(member.getRole()) || "OWNER".equals(member.getRole()));
    }

    /**
     * 转让群主
     * 
     * @param groupId 群组ID
     * @param currentOwnerId 当前群主ID
     * @param newOwnerId 新群主ID
     * @return 是否成功
     */
    @Override
    @Transactional
    public boolean transferOwnership(Long groupId, Long currentOwnerId, Long newOwnerId) {
        // 检查当前用户是否为群主
        if (!isGroupOwner(groupId, currentOwnerId)) {
            return false;
        }
        
        // 检查新群主是否为群成员
        if (!imGroupMemberMapper.isGroupMember(groupId, newOwnerId)) {
            return false;
        }
        
        // 更新原群主角色为普通成员
        imGroupMemberMapper.updateMemberRole(groupId, currentOwnerId, "MEMBER");
        
        // 更新新群主角色
        imGroupMemberMapper.updateMemberRole(groupId, newOwnerId, "OWNER");
        
        // 更新群组的群主ID
        ImGroup group = new ImGroup();
        group.setId(groupId);
        group.setOwnerId(newOwnerId);
        group.setUpdateTime(new Date());
        
        return this.updateById(group);
    }

    /**
     * 更新群组成员数量
     * 
     * @param groupId 群组ID
     * @return 是否成功
     */
    @Override
    public boolean updateMemberCount(Long groupId) {
        int memberCount = imGroupMemberMapper.countGroupMembers(groupId);
        return imGroupMapper.updateMemberCount(groupId, memberCount) > 0;
    }

    /**
     * 批量查询群组信息
     * 
     * @param groupIds 群组ID列表
     * @return 群组列表
     */
    @Override
    public List<ImGroup> selectGroupsByIds(List<Long> groupIds) {
        if (groupIds == null || groupIds.isEmpty()) {
            return new ArrayList<>();
        }
        return imGroupMapper.selectBatchByIds(groupIds);
    }

    /**
     * 查询用户创建的群组
     * 
     * @param ownerId 群主ID
     * @return 群组列表
     */
    @Override
    public List<ImGroup> selectGroupsByOwner(Long ownerId) {
        return imGroupMapper.selectGroupsByOwner(ownerId);
    }

    /**
     * 查询活跃群组列表
     * 
     * @param limit 限制数量
     * @return 群组列表
     */
    @Override
    public List<ImGroup> selectActiveGroups(int limit) {
        return imGroupMapper.selectActiveGroups(7, limit); // 查询最近7天活跃的群组
    }

    /**
     * 统计用户加入的群组数量
     * 
     * @param userId 用户ID
     * @return 群组数量
     */
    @Override
    public int countUserGroups(Long userId) {
        List<Long> groupIds = imGroupMemberMapper.selectUserGroupIds(userId);
        return groupIds.size();
    }

    /**
     * 检查群组是否存在且活跃
     * 
     * @param groupId 群组ID
     * @return 是否存在且活跃
     */
    @Override
    public boolean isGroupActive(Long groupId) {
        ImGroup group = this.getById(groupId);
        return group != null && "NORMAL".equals(group.getStatus());
    }
}