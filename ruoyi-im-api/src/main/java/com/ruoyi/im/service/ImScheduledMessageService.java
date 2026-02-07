package com.ruoyi.im.service;

import com.ruoyi.im.domain.ImScheduledMessage;
import com.ruoyi.im.dto.message.ScheduledMessageCreateRequest;
import com.ruoyi.im.vo.message.ScheduledMessageVO;

import java.util.List;

/**
 * 定时消息服务接口
 *
 * @author ruoyi
 */
public interface ImScheduledMessageService {

    /**
     * 创建定时消息
     *
     * @param request 创建请求
     * @param userId 用户ID
     * @return 定时消息VO
     */
    ScheduledMessageVO createScheduledMessage(ScheduledMessageCreateRequest request, Long userId);

    /**
     * 取消定时消息
     *
     * @param id 消息ID
     * @param userId 用户ID
     */
    void cancelScheduledMessage(Long id, Long userId);

    /**
     * 获取用户的定时消息列表
     *
     * @param userId 用户ID
     * @return 消息列表
     */
    List<ScheduledMessageVO> getUserScheduledMessages(Long userId);

    /**
     * 处理到期的定时消息
     * 由定时任务调用
     */
    void processScheduledMessages();

    /**
     * 删除已发送的定时消息记录
     *
     * @param id 消息ID
     * @param userId 用户ID
     */
    void deleteScheduledMessage(Long id, Long userId);
}
