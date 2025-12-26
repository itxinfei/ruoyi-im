package com.ruoyi.im.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.im.domain.ImFriendRequest;
import com.ruoyi.im.mapper.ImFriendRequestMapper;
import com.ruoyi.im.service.IImFriendRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 好友请求Service业务层处理
 * 
 * @author ruoyi
 * @date 2024-01-01
 */
@Service
public class ImFriendRequestServiceImpl extends ServiceImpl<ImFriendRequestMapper, ImFriendRequest> implements IImFriendRequestService {
    
    @Autowired
    private ImFriendRequestMapper imFriendRequestMapper;

    /**
     * 发送好友请求
     * 
     * @param senderId 发送者用户ID
     * @param receiverId 接收者用户ID
     * @param message 请求消息
     * @return 是否成功
     */
    @Override
    public boolean sendFriendRequest(Long senderId, Long receiverId, String message) {
        // 检查是否已存在待处理的请求
        ImFriendRequest existingRequest = imFriendRequestMapper.selectPendingRequest(senderId, receiverId);
        if (existingRequest != null) {
            return false; // 已存在待处理请求
        }
        
        ImFriendRequest request = new ImFriendRequest();
        request.setFromUserId(senderId);
        request.setToUserId(receiverId);
        request.setMessage(message);
        request.setStatus("PENDING");
        request.setCreateTime(new Date());
        
        return imFriendRequestMapper.insert(request) > 0;
    }

    /**
     * 处理好友请求
     * 
     * @param requestId 请求ID
     * @param handlerId 处理人ID
     * @param status 处理状态（APPROVED/REJECTED）
     * @return 是否成功
     */
    @Override
    public boolean handleFriendRequest(Long requestId, Long handlerId, String status) {
        ImFriendRequest request = imFriendRequestMapper.selectById(requestId);
        if (request == null || !"PENDING".equals(request.getStatus())) {
            return false;
        }
        
        request.setStatus(status);
        request.setHandledTime(new Date());
        request.setUpdateTime(new Date());
        
        return imFriendRequestMapper.updateById(request) > 0;
    }

    /**
     * 批量处理好友请求
     * 
     * @param requestIds 请求ID列表
     * @param handlerId 处理人ID
     * @param status 处理状态
     * @return 处理数量
     */
    @Override
    public int handleFriendRequestBatch(List<Long> requestIds, Long handlerId, String status) {
        return imFriendRequestMapper.updateStatusBatch(requestIds, status, handlerId);
    }

    /**
     * 查询用户收到的好友请求列表
     * 
     * @param receiverId 接收者用户ID
     * @return 好友请求列表
     */
    @Override
    public List<ImFriendRequest> selectReceivedRequests(Long receiverId) {
        return imFriendRequestMapper.selectReceivedRequestsWithDetails(receiverId);
    }

    /**
     * 查询用户发送的好友请求列表
     * 
     * @param senderId 发送者用户ID
     * @return 好友请求列表
     */
    @Override
    public List<ImFriendRequest> selectSentRequests(Long senderId) {
        return imFriendRequestMapper.selectSentRequestsWithDetails(senderId);
    }

    /**
     * 查询待处理的好友请求
     * 
     * @param senderId 发送者用户ID
     * @param receiverId 接收者用户ID
     * @return 好友请求
     */
    @Override
    public ImFriendRequest getPendingRequest(Long senderId, Long receiverId) {
        return imFriendRequestMapper.selectPendingRequest(senderId, receiverId);
    }

    /**
     * 统计用户收到的待处理请求数量
     * 
     * @param receiverId 接收者用户ID
     * @return 待处理请求数量
     */
    @Override
    public int countPendingRequests(Long receiverId) {
        return imFriendRequestMapper.countPendingRequests(receiverId);
    }

    /**
     * 撤回好友请求
     * 
     * @param requestId 请求ID
     * @param senderId 发送者用户ID
     * @return 是否成功
     */
    @Override
    public boolean withdrawFriendRequest(Long requestId, Long senderId) {
        ImFriendRequest request = imFriendRequestMapper.selectById(requestId);
        if (request == null || !request.getFromUserId().equals(senderId) || !"PENDING".equals(request.getStatus())) {
            return false;
        }
        
        return imFriendRequestMapper.deleteById(requestId) > 0;
    }

    /**
     * 检查是否已发送好友请求
     * 
     * @param senderId 发送者用户ID
     * @param receiverId 接收者用户ID
     * @return 是否已发送
     */
    @Override
    public boolean hasPendingRequest(Long senderId, Long receiverId) {
        ImFriendRequest request = imFriendRequestMapper.selectPendingRequest(senderId, receiverId);
        return request != null;
    }

    /**
     * 清理过期的好友请求
     * 
     * @param days 过期天数
     * @return 清理数量
     */
    @Override
    public int cleanExpiredRequests(int days) {
        return imFriendRequestMapper.deleteExpiredRequests(days);
    }

    /**
     * 获取请求详情
     * 
     * @param requestId 请求ID
     * @return 请求详情
     */
    @Override
    public ImFriendRequest getRequestInfo(Long requestId) {
        return imFriendRequestMapper.selectById(requestId);
    }

    /**
     * 删除用户所有相关请求
     * 
     * @param userId 用户ID
     * @return 删除数量
     */
    @Override
    public int deleteUserAllRequests(Long userId) {
        QueryWrapper<ImFriendRequest> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("from_user_id", userId).or().eq("to_user_id", userId);
        return imFriendRequestMapper.delete(queryWrapper);
    }
}