package com.ruoyi.web.service;

import com.ruoyi.web.domain.ImGroupMember;
import java.util.List;
import java.util.Map;

/**
 * 群组成员Service接口
 *
 * @author ruoyi
 */
public interface ImGroupMemberService {

    /**
     * 查询群组成员
     *
     * @param id 群组成员主键
     * @return 群组成员
     */
    ImGroupMember selectImGroupMemberById(Long id);

    /**
     * 查询群组成员列表
     *
     * @param imGroupMember 群组成员
     * @return 群组成员集合
     */
    List<ImGroupMember> selectImGroupMemberList(ImGroupMember imGroupMember);

    /**
     * 根据群组ID查询成员列表
     *
     * @param groupId 群组ID
     * @return 成员列表
     */
    List<ImGroupMember> selectMembersByGroupId(Long groupId);

    /**
     * 统计群组成员数量
     *
     * @param groupId 群组ID
     * @return 成员数量
     */
    Integer countMembersByGroupId(Long groupId);

    /**
     * 查询用户加入的群组列表
     *
     * @param userId 用户ID
     * @return 群组成员列表
     */
    List<ImGroupMember> selectUserGroups(Long userId);

    /**
     * 新增群组成员
     *
     * @param imGroupMember 群组成员
     * @return 结果
     */
    int insertImGroupMember(ImGroupMember imGroupMember);

    /**
     * 新增群组成员（带日志记录）
     *
     * @param imGroupMember 群组成员
     * @param operatorId 操作者ID
     * @param operatorName 操作者名称
     * @return 结果
     */
    int insertImGroupMemberWithLog(ImGroupMember imGroupMember, Long operatorId, String operatorName);

    /**
     * 修改群组成员
     *
     * @param imGroupMember 群组成员
     * @return 结果
     */
    int updateImGroupMember(ImGroupMember imGroupMember);

    /**
     * 修改成员角色
     *
     * @param groupId 群组ID
     * @param userId 用户ID
     * @param role 角色
     * @return 结果
     */
    int updateMemberRole(Long groupId, Long userId, String role);

    /**
     * 修改成员角色（带日志记录）
     *
     * @param groupId 群组ID
     * @param userId 用户ID
     * @param role 角色
     * @param operatorId 操作者ID
     * @param operatorName 操作者名称
     * @param targetUserName 目标用户名称
     * @return 结果
     */
    int updateMemberRoleWithLog(Long groupId, Long userId, String role, Long operatorId, String operatorName, String targetUserName);

    /**
     * 批量删除群组成员
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    int deleteImGroupMemberByIds(Long[] ids);

    /**
     * 移除成员（软删除）
     *
     * @param groupId 群组ID
     * @param userId 用户ID
     * @return 结果
     */
    int removeMember(Long groupId, Long userId);

    /**
     * 移除成员（带日志记录）
     *
     * @param groupId 群组ID
     * @param userId 用户ID
     * @param operatorId 操作者ID
     * @param operatorName 操作者名称
     * @param targetUserName 目标用户名称
     * @return 结果
     */
    int removeMemberWithLog(Long groupId, Long userId, Long operatorId, String operatorName, String targetUserName);

    /**
     * 获取群组成员统计数据
     *
     * @return 统计数据
     */
    Map<String, Object> getMemberStatistics();

    /**
     * 获取群组信息
     *
     * @param groupId 群组ID
     * @return 群组信息
     */
    Map<String, Object> getGroupInfo(Long groupId);
}
