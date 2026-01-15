package com.ruoyi.web.service.impl;

import com.ruoyi.web.domain.ImMessage;
import com.ruoyi.web.mapper.ImMessageMapper;
import com.ruoyi.web.service.ImMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;

/**
 * IM消息Service实现（Admin模块专用）
 */
@Service
public class ImMessageServiceImpl implements ImMessageService {

    @Autowired
    private ImMessageMapper messageMapper;

    @Override
    public List<ImMessage> selectImMessageList(ImMessage imMessage) {
        return messageMapper.selectImMessageList(imMessage);
    }

    @Override
    public List<ImMessage> selectImMessageListByConversationId(Long conversationId) {
        return messageMapper.selectImMessageListByConversationId(conversationId);
    }

    @Override
    public List<ImMessage> selectImMessageListByTimeRange(Long conversationId, String startTime, String endTime) {
        return messageMapper.selectImMessageListByTimeRange(conversationId, startTime, endTime);
    }

    @Override
    public ImMessage selectImMessageById(Long id) {
        return messageMapper.selectImMessageById(id);
    }

    @Override
    public int insertImMessage(ImMessage imMessage) {
        return messageMapper.insertImMessage(imMessage);
    }

    @Override
    public int updateImMessage(ImMessage imMessage) {
        return messageMapper.updateImMessage(imMessage);
    }

    @Override
    public int countMessages(ImMessage imMessage) {
        return messageMapper.countMessages(imMessage);
    }

    @Override
    public int countSensitiveMessages() {
        return messageMapper.countSensitiveMessages();
    }

    @Override
    public int deleteImMessageById(Long id) {
        return messageMapper.deleteImMessageById(id);
    }

    @Override
    public int deleteImMessageByIds(Long[] ids) {
        return messageMapper.deleteImMessageByIds(ids);
    }

    @Override
    public int revokeMessage(Long messageId) {
        return messageMapper.revokeMessage(messageId);
    }

    @Override
    public Map<String, Object> getMessageStatistics() {
        return messageMapper.getMessageStatistics();
    }
}
