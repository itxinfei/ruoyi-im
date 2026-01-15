package com.ruoyi.web.service;

import com.ruoyi.web.domain.ImGroup;
import com.ruoyi.web.domain.ImGroupMember;

import java.util.List;
import java.util.Map;

/**
 * IM群组Service接口（Admin模块专用）
 */
public interface ImGroupService {

    List<ImGroup> selectImGroupList(ImGroup imGroup);

    ImGroup selectImGroupById(Long id);

    int insertImGroup(ImGroup imGroup);

    int updateImGroup(ImGroup imGroup);

    int deleteImGroupById(Long id);

    int deleteImGroupByIds(Long[] ids);

    List<ImGroupMember> selectGroupMembersByGroupId(Long groupId);

    int addGroupMember(Long groupId, Long userId, String role, Long inviterId);

    int dismissGroup(Long groupId);

    /**
     * 获取群组统计数据
     */
    Map<String, Object> getGroupStatistics();
}
