package com.ruoyi.web.service.impl;

import com.ruoyi.web.domain.ImConversation;
import com.ruoyi.web.mapper.ImConversationMapper;
import com.ruoyi.web.service.ImConversationService;
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

    @Autowired
    private ImConversationMapper imConversationMapper;

    @Override
    public ImConversation selectImConversationById(Long id) {
        return imConversationMapper.selectImConversationById(id);
    }

    @Override
    public List<ImConversation> selectImConversationList(ImConversation imConversation) {
        return imConversationMapper.selectImConversationList(imConversation);
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
        return imConversationMapper.getConversationStatistics();
    }

    @Override
    public List<ImConversation> getActiveConversations(Integer limit) {
        return imConversationMapper.getActiveConversations(limit);
    }
}
