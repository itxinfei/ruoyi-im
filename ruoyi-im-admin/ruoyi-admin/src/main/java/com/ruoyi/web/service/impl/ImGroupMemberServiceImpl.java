package com.ruoyi.web.service.impl;

import com.ruoyi.web.domain.ImGroupLog;
import com.ruoyi.web.domain.ImGroupMember;
import com.ruoyi.web.mapper.ImGroupMemberMapper;
import com.ruoyi.web.service.ImGroupLogService;
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

    @Autowired(required = false)
    private ImGroupLogService imGroupLogService;

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
    public int insertImGroupMemberWithLog(ImGroupMember imGroupMember, Long operatorId, String operatorName) {
        int result = imGroupMemberMapper.insertImGroupMember(imGroupMember);
        if (result > 0 && imGroupMember.getUserId() != null && imGroupLogService != null) {
            imGroupLogService.logMemberOperation(
                imGroupMember.getGroupId(),
                operatorId,
                operatorName,
                "ADD_MEMBER",
                "添加成员到群组",
                imGroupMember.getUserId(),
                imGroupMember.getNickname()
            );
        }
        return result;
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
    public int updateMemberRoleWithLog(Long groupId, Long userId, String role, Long operatorId, String operatorName, String targetUserName) {
        Map<String, Object> params = new HashMap<>();
        params.put("groupId", groupId);
        params.put("userId", userId);
        params.put("role", role);
        int result = imGroupMemberMapper.updateMemberRole(params);
        if (result > 0 && imGroupLogService != null) {
            imGroupLogService.logMemberOperation(
                groupId,
                operatorId,
                operatorName,
                "CHANGE_ROLE",
                "修改成员角色为：" + role,
                userId,
                targetUserName
            );
        }
        return result;
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
    public int removeMemberWithLog(Long groupId, Long userId, Long operatorId, String operatorName, String targetUserName) {
        Map<String, Object> params = new HashMap<>();
        params.put("groupId", groupId);
        params.put("userId", userId);
        int result = imGroupMemberMapper.removeMember(params);
        if (result > 0 && imGroupLogService != null) {
            imGroupLogService.logMemberOperation(
                groupId,
                operatorId,
                operatorName,
                "REMOVE_MEMBER",
                "从群组移除成员",
                userId,
                targetUserName
            );
        }
        return result;
    }

    @Override
    public Map<String, Object> getMemberStatistics() {
        return imGroupMemberMapper.getMemberStatistics();
    }

    @Override
    public Map<String, Object> getGroupInfo(Long groupId) {
        return imGroupMemberMapper.getGroupInfo(groupId);
    }
}
