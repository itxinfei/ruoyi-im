package com.ruoyi.web.mapper;

import com.ruoyi.web.domain.ImGroup;
import com.ruoyi.web.domain.ImGroupMember;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

/**
 * IM群组Mapper接口（Admin模块专用）
 *
 * @author ruoyi
 * @date 2025-01-07
 */
@Mapper
public interface ImGroupMapper {

    /**
     * 查询群组列表
     */
    List<ImGroup> selectImGroupList(ImGroup imGroup);

    /**
     * 获取群组详情
     */
    ImGroup selectImGroupById(Long id);

    /**
     * 新增群组
     */
    int insertImGroup(ImGroup imGroup);

    /**
     * 修改群组
     */
    int updateImGroup(ImGroup imGroup);

    /**
     * 删除群组
     */
    int deleteImGroupById(Long id);

    /**
     * 批量删除群组
     */
    int deleteImGroupByIds(Long[] ids);

    /**
     * 获取群组成员列表
     */
    List<ImGroupMember> selectGroupMembersByGroupId(Long groupId);

    /**
     * 添加群组成员
     */
    int addGroupMember(Long groupId, Long userId, String role, Long inviterId);

    /**
     * 解散群组（删除所有成员）
     */
    int dismissGroup(Long groupId);

    /**
     * 统计群组总数
     */
    int countTotalGroups();

    /**
     * 统计今日创建群组数
     */
    int countTodayCreatedGroups();

    /**
     * 统计群成员总数
     */
    int countTotalGroupMembers();

    /**
     * 统计活跃群组数（7天内有消息）
     */
    int countActiveGroups();

    /**
     * 统计全员禁言的群组数
     */
    int countMutedGroups();

    /**
     * 统计大群数（成员>100）
     */
    int countLargeGroups();
}
