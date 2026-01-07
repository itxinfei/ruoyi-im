package com.ruoyi.web.mapper;

import com.ruoyi.im.domain.ImGroup;
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
}
