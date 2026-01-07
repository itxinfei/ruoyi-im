package com.ruoyi.web.service.impl;

import com.ruoyi.im.domain.ImGroup;
import com.ruoyi.im.domain.ImGroupMember;
import com.ruoyi.web.mapper.ImGroupMapper;
import com.ruoyi.web.service.ImGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

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
}
