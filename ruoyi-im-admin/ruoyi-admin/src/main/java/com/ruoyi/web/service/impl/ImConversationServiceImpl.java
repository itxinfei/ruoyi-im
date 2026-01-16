package com.ruoyi.web.service.impl;

import com.ruoyi.web.domain.ImConversation;
import com.ruoyi.web.domain.ImConversationMember;
import com.ruoyi.web.mapper.ImConversationMapper;
import com.ruoyi.web.mapper.ImConversationMemberMapper;
import com.ruoyi.web.service.ImConversationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 会话Service实现
 *
 * @author ruoyi
 */
@Service
public class ImConversationServiceImpl implements ImConversationService {

    private static final Logger logger = LoggerFactory.getLogger(ImConversationServiceImpl.class);

    @Autowired
    private ImConversationMapper imConversationMapper;

    @Autowired
    private ImConversationMemberMapper imConversationMemberMapper;

    @Override
    public ImConversation selectImConversationById(Long id) {
        return imConversationMapper.selectImConversationById(id);
    }

    @Override
    public List<ImConversation> selectImConversationList(ImConversation imConversation) {
        try {
            List<ImConversation> list = imConversationMapper.selectImConversationList(imConversation);
            if (list == null) {
                return new java.util.ArrayList<>();
            }
            for (ImConversation conversation : list) {
                if (conversation.getMemberCount() == null) {
                    conversation.setMemberCount(0);
                }
                if (conversation.getTotalUnreadCount() == null) {
                    conversation.setTotalUnreadCount(0);
                }
            }
            return list;
        } catch (Exception e) {
            logger.error("查询会话列表异常", e);
            return new java.util.ArrayList<>();
        }
    }

    @Override
    public int insertImConversation(ImConversation imConversation) {
        return imConversationMapper.insertImConversation(imConversation);
    }

    @Override
    public int updateImConversation(ImConversation imConversation) {
        return imConversationMapper.updateImConversation(imConversation);
    }

    @Override
    public int deleteImConversationByIds(Long[] ids) {
        return imConversationMapper.deleteImConversationByIds(ids);
    }

    @Override
    public Map<String, Object> getConversationStatistics() {
        try {
            Map<String, Object> result = imConversationMapper.getConversationStatistics();
            if (result == null) {
                result = new java.util.HashMap<>();
                result.put("totalCount", 0);
                result.put("privateCount", 0);
                result.put("groupCount", 0);
                result.put("activeCount", 0);
            }
            return result;
        } catch (Exception e) {
            Map<String, Object> errorResult = new java.util.HashMap<>();
            errorResult.put("totalCount", 0);
            errorResult.put("privateCount", 0);
            errorResult.put("groupCount", 0);
            errorResult.put("activeCount", 0);
            return errorResult;
        }
    }

    @Override
    public List<ImConversation> getActiveConversations(Integer limit) {
        return imConversationMapper.getActiveConversations(limit);
    }

    @Override
    public List<ImConversationMember> selectMembersByConversationId(Long conversationId, Long userId, String nickname, String role) {
        ImConversationMember queryParam = new ImConversationMember();
        queryParam.setConversationId(conversationId);
        if (userId != null) {
            queryParam.setUserId(userId);
        }
        if (nickname != null && !nickname.isEmpty()) {
            queryParam.setNickname(nickname);
        }
        if (role != null && !role.isEmpty()) {
            queryParam.setRole(role);
        }
        return imConversationMemberMapper.selectImConversationMemberList(queryParam);
    }
}
