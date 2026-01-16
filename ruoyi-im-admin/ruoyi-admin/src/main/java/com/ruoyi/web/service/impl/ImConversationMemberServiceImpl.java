package com.ruoyi.web.service.impl;

import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.web.domain.ImConversationMember;
import com.ruoyi.web.mapper.ImConversationMemberMapper;
import com.ruoyi.web.service.ImConversationMemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 会话成员Service实现
 *
 * @author ruoyi
 */
@Service
public class ImConversationMemberServiceImpl implements ImConversationMemberService {

    @Autowired
    private ImConversationMemberMapper imConversationMemberMapper;

    @Override
    public ImConversationMember selectImConversationMemberById(Long id) {
        return imConversationMemberMapper.selectImConversationMemberById(id);
    }

    @Override
    public List<ImConversationMember> selectImConversationMemberList(ImConversationMember imConversationMember) {
        return imConversationMemberMapper.selectImConversationMemberList(imConversationMember);
    }

    @Override
    public List<ImConversationMember> selectMembersByConversationId(Long conversationId) {
        return imConversationMemberMapper.selectMembersByConversationId(conversationId);
    }

    @Override
    public Integer countMembersByConversationId(Long conversationId) {
        return imConversationMemberMapper.countMembersByConversationId(conversationId);
    }

    @Override
    public int insertImConversationMember(ImConversationMember imConversationMember) {
        return imConversationMemberMapper.insertImConversationMember(imConversationMember);
    }

    @Override
    public int updateImConversationMember(ImConversationMember imConversationMember) {
        return imConversationMemberMapper.updateImConversationMember(imConversationMember);
    }

    @Override
    public int deleteImConversationMemberByIds(Long[] ids) {
        return imConversationMemberMapper.deleteImConversationMemberByIds(ids);
    }

    @Override
    public int removeMember(Long conversationId, Long userId) {
        Map<String, Object> params = new HashMap<>();
        params.put("conversationId", conversationId);
        params.put("userId", userId);
        return imConversationMemberMapper.removeMember(params);
    }

    @Override
    public AjaxResult getStatistics() {
        Map<String, Object> data = imConversationMemberMapper.getStatistics();
        return AjaxResult.success(data);
    }
}
