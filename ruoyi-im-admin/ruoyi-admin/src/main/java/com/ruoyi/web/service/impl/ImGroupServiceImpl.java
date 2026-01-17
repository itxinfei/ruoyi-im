package com.ruoyi.web.service.impl;

import com.ruoyi.web.domain.ImGroup;
import com.ruoyi.web.domain.ImGroupMember;
import com.ruoyi.web.mapper.ImGroupMapper;
import com.ruoyi.web.service.ImGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * IM群组Service实现（Admin模块专用）
 */
@Service
public class ImGroupServiceImpl implements ImGroupService {

    @Autowired
    private ImGroupMapper groupMapper;

    @Override
    public List<ImGroup> selectImGroupList(ImGroup imGroup) {
        return groupMapper.selectImGroupList(imGroup);
    }

    @Override
    public ImGroup selectImGroupById(Long id) {
        return groupMapper.selectImGroupById(id);
    }

    @Override
    public int insertImGroup(ImGroup imGroup) {
        return groupMapper.insertImGroup(imGroup);
    }

    @Override
    public int updateImGroup(ImGroup imGroup) {
        return groupMapper.updateImGroup(imGroup);
    }

    @Override
    public int deleteImGroupById(Long id) {
        return groupMapper.deleteImGroupById(id);
    }

    @Override
    public int deleteImGroupByIds(Long[] ids) {
        return groupMapper.deleteImGroupByIds(ids);
    }

    @Override
    public List<ImGroupMember> selectGroupMembersByGroupId(Long groupId) {
        return groupMapper.selectGroupMembersByGroupId(groupId);
    }

    @Override
    public int addGroupMember(Long groupId, Long userId, String role, Long inviterId) {
        return groupMapper.addGroupMember(groupId, userId, role, inviterId);
    }

    @Override
    public int dismissGroup(Long groupId) {
        return groupMapper.dismissGroup(groupId);
    }

    /**
     * 获取群组统计数据
     *
     * @return 统计数据，包含totalCount、activeCount、mutedCount、largeCount
     */
    @Override
    public Map<String, Object> getGroupStatistics() {
        Map<String, Object> result = new HashMap<>();
        // 群组总数
        result.put("totalCount", groupMapper.countTotalGroups());
        // 活跃群组数（7天内有消息）
        result.put("activeCount", groupMapper.countActiveGroups());
        // 全员禁言群组数
        result.put("mutedCount", groupMapper.countMutedGroups());
        // 大群数（成员>100）
        result.put("largeCount", groupMapper.countLargeGroups());
        return result;
    }
}
