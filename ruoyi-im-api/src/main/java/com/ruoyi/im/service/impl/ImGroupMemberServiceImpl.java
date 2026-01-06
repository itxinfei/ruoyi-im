package com.ruoyi.im.service.impl;

import com.ruoyi.im.domain.ImGroupMember;
import com.ruoyi.im.mapper.ImGroupMemberMapper;
import com.ruoyi.im.service.ImGroupMemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Group member service implementation
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
    public int insertImGroupMember(ImGroupMember imGroupMember) {
        return imGroupMemberMapper.insertImGroupMember(imGroupMember);
    }

    @Override
    public int updateImGroupMember(ImGroupMember imGroupMember) {
        return imGroupMemberMapper.updateImGroupMember(imGroupMember);
    }

    @Override
    public int deleteImGroupMemberById(Long id) {
        return imGroupMemberMapper.deleteImGroupMemberById(id);
    }

    @Override
    public List<ImGroupMember> selectImGroupMemberListByGroupId(Long groupId) {
        return imGroupMemberMapper.selectImGroupMemberListByGroupId(groupId);
    }

    @Override
    public ImGroupMember selectImGroupMemberByGroupIdAndUserId(Long groupId, Long userId) {
        return imGroupMemberMapper.selectImGroupMemberByGroupIdAndUserId(groupId, userId);
    }
}
