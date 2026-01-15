package com.ruoyi.web.service.impl;

import com.ruoyi.web.domain.ImDingMessage;
import com.ruoyi.web.mapper.ImDingMessageMapper;
import com.ruoyi.web.service.ImDingMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * DING消息Service实现
 *
 * @author ruoyi
 * @date 2025-01-15
 */
@Service
public class ImDingMessageServiceImpl implements ImDingMessageService {

    @Autowired
    private ImDingMessageMapper dingMessageMapper;

    @Override
    public List<ImDingMessage> selectImDingMessageList(ImDingMessage imDingMessage) {
        return dingMessageMapper.selectImDingMessageList(imDingMessage);
    }

    @Override
    public ImDingMessage selectImDingMessageById(Long id) {
        return dingMessageMapper.selectImDingMessageById(id);
    }

    @Override
    public int insertImDingMessage(ImDingMessage imDingMessage) {
        return dingMessageMapper.insertImDingMessage(imDingMessage);
    }

    @Override
    public int updateImDingMessage(ImDingMessage imDingMessage) {
        return dingMessageMapper.updateImDingMessage(imDingMessage);
    }

    @Override
    public int deleteImDingMessageByIds(Long[] ids) {
        return dingMessageMapper.deleteImDingMessageByIds(ids);
    }

    @Override
    public Map<String, Object> getDingStatistics() {
        return dingMessageMapper.getDingStatistics();
    }

    @Override
    public List<Map<String, Object>> getDingReceipts(Long dingId) {
        return dingMessageMapper.getDingReceipts(dingId);
    }
}
