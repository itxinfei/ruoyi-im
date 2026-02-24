package com.ruoyi.im.service.impl;

import com.ruoyi.im.constant.StatusConstants;
import com.ruoyi.im.domain.ImFriendRequest;
import com.ruoyi.im.dto.contact.ImFriendAddRequest;
import com.ruoyi.im.exception.BusinessException;
import com.ruoyi.im.mapper.ImFriendRequestMapper;
import com.ruoyi.im.service.ImFriendRequestService;
import com.ruoyi.im.service.ImFriendService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 好友申请服务实现
 */
@Service
public class ImFriendRequestServiceImpl implements ImFriendRequestService {

    private final ImFriendRequestMapper requestMapper;
    private final ImFriendService friendService;

    public ImFriendRequestServiceImpl(ImFriendRequestMapper requestMapper, ImFriendService friendService) {
        this.requestMapper = requestMapper;
        this.friendService = friendService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long sendFriendRequest(ImFriendAddRequest request, Long userId) {
        if (userId.equals(request.getTargetUserId())) throw new BusinessException("不能添加自己");
        if (friendService.isFriend(userId, request.getTargetUserId())) throw new BusinessException("已经是好友");

        ImFriendRequest req = new ImFriendRequest();
        req.setFromUserId(userId);
        req.setToUserId(request.getTargetUserId());
        req.setMessage(request.getMessage());
        req.setStatus(StatusConstants.FriendRequest.PENDING);
        req.setCreateTime(LocalDateTime.now());
        requestMapper.insert(req);
        return req.getId();
    }

    @Override
    public List<ImFriendRequest> getReceivedRequests(Long userId) {
        return requestMapper.selectImFriendRequestListByToUserId(userId);
    }

    @Override
    public List<ImFriendRequest> getSentRequests(Long userId) {
        return requestMapper.selectImFriendRequestListByFromUserId(userId);
    }

    @Override
    public List<ImFriendRequest> getPendingRequests(Long userId) {
        // 获取收到的待处理申请
        return requestMapper.selectImFriendRequestListByToUserIdAndStatus(userId, "PENDING");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void processFriendRequest(Long requestId, Boolean approved, Long userId) {
        ImFriendRequest req = requestMapper.selectById(requestId);
        if (req == null || !req.getToUserId().equals(userId)) throw new BusinessException("请求不存在");

        req.setStatus(approved ? StatusConstants.FriendRequest.ACCEPTED : StatusConstants.FriendRequest.REJECTED);
        req.setHandledTime(LocalDateTime.now());
        requestMapper.updateById(req);

        if (approved) {
            friendService.addFriend(req.getFromUserId(), req.getToUserId());
            friendService.addFriend(req.getToUserId(), req.getFromUserId());
        }
    }
}
