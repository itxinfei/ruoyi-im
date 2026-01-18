package com.ruoyi.web.service;

import com.ruoyi.web.domain.ImDingMessage;

import java.util.List;
import java.util.Map;

/**
 * DING消息Service接口
 *
 * @author ruoyi
 * @date 2025-01-15
 */
public interface ImDingMessageService {

    /**
     * 查询DING消息列表
     */
    List<ImDingMessage> selectImDingMessageList(ImDingMessage imDingMessage);

    /**
     * 根据ID获取DING消息
     */
    ImDingMessage selectImDingMessageById(Long id);

    /**
     * 新增DING消息
     */
    int insertImDingMessage(ImDingMessage imDingMessage);

    /**
     * 修改DING消息
     */
    int updateImDingMessage(ImDingMessage imDingMessage);

    /**
     * 删除DING消息
     */
    int deleteImDingMessageByIds(Long[] ids);

    /**
     * 获取DING消息统计
     */
    Map<String, Object> getDingStatistics();

    /**
     * 获取DING消息回执详情
     */
    List<Map<String, Object>> getDingReceipts(Long dingId);

    /**
     * 发送DING消息
     */
    void sendDingMessage(ImDingMessage imDingMessage);

    /**
     * 定时发送DING消息
     */
    void scheduleDingMessage(ImDingMessage imDingMessage);

    /**
     * 取消定时发送
     */
    void cancelScheduledMessage(Long id);

    /**
     * 撤回DING消息
     */
    void revokeDingMessage(Long id);

    /**
     * 获取DING消息回执统计
     */
    Map<String, Object> getReceiptStatistics(Long dingId);

    /**
     * 提醒未读用户
     */
    void remindUnreadUsers(Long id);

    /**
     * 获取待发送的定时消息
     */
    List<ImDingMessage> getScheduledMessages();

    /**
     * 获取DING消息模板列表
     */
    List<Map<String, Object>> getDingTemplates();

    /**
     * 保存DING消息模板
     */
    void saveDingTemplate(Map<String, Object> template);

    /**
     * 删除DING消息模板
     */
    void deleteDingTemplate(Long id);

    /**
     * 使用模板创建DING消息
     */
    ImDingMessage createDingFromTemplate(Long templateId);
}
