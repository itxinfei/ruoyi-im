package com.ruoyi.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.im.domain.ImGroup;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 群组Mapper接口
 * 
 * @author ruoyi
 * @date 2024-01-01
 */
@Mapper
public interface ImGroupMapper extends BaseMapper<ImGroup> {

    /**
     * 查询用户加入的群组列表（包含群主详细信息）
     * 
     * @param userId 用户ID
     * @return 群组列表
     */
    List<ImGroup> selectUserGroupsWithDetails(@Param("userId") Long userId);

    /**
     * 查询群组详细信息（包含群主信息和成员列表）
     * 
     * @param groupId 群组ID
     * @return 群组详细信息
     */
    ImGroup selectGroupWithDetails(@Param("groupId") Long groupId);

    /**
     * 查询用户在指定群组中的角色和权限信息
     * 
     * @param groupId 群组ID
     * @param userId 用户ID
     * @return 群组信息（包含用户角色）
     */
    ImGroup selectGroupWithUserRole(@Param("groupId") Long groupId, @Param("userId") Long userId);

    /**
     * 搜索群组（根据群组名称）
     * 
     * @param keyword 搜索关键词
     * @param userId 当前用户ID（用于过滤已加入的群组）
     * @return 群组列表
     */
    List<ImGroup> searchGroups(@Param("keyword") String keyword, @Param("userId") Long userId);

    /**
     * 查询用户创建的群组列表
     * 
     * @param ownerId 群主用户ID
     * @return 群组列表
     */
    List<ImGroup> selectGroupsByOwner(@Param("ownerId") Long ownerId);

    /**
     * 更新群组成员数量
     * 
     * @param groupId 群组ID
     * @param memberCount 成员数量
     * @return 更新数量
     */
    int updateMemberCount(@Param("groupId") Long groupId, @Param("memberCount") Integer memberCount);

    /**
     * 批量查询群组信息
     * 
     * @param groupIds 群组ID列表
     * @return 群组列表
     */
    List<ImGroup> selectBatchByIds(@Param("groupIds") List<Long> groupIds);

    /**
     * 查询活跃群组（根据最近消息时间）
     * 
     * @param days 天数
     * @param limit 限制数量
     * @return 群组列表
     */
    List<ImGroup> selectActiveGroups(@Param("days") int days, @Param("limit") int limit);
}