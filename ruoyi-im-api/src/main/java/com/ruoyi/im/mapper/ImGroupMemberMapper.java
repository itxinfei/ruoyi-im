package com.ruoyi.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.im.domain.ImGroupMember;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 群组成员Mapper接口
 *
 * @author ruoyi
 */
@Mapper
public interface ImGroupMemberMapper extends BaseMapper<ImGroupMember> {

    /**
     * 查询群组成员
     *
     * @param id 群组成员ID
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
     * 新增群组成员
     *
     * @param imGroupMember 群组成员
     * @return 结果
     */
    int insertImGroupMember(ImGroupMember imGroupMember);

    /**
     * 修改群组成员
     *
     * @param imGroupMember 群组成员
     * @return 结果
     */
    int updateImGroupMember(ImGroupMember imGroupMember);

    /**
     * 删除群组成员
     *
     * @param id 群组成员ID
     * @return 结果
     */
    int deleteImGroupMemberById(Long id);

    /**
     * 批量删除群组成员
     *
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    int deleteImGroupMemberByIds(Long[] ids);

    /**
     * 根据群组ID查询群组成员列表
     *
     * @param groupId 群组ID
     * @return 群组成员集合
     */
    List<ImGroupMember> selectImGroupMemberListByGroupId(Long groupId);

    /**
     * 根据群组ID和用户ID查询群组成员
     *
     * @param groupId 群组ID
     * @param userId  用户ID
     * @return 群组成员
     */
    ImGroupMember selectImGroupMemberByGroupIdAndUserId(Long groupId, Long userId);

    /**
     * 批量删除群组成员（根据群组ID）
     *
     * @param groupIds 群组ID数组
     * @return 结果
     */
    int deleteImGroupMemberByGroupIds(Long[] groupIds);

    /**
     * 统计群组成员数量
     *
     * @param groupId 群组ID
     * @return 成员数量
     */
    Integer countMembersByGroupId(@Param("groupId") Long groupId);

    /**
     * 查询群组成员列表（带用户信息）
     *
     * @param groupId 群组ID
     * @return 成员列表
     */
    List<Map<String, Object>> selectMembersByGroupId(@Param("groupId") Long groupId);

    /**
     * 根据群组ID和用户ID删除成员
     *
     * @param groupId 群组ID
     * @param userId 用户ID
     * @return 结果
     */
    int deleteByGroupIdAndUserId(@Param("groupId") Long groupId, @Param("userId") Long userId);

    /**
     * 批量删除群组成员（根据群组ID列表）
     *
     * @param groupIds 群组ID列表
     * @return 结果
     */
    int deleteByGroupIds(@Param("groupIds") List<Long> groupIds);
}
