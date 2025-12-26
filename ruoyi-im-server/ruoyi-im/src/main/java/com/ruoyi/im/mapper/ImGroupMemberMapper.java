package com.ruoyi.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.im.domain.ImGroupMember;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 群组成员Mapper接口
 * 
 * @author ruoyi
 * @date 2024-01-01
 */
@Mapper
public interface ImGroupMemberMapper extends BaseMapper<ImGroupMember> {

    /**
     * 查询群组成员列表（包含用户详细信息）
     * 
     * @param groupId 群组ID
     * @return 群组成员列表
     */
    List<ImGroupMember> selectGroupMembersWithDetails(@Param("groupId") Long groupId);

    /**
     * 查询用户在群组中的成员信息
     * 
     * @param groupId 群组ID
     * @param userId 用户ID
     * @return 群组成员信息
     */
    ImGroupMember selectGroupMember(@Param("groupId") Long groupId, @Param("userId") Long userId);

    /**
     * 检查用户是否为群组成员
     * 
     * @param groupId 群组ID
     * @param userId 用户ID
     * @return 是否为群组成员
     */
    boolean isGroupMember(@Param("groupId") Long groupId, @Param("userId") Long userId);

    /**
     * 统计群组成员数量
     * 
     * @param groupId 群组ID
     * @return 成员数量
     */
    int countGroupMembers(@Param("groupId") Long groupId);

    /**
     * 查询群组管理员列表
     * 
     * @param groupId 群组ID
     * @return 管理员列表
     */
    List<ImGroupMember> selectGroupAdmins(@Param("groupId") Long groupId);

    /**
     * 批量添加群组成员
     * 
     * @param members 成员列表
     * @return 添加数量
     */
    int insertBatch(@Param("members") List<ImGroupMember> members);

    /**
     * 批量删除群组成员
     * 
     * @param groupId 群组ID
     * @param userIds 用户ID列表
     * @return 删除数量
     */
    int deleteBatchByGroupIdAndUserIds(@Param("groupId") Long groupId, @Param("userIds") List<Long> userIds);

    /**
     * 更新成员角色
     * 
     * @param groupId 群组ID
     * @param userId 用户ID
     * @param role 新角色
     * @return 更新数量
     */
    int updateMemberRole(@Param("groupId") Long groupId, @Param("userId") Long userId, @Param("role") String role);

    /**
     * 更新成员禁言状态
     * 
     * @param groupId 群组ID
     * @param userId 用户ID
     * @param muteUntil 禁言截止时间
     * @return 更新数量
     */
    int updateMemberMute(@Param("groupId") Long groupId, @Param("userId") Long userId, @Param("muteUntil") Long muteUntil);

    /**
     * 搜索群组成员（根据用户名或昵称）
     * 
     * @param groupId 群组ID
     * @param keyword 搜索关键词
     * @return 群组成员列表
     */
    List<ImGroupMember> searchGroupMembers(@Param("groupId") Long groupId, @Param("keyword") String keyword);

    /**
     * 查询用户加入的群组ID列表
     * 
     * @param userId 用户ID
     * @return 群组ID列表
     */
    List<Long> selectUserGroupIds(@Param("userId") Long userId);

    /**
     * 删除用户在所有群组中的成员关系
     * 
     * @param userId 用户ID
     * @return 删除数量
     */
    int deleteByUserId(@Param("userId") Long userId);
}