package com.ruoyi.web.service;

import com.ruoyi.web.domain.ImMessageMention;

import java.util.List;
import java.util.Map;

/**
 * 消息@记录Service接口
 *
 * @author ruoyi
 * @date 2025-01-18
 */
public interface ImMessageMentionService {

    /**
     * 查询消息@记录列表
     *
     * @param imMessageMention 查询条件
     * @return 消息@记录列表
     */
    List<ImMessageMention> selectImMessageMentionList(ImMessageMention imMessageMention);

    /**
     * 根据ID查询消息@记录
     *
     * @param id 主键ID
     * @return 消息@记录
     */
    ImMessageMention selectImMessageMentionById(Long id);

    /**
     * 根据消息ID查询@记录列表
     *
     * @param messageId 消息ID
     * @return 消息@记录列表
     */
    List<ImMessageMention> selectImMessageMentionByMessageId(Long messageId);

    /**
     * 获取@统计数据
     *
     * @return 统计数据
     */
    Map<String, Object> getMentionStatistics();
}
