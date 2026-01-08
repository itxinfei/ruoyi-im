package com.ruoyi.im.service;

import com.ruoyi.im.dto.message.ImMessageSendRequest;
import com.ruoyi.im.vo.message.ImMessageSearchResultVO;
import com.ruoyi.im.vo.message.ImMessageVO;

import java.util.List;

/**
 * 消息服务接口
 *
 * @author ruoyi
 */
public interface ImMessageService {

    /**
     * 发送消息
     *
     * @param request 发送请求
     * @param userId 当前用户ID
     * @return 消息ID
     */
    Long sendMessage(ImMessageSendRequest request, Long userId);

    /**
     * 获取会话消息列表
     *
     * @param conversationId 会话ID
     * @param userId 当前用户ID
     * @param lastId 最后一条消息ID（分页用）
     * @param limit 每页条数
     * @return 消息列表
     */
    List<ImMessageVO> getMessages(Long conversationId, Long userId, Long lastId, Integer limit);

    /**
     * 撤回消息
     *
     * @param messageId 消息ID
     * @param userId 当前用户ID
     */
    void recallMessage(Long messageId, Long userId);

    /**
     * 编辑消息
     *
     * @param messageId 消息ID
     * @param newContent 新内容
     * @param userId 当前用户ID
     */
    void editMessage(Long messageId, String newContent, Long userId);

    /**
     * 转发消息
     *
     * @param messageId 消息ID
     * @param toConversationId 目标会话ID（为空表示转发到原会话）
     * @param toUserId 目标用户ID（为空表示转发给原发送者）
     * @param content 转发时附加内容
     * @param userId 当前用户ID
     * @return 新消息ID
     */
    Long forwardMessage(Long messageId, Long toConversationId, Long toUserId, String content, Long userId);

    /**
     * 引用/回复消息
     *
     * @param messageId 原消息ID
     * @param content 引用内容
     * @param userId 当前用户ID
     * @return 引用消息ID
     */
    Long replyMessage(Long messageId, String content, Long userId);

    /**
     * 标记消息已读
     *
     * @param conversationId 会话ID
     * @param userId 当前用户ID
     * @param messageIds 消息ID列表
     */
    void markAsRead(Long conversationId, Long userId, List<Long> messageIds);

    /**
     * 搜索消息
     * 支持关键词搜索、时间范围筛选、消息类型筛选等
     *
     * @param conversationId 会话ID（可选，为空则搜索所有会话）
     * @param keyword 搜索关键词（可选）
     * @param messageType 消息类型（可选）
     * @param senderId 发送者ID（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @param includeRevoked 是否包含撤回的消息
     * @param exactMatch 是否精确匹配
     * @param userId 当前用户ID
     * @return 搜索结果
     */
    ImMessageSearchResultVO searchMessages(Long conversationId, String keyword, String messageType,
                                           Long senderId, java.time.LocalDateTime startTime,
                                           java.time.LocalDateTime endTime, Integer pageNum, Integer pageSize,
                                           Boolean includeRevoked, Boolean exactMatch, Long userId);
}
