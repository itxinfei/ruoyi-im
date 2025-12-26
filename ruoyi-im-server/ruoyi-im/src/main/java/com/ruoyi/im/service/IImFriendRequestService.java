package com.ruoyi.im.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.im.domain.ImFriendRequest;

import java.util.List;

/**
 * 好友请求Service接口
 * 
 * @author ruoyi
 * @date 2024-01-01
 */
public interface IImFriendRequestService extends IService<ImFriendRequest> {

    /**
     * 发送好友请求
     * 
     * @param senderId 发送者用户ID
     * @param receiverId 接收者用户ID
     * @param message 请求消息
     * @return 是否成功
     */
    boolean sendFriendRequest(Long senderId, Long receiverId, String message);

    /**
     * 处理好友请求
     * 
     * @param requestId 请求ID
     * @param handlerId 处理人ID
     * @param status 处理状态（APPROVED/REJECTED）
     * @return 是否成功
     */
    boolean handleFriendRequest(Long requestId, Long handlerId, String status);

    /**
     * 批量处理好友请求
     * 
     * @param requestIds 请求ID列表
     * @param handlerId 处理人ID
     * @param status 处理状态
     * @return 处理数量
     */
    int handleFriendRequestBatch(List<Long> requestIds, Long handlerId, String status);

    /**
     * 查询用户收到的好友请求列表
     * 
     * @param receiverId 接收者用户ID
     * @return 好友请求列表
     */
    List<ImFriendRequest> selectReceivedRequests(Long receiverId);

    /**
     * 查询用户发送的好友请求列表
     * 
     * @param senderId 发送者用户ID
     * @return 好友请求列表
     */
    List<ImFriendRequest> selectSentRequests(Long senderId);

    /**
     * 查询待处理的好友请求
     * 
     * @param senderId 发送者用户ID
     * @param receiverId 接收者用户ID
     * @return 好友请求
     */
    ImFriendRequest getPendingRequest(Long senderId, Long receiverId);

    /**
     * 统计用户收到的待处理请求数量
     * 
     * @param receiverId 接收者用户ID
     * @return 待处理请求数量
     */
    int countPendingRequests(Long receiverId);

    /**
     * 撤回好友请求
     * 
     * @param requestId 请求ID
     * @param senderId 发送者用户ID
     * @return 是否成功
     */
    boolean withdrawFriendRequest(Long requestId, Long senderId);

    /**
     * 检查是否已发送好友请求
     * 
     * @param senderId 发送者用户ID
     * @param receiverId 接收者用户ID
     * @return 是否已发送
     */
    boolean hasPendingRequest(Long senderId, Long receiverId);

    /**
     * 清理过期的好友请求
     * 
     * @param days 过期天数
     * @return 清理数量
     */
    int cleanExpiredRequests(int days);

    /**
     * 获取好友请求详细信息
     * 
     * @param requestId 请求ID
     * @return 好友请求信息
     */
    ImFriendRequest getRequestInfo(Long requestId);

    /**
     * 删除用户的所有好友请求
     * 
     * @param userId 用户ID
     * @return 删除数量
     */
    int deleteUserAllRequests(Long userId);
}