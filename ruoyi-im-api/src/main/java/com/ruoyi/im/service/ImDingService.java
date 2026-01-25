package com.ruoyi.im.service;

import com.ruoyi.im.dto.ding.DingQueryRequest;
import com.ruoyi.im.dto.ding.DingSendRequest;
import com.ruoyi.im.vo.ding.DingMessageVO;

import java.util.List;

/**
 * DING消息服务接口
 *
 * @author ruoyi
 */
public interface ImDingService {

    /**
     * 发送DING消息
     *
     * @param request DING发送请求
     * @param userId  当前用户ID
     * @return DING消息VO
     */
    DingMessageVO sendDing(DingSendRequest request, Long userId);

    /**
     * 查询DING消息列表
     *
     * @param request 查询请求
     * @param userId  当前用户ID
     * @return DING消息列表
     */
    List<DingMessageVO> queryDings(DingQueryRequest request, Long userId);

    /**
     * 获取DING消息详情
     *
     * @param dingId DING消息ID
     * @param userId  当前用户ID
     * @return DING消息VO
     */
    DingMessageVO getDingDetail(Long dingId, Long userId);

    /**
     * 标记DING消息为已读
     *
     * @param dingId DING消息ID
     * @param userId  当前用户ID
     */
    void markAsRead(Long dingId, Long userId);

    /**
     * 取消DING消息
     *
     * @param dingId DING消息ID
     * @param userId  当前用户ID
     */
    void cancelDing(Long dingId, Long userId);

    /**
     * 获取用户未读DING消息数量
     *
     * @param userId 用户ID
     * @return 未读数量
     */
    int getUnreadCount(Long userId);

    /**
     * 获取DING消息已读状态
     *
     * @param dingId DING消息ID
     * @param userId 当前用户ID
     * @return 已读用户ID列表
     */
    List<Long> getReadUserIds(Long dingId, Long userId);

    /**
     * 处理过期的DING消息
     * 定时任务调用，将过期的DING消息状态更新为EXPIRED
     */
    void processExpiredDings();
}
