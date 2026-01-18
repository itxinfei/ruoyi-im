package com.ruoyi.web.service.impl;

import com.ruoyi.web.domain.ImMessageMention;
import com.ruoyi.web.mapper.ImMessageMentionMapper;
import com.ruoyi.web.service.ImMessageMentionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 消息@记录Service实现
 *
 * @author ruoyi
 * @date 2025-01-18
 */
@Service
public class ImMessageMentionServiceImpl implements ImMessageMentionService {

    @Autowired
    private ImMessageMentionMapper mentionMapper;

    /**
     * 查询消息@记录列表
     *
     * @param imMessageMention 查询条件
     * @return 消息@记录列表
     */
    @Override
    public List<ImMessageMention> selectImMessageMentionList(ImMessageMention imMessageMention) {
        return mentionMapper.selectImMessageMentionList(imMessageMention);
    }

    /**
     * 根据ID查询消息@记录
     *
     * @param id 主键ID
     * @return 消息@记录
     */
    @Override
    public ImMessageMention selectImMessageMentionById(Long id) {
        return mentionMapper.selectImMessageMentionById(id);
    }

    /**
     * 根据消息ID查询@记录列表
     *
     * @param messageId 消息ID
     * @return 消息@记录列表
     */
    @Override
    public List<ImMessageMention> selectImMessageMentionByMessageId(Long messageId) {
        return mentionMapper.selectImMessageMentionByMessageId(messageId);
    }

    /**
     * 获取@统计数据
     *
     * @return 统计数据
     */
    @Override
    public Map<String, Object> getMentionStatistics() {
        return mentionMapper.getMentionStatistics();
    }
}
