package com.ruoyi.im.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.im.mapper.ImGroupMemberMapper;
import com.ruoyi.im.domain.ImGroupMember;
import com.ruoyi.im.service.ImGroupMemberService;

/**
 * 群组成员Service业务层处理
 * 
 * @author ruoyi
 */
@Service
public class ImGroupMemberServiceImpl implements ImGroupMemberService {
    @Autowired
    private ImGroupMemberMapper imGroupMemberMapper;

    /**
     * 查询群组成员
     * 
     * @param id 群组成员ID
     * @return 群组成员
     */
    @Override
    public ImGroupMember selectImGroupMemberById(Long id) {
        return imGroupMemberMapper.selectImGroupMemberById(id);
    }

    /**
     * 查询群组成员列表
     * 
     * @param imGroupMember 群组成员
     * @return 群组成员
     */
    @Override
    public List<ImGroupMember> selectImGroupMemberList(ImGroupMember imGroupMember) {
        return imGroupMemberMapper.selectImGroupMemberList(imGroupMember);
    }

    /**
     * 新增群组成员
     * 
     * @param imGroupMember 群组成员
     * @return 结果
     */
    @Override
    public int insertImGroupMember(ImGroupMember imGroupMember) {
        return imGroupMemberMapper.insertImGroupMember(imGroupMember);
    }

    /**
     * 修改群组成员
     * 
     * @param imGroupMember 群组成员
     * @return 结果
     */
    @Override
    public int updateImGroupMember(ImGroupMember imGroupMember) {
        return imGroupMemberMapper.updateImGroupMember(imGroupMember);
    }

    /**
     * 批量删除群组成员
     * 
     * @param ids 需要删除的群组成员ID
     * @return 结果
     */
    @Override
    public int deleteImGroupMemberByIds(Long[] ids) {
        return imGroupMemberMapper.deleteImGroupMemberByIds(ids);
    }

    /**
     * 删除群组成员信息
     * 
     * @param id 群组成员ID
     * @return 结果
     */
    @Override
    public int deleteImGroupMemberById(Long id) {
        return imGroupMemberMapper.deleteImGroupMemberById(id);
    }
    
    /**
     * 根据群组ID查询群组成员列表
     * 
     * @param groupId 群组ID
     * @return 群组成员集合
     */
    @Override
    public List<ImGroupMember> selectImGroupMemberListByGroupId(Long groupId) {
        return imGroupMemberMapper.selectImGroupMemberListByGroupId(groupId);
    }
    
    /**
     * 根据群组ID和用户ID查询群组成员
     * 
     * @param groupId 群组ID
     * @param userId 用户ID
     * @return 群组成员
     */
    @Override
    public ImGroupMember selectImGroupMemberByGroupIdAndUserId(Long groupId, Long userId) {
        return imGroupMemberMapper.selectImGroupMemberByGroupIdAndUserId(groupId, userId);
    }
    
    /**
     * 添加群组成员
     * 
     * @param groupId 群组ID
     * @param userIds 用户ID列表
     * @param role 角色
     * @param inviterId 邀请人ID
     * @return 结果
     */
    @Override
    public int addGroupMembers(Long groupId, List<Long> userIds, String role, Long inviterId) {
        int result = 0;
        for (Long userId : userIds) {
            // 检查用户是否已经是群成员
            ImGroupMember existingMember = selectImGroupMemberByGroupIdAndUserId(groupId, userId);
            if (existingMember == null) {
                ImGroupMember member = new ImGroupMember();
                member.setGroupId(groupId);
                member.setUserId(userId);
                member.setRole(role != null ? role : "MEMBER");
                member.setInviterId(inviterId);
                
                result += insertImGroupMember(member);
            }
        }
        return result;
    }
    
    /**
     * 移除群组成员
     * 
     * @param groupId 群组ID
     * @param userId 用户ID
     * @param operatorId 操作人ID
     * @return 结果
     */
    @Override
    public int removeGroupMember(Long groupId, Long userId, Long operatorId) {
        ImGroupMember member = selectImGroupMemberByGroupIdAndUserId(groupId, userId);
        if (member != null) {
            return deleteImGroupMemberById(member.getId());
        }
        return 0;
    }
    
    /**
     * 更新群组成员角色
     * 
     * @param groupId 群组ID
     * @param userId 用户ID
     * @param newRole 新角色
     * @param operatorId 操作人ID
     * @return 结果
     */
    @Override
    public int updateGroupMemberRole(Long groupId, Long userId, String newRole, Long operatorId) {
        ImGroupMember member = selectImGroupMemberByGroupIdAndUserId(groupId, userId);
        if (member != null) {
            member.setRole(newRole);
            return updateImGroupMember(member);
        }
        return 0;
    }
    
    /**
     * 设置群组成员禁言时间
     * 
     * @param groupId 群组ID
     * @param userId 用户ID
     * @param muteEndTime 禁言结束时间
     * @param operatorId 操作人ID
     * @return 结果
     */
    @Override
    public int setGroupMemberMuteTime(Long groupId, Long userId, java.time.LocalDateTime muteEndTime, Long operatorId) {
        ImGroupMember member = selectImGroupMemberByGroupIdAndUserId(groupId, userId);
        if (member != null) {
            member.setMuteEndTime(muteEndTime);
            return updateImGroupMember(member);
        }
        return 0;
    }
}