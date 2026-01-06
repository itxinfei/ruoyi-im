package com.ruoyi.im.service;

import com.ruoyi.im.dto.message.ImMessageSendRequest;
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
     * @param sessionId 会话ID
     * @param userId 当前用户ID
     * @param lastId 最后一条消息ID（分页用）
     * @param limit 每页条数
     * @return 消息列表
     */
    List<ImMessageVO> getMessages(Long sessionId, Long userId, Long lastId, Integer limit);

    /**
     * 撤回消息
     *
     * @param messageId 消息ID
     * @param userId 当前用户ID
     */
    void recallMessage(Long messageId, Long userId);

    /**
     * 标记消息已读
     *
     * @param sessionId 会话ID
     * @param userId 当前用户ID
     * @param messageIds 消息ID列表
     */
    void markAsRead(Long sessionId, Long userId, List<Long> messageIds);
}
