package com.ruoyi.im.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.im.domain.ImGroupMember;

import java.util.List;

/**
 * 群组成员Service接口
 * 
 * @author ruoyi
 * @date 2024-01-01
 */
public interface IImGroupMemberService extends IService<ImGroupMember> {

    /**
     * 添加群组成员
     * 
     * @param groupId 群组ID
     * @param userIds 用户ID列表
     * @param operatorId 操作者ID
     * @return 添加数量
     */
    int addGroupMembers(Long groupId, List<Long> userIds, Long operatorId);

    /**
     * 移除群组成员
     * 
     * @param groupId 群组ID
     * @param userIds 用户ID列表
     * @param operatorId 操作者ID
     * @return 移除数量
     */
    int removeGroupMembers(Long groupId, List<Long> userIds, Long operatorId);

    /**
     * 用户主动退出群组
     * 
     * @param groupId 群组ID
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean leaveGroup(Long groupId, Long userId);

    /**
     * 查询群组成员列表
     * 
     * @param groupId 群组ID
     * @return 成员列表
     */
    List<ImGroupMember> selectGroupMembers(Long groupId);

    /**
     * 查询群组管理员列表
     * 
     * @param groupId 群组ID
     * @return 管理员列表
     */
    List<ImGroupMember> selectGroupAdmins(Long groupId);

    /**
     * 设置/取消群组管理员
     * 
     * @param groupId 群组ID
     * @param userId 用户ID
     * @param isAdmin 是否为管理员
     * @param operatorId 操作者ID
     * @return 是否成功
     */
    boolean setGroupAdmin(Long groupId, Long userId, boolean isAdmin, Long operatorId);

    /**
     * 禁言/解除禁言群组成员
     * 
     * @param groupId 群组ID
     * @param userId 用户ID
     * @param muteEndTime 禁言结束时间（null表示解除禁言）
     * @param operatorId 操作者ID
     * @return 是否成功
     */
    boolean muteGroupMember(Long groupId, Long userId, java.util.Date muteEndTime, Long operatorId);

    /**
     * 更新群组成员昵称
     * 
     * @param groupId 群组ID
     * @param userId 用户ID
     * @param nickname 群昵称
     * @return 是否成功
     */
    boolean updateMemberNickname(Long groupId, Long userId, String nickname);

    /**
     * 检查用户是否为群组成员
     * 
     * @param groupId 群组ID
     * @param userId 用户ID
     * @return 是否为成员
     */
    boolean isGroupMember(Long groupId, Long userId);

    /**
     * 获取群组成员信息
     * 
     * @param groupId 群组ID
     * @param userId 用户ID
     * @return 成员信息
     */
    ImGroupMember getGroupMember(Long groupId, Long userId);

    /**
     * 统计群组成员数量
     * 
     * @param groupId 群组ID
     * @return 成员数量
     */
    int countGroupMembers(Long groupId);

    /**
     * 搜索群组成员
     * 
     * @param groupId 群组ID
     * @param keyword 搜索关键词
     * @return 成员列表
     */
    List<ImGroupMember> searchGroupMembers(Long groupId, String keyword);

    /**
     * 查询用户加入的群组ID列表
     * 
     * @param userId 用户ID
     * @return 群组ID列表
     */
    List<Long> selectUserGroupIds(Long userId);

    /**
     * 批量查询用户在多个群组中的成员信息
     * 
     * @param userId 用户ID
     * @param groupIds 群组ID列表
     * @return 成员信息列表
     */
    List<ImGroupMember> selectUserMemberships(Long userId, List<Long> groupIds);

    /**
     * 删除用户在所有群组中的成员关系
     * 
     * @param userId 用户ID
     * @return 删除数量
     */
    int deleteUserAllMemberships(Long userId);

    /**
     * 删除群组的所有成员
     * 
     * @param groupId 群组ID
     * @return 删除数量
     */
    int deleteGroupAllMembers(Long groupId);

    /**
     * 检查用户是否被禁言
     * 
     * @param groupId 群组ID
     * @param userId 用户ID
     * @return 是否被禁言
     */
    boolean isMemberMuted(Long groupId, Long userId);
}