package com.ruoyi.im.service.impl;

import com.ruoyi.im.domain.ImMessageEditHistory;
import com.ruoyi.im.mapper.ImMessageEditHistoryMapper;
import com.ruoyi.im.service.ImMessageEditHistoryService;
import com.ruoyi.im.vo.message.MessageEditHistoryVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 消息编辑历史服务实现
 *
 * @author ruoyi
 */
@Service
public class ImMessageEditHistoryServiceImpl implements ImMessageEditHistoryService {

    private static final Logger log = LoggerFactory.getLogger(ImMessageEditHistoryServiceImpl.class);

    @Autowired
    private ImMessageEditHistoryMapper editHistoryMapper;

    /**
     * 保存编辑历史
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveEditHistory(ImMessageEditHistory history) {
        try {
            history.setEditTime(LocalDateTime.now());
            history.setCreateTime(LocalDateTime.now());
            editHistoryMapper.insert(history);
            log.info("保存消息编辑历史成功：messageId={}, editorId={}",
                    history.getMessageId(), history.getEditorId());
        } catch (Exception e) {
            log.error("保存消息编辑历史失败：messageId={}", history.getMessageId(), e);
            throw e;
        }
    }

    /**
     * 查询消息的编辑历史列表
     */
    @Override
    public List<MessageEditHistoryVO> getEditHistory(Long messageId) {
        try {
            List<ImMessageEditHistory> historyList = editHistoryMapper.selectByMessageId(messageId);
            return historyList.stream().map(history -> {
                MessageEditHistoryVO vo = new MessageEditHistoryVO();
                BeanUtils.copyProperties(history, vo);
                return vo;
            }).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("查询消息编辑历史失败：messageId={}", messageId, e);
            return null;
        }
    }

    /**
     * 统计消息的编辑次数
     */
    @Override
    public Integer countEditTimes(Long messageId) {
        try {
            return editHistoryMapper.countByMessageId(messageId);
        } catch (Exception e) {
            log.error("统计编辑次数失败：messageId={}", messageId, e);
            return 0;
        }
    }
}
