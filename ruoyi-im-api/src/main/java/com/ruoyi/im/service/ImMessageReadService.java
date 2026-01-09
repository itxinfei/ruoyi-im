package com.ruoyi.im.service;

import com.ruoyi.im.vo.message.ImMessageReadStatusVO;
import com.ruoyi.im.vo.message.ImMessageReadDetailVO;

import java.util.List;

/**
 * 消息已读回执服务
 *
 * @author ruoyi
 */
public interface ImMessageReadService {

    /**
     * 标记消息为已读
     *
     * @param conversationId 会话ID
     * @param messageId      消息ID
     * @param userId         用户ID
     */
    void markMessageAsRead(Long conversationId, Long messageId, Long userId);

    /**
     * 批量标记消息为已读
     *
     * @param conversationId 会话ID
     * @param messageIds     消息ID列表
     * @param userId         用户ID
     */
    void markMessagesAsRead(Long conversationId, List<Long> messageIds, Long userId);

    /**
     * 标记会话中所有消息为已读（直到指定消息ID）
     *
     * @param conversationId 会话ID
     * @param upToMessageId  标记到此消息ID为止
     * @param userId         用户ID
     */
    void markConversationAsRead(Long conversationId, Long upToMessageId, Long userId);

    /**
     * 获取消息的已读状态
     *
     * @param messageId 消息ID
     * @param senderId  发送者ID（用于验证权限）
     * @return 已读状态信息
     */
    ImMessageReadStatusVO getMessageReadStatus(Long messageId, Long senderId);

    /**
     * 获取消息的已读详情（已读用户列表）
     *
     * @param messageId 消息ID
     * @param senderId  发送者ID（用于验证权限）
     * @return 已读详情
     */
    ImMessageReadDetailVO getMessageReadDetail(Long messageId, Long senderId);

    /**
     * 获取会话中消息的已读状态
     *
     * @param conversationId 会话ID
     * @param userId         当前用户ID
     * @return 消息已读状态列表
     */
    List<ImMessageReadStatusVO> getConversationMessageReadStatus(Long conversationId, Long userId);

    /**
     * 撤回已读回执（删除已读记录）
     *
     * @param messageId 消息ID
     * @param userId    用户ID
     */
    void revokeReadReceipt(Long messageId, Long userId);
}
