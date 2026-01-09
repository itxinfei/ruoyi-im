package com.ruoyi.im.service;

import com.ruoyi.im.domain.ImMessageMention;
import com.ruoyi.im.dto.mention.ImMentionInfo;

import java.util.List;
import java.util.Map;

/**
 * 消息@提及服务接口
 *
 * @author ruoyi
 */
public interface ImMessageMentionService {

    /**
     * 创建消息@提及记录
     * 发送消息时解析@提及信息并保存
     *
     * @param messageId 消息ID
     * @param mentionInfo @提及信息
     * @param senderId 发送者ID
     */
    void createMentions(Long messageId, ImMentionInfo mentionInfo, Long senderId);

    /**
     * 获取用户未读的@提及列表
     *
     * @param userId 用户ID
     * @return 未读@提及列表
     */
    List<ImMessageMention> getUnreadMentions(Long userId);

    /**
     * 获取用户未读的@提及数量
     *
     * @param userId 用户ID
     * @return 未读数量
     */
    int getUnreadMentionCount(Long userId);

    /**
     * 标记@提及为已读
     *
     * @param messageId 消息ID
     * @param userId 用户ID
     */
    void markAsRead(Long messageId, Long userId);

    /**
     * 批量标记@提及为已读
     *
     * @param mentionIds 提及ID列表
     */
    void batchMarkAsRead(List<Long> mentionIds);

    /**
     * 解析消息内容中的@提及
     * 从消息内容中解析出@的用户和@所有人信息
     *
     * @param content 消息内容
     * @return @提及信息
     */
    ImMentionInfo parseMentions(String content);

    /**
     * 获取会话中可以@的用户列表
     *
     * @param conversationId 会话ID
     * @param keyword 搜索关键词
     * @return 可@的用户列表
     */
    List<Map<String, Object>> getMentionableUsers(Long conversationId, String keyword);

    /**
     * 检查用户是否可以@所有人
     *
     * @param conversationId 会话ID
     * @param userId 用户ID
     * @return 是否可以@所有人
     */
    boolean canMentionAll(Long conversationId, Long userId);
}
