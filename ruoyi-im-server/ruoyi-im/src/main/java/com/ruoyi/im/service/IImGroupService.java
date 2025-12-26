package com.ruoyi.im.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.im.domain.ImGroup;

import java.util.List;

/**
 * 群组Service接口
 * 
 * @author ruoyi
 * @date 2024-01-01
 */
public interface IImGroupService extends IService<ImGroup> {

    /**
     * 创建群组
     * 
     * @param group 群组信息
     * @param memberIds 初始成员ID列表
     * @return 是否成功
     */
    boolean createGroup(ImGroup group, List<Long> memberIds);

    /**
     * 解散群组
     * 
     * @param groupId 群组ID
     * @param operatorId 操作者ID
     * @return 是否成功
     */
    boolean dissolveGroup(Long groupId, Long operatorId);

    /**
     * 更新群组信息
     * 
     * @param group 群组信息
     * @param operatorId 操作者ID
     * @return 是否成功
     */
    boolean updateGroup(ImGroup group, Long operatorId);

    /**
     * 查询用户加入的群组列表
     * 
     * @param userId 用户ID
     * @return 群组列表
     */
    List<ImGroup> selectUserGroups(Long userId);

    /**
     * 查询群组详细信息（包含成员信息）
     * 
     * @param groupId 群组ID
     * @param userId 查询用户ID（用于权限验证）
     * @return 群组信息
     */
    ImGroup getGroupDetail(Long groupId, Long userId);

    /**
     * 搜索群组
     * 
     * @param keyword 搜索关键词
     * @param userId 搜索用户ID
     * @return 群组列表
     */
    List<ImGroup> searchGroups(String keyword, Long userId);

    /**
     * 检查用户是否为群主
     * 
     * @param groupId 群组ID
     * @param userId 用户ID
     * @return 是否为群主
     */
    boolean isGroupOwner(Long groupId, Long userId);

    /**
     * 检查用户是否为群管理员
     * 
     * @param groupId 群组ID
     * @param userId 用户ID
     * @return 是否为管理员
     */
    boolean isGroupAdmin(Long groupId, Long userId);

    /**
     * 转让群主
     * 
     * @param groupId 群组ID
     * @param currentOwnerId 当前群主ID
     * @param newOwnerId 新群主ID
     * @return 是否成功
     */
    boolean transferOwnership(Long groupId, Long currentOwnerId, Long newOwnerId);

    /**
     * 更新群组成员数量
     * 
     * @param groupId 群组ID
     * @return 是否成功
     */
    boolean updateMemberCount(Long groupId);

    /**
     * 批量查询群组信息
     * 
     * @param groupIds 群组ID列表
     * @return 群组列表
     */
    List<ImGroup> selectGroupsByIds(List<Long> groupIds);

    /**
     * 查询用户创建的群组
     * 
     * @param ownerId 群主ID
     * @return 群组列表
     */
    List<ImGroup> selectGroupsByOwner(Long ownerId);

    /**
     * 查询活跃群组列表
     * 
     * @param limit 限制数量
     * @return 群组列表
     */
    List<ImGroup> selectActiveGroups(int limit);

    /**
     * 统计用户加入的群组数量
     * 
     * @param userId 用户ID
     * @return 群组数量
     */
    int countUserGroups(Long userId);

    /**
     * 检查群组是否存在且活跃
     * 
     * @param groupId 群组ID
     * @return 是否存在且活跃
     */
    boolean isGroupActive(Long groupId);
}