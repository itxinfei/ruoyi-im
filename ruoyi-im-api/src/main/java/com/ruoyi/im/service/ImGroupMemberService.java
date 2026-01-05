package com.ruoyi.im.service;

import com.ruoyi.im.domain.ImGroupMember;
import java.util.List;

/**
 * 群组成员Service接口
 * 
 * @author ruoyi
 */
public interface ImGroupMemberService extends BaseService<ImGroupMember> {
    
    @Override
    ImGroupMember selectById(Long id);
    
    @Override
    List<ImGroupMember> selectList(ImGroupMember imGroupMember);
    
    @Override
    int insert(ImGroupMember imGroupMember);
    
    @Override
    int update(ImGroupMember imGroupMember);
    
    @Override
    int deleteByIds(Long[] ids);
    
    @Override
    int deleteById(Long id);
    
    /**
     * 根据群组ID查询群组成员列表
     * 
     * @param groupId 群组ID
     * @return 群组成员集合
     */
    public List<ImGroupMember> selectImGroupMemberListByGroupId(Long groupId);
    
    /**
     * 根据群组ID和用户ID查询群组成员
     * 
     * @param groupId 群组ID
     * @param userId 用户ID
     * @return 群组成员
     */
    public ImGroupMember selectImGroupMemberByGroupIdAndUserId(Long groupId, Long userId);
    
    /**
     * 添加群组成员
     * 
     * @param groupId 群组ID
     * @param userIds 用户ID列表
     * @param role 角色
     * @param inviterId 邀请人ID
     * @return 结果
     */
    public int addGroupMembers(Long groupId, List<Long> userIds, String role, Long inviterId);
    
    /**
     * 移除群组成员
     * 
     * @param groupId 群组ID
     * @param userId 用户ID
     * @param operatorId 操作人ID
     * @return 结果
     */
    public int removeGroupMember(Long groupId, Long userId, Long operatorId);
    
    /**
     * 更新群组成员角色
     * 
     * @param groupId 群组ID
     * @param userId 用户ID
     * @param newRole 新角色
     * @param operatorId 操作人ID
     * @return 结果
     */
    public int updateGroupMemberRole(Long groupId, Long userId, String newRole, Long operatorId);
    
    /**
     * 设置群组成员禁言时间
     * 
     * @param groupId 群组ID
     * @param userId 用户ID
     * @param muteEndTime 禁言结束时间
     * @param operatorId 操作人ID
     * @return 结果
     */
    public int setGroupMemberMuteTime(Long groupId, Long userId, java.time.LocalDateTime muteEndTime, Long operatorId);
}