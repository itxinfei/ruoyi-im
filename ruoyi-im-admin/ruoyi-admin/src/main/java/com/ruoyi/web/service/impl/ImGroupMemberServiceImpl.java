package com.ruoyi.web.service.impl;

import com.ruoyi.web.domain.ImGroupMember;
import com.ruoyi.web.mapper.ImGroupMemberMapper;
import com.ruoyi.web.service.ImGroupMemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 群组成员Service实现
 *
 * @author ruoyi
 */
@Service
public class ImGroupMemberServiceImpl implements ImGroupMemberService {

    @Autowired
    private ImGroupMemberMapper imGroupMemberMapper;

    @Override
    public ImGroupMember selectImGroupMemberById(Long id) {
        return imGroupMemberMapper.selectImGroupMemberById(id);
    }

    @Override
    public List<ImGroupMember> selectImGroupMemberList(ImGroupMember imGroupMember) {
        return imGroupMemberMapper.selectImGroupMemberList(imGroupMember);
    }

    @Override
    public List<ImGroupMember> selectMembersByGroupId(Long groupId) {
        return imGroupMemberMapper.selectMembersByGroupId(groupId);
    }

    @Override
    public Integer countMembersByGroupId(Long groupId) {
        return imGroupMemberMapper.countMembersByGroupId(groupId);
    }

    @Override
    public List<ImGroupMember> selectUserGroups(Long userId) {
        return imGroupMemberMapper.selectUserGroups(userId);
    }

    @Override
    public int insertImGroupMember(ImGroupMember imGroupMember) {
        return imGroupMemberMapper.insertImGroupMember(imGroupMember);
    }

    @Override
    public int updateImGroupMember(ImGroupMember imGroupMember) {
        return imGroupMemberMapper.updateImGroupMember(imGroupMember);
    }

    @Override
    public int updateMemberRole(Long groupId, Long userId, String role) {
        Map<String, Object> params = new HashMap<>();
        params.put("groupId", groupId);
        params.put("userId", userId);
        params.put("role", role);
        return imGroupMemberMapper.updateMemberRole(params);
    }

    @Override
    public int deleteImGroupMemberByIds(Long[] ids) {
        return imGroupMemberMapper.deleteImGroupMemberByIds(ids);
    }

    @Override
    public int removeMember(Long groupId, Long userId) {
        Map<String, Object> params = new HashMap<>();
        params.put("groupId", groupId);
        params.put("userId", userId);
        return imGroupMemberMapper.removeMember(params);
    }

    @Override
    public Map<String, Object> getMemberStatistics() {
        return imGroupMemberMapper.getMemberStatistics();
    }
}
