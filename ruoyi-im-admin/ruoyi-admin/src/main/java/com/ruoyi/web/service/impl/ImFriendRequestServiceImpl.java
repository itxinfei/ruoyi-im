package com.ruoyi.web.service.impl;

import com.ruoyi.web.domain.ImFriend;
import com.ruoyi.web.domain.ImFriendRequest;
import com.ruoyi.web.mapper.ImFriendRequestMapper;
import com.ruoyi.web.service.ImFriendRequestService;
import com.ruoyi.web.service.ImFriendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 好友申请Service实现
 *
 * @author ruoyi
 */
@Service
public class ImFriendRequestServiceImpl implements ImFriendRequestService {

    @Autowired
    private ImFriendRequestMapper imFriendRequestMapper;

    @Autowired
    private ImFriendService imFriendService;

    @Override
    public ImFriendRequest selectImFriendRequestById(Long id) {
        return imFriendRequestMapper.selectImFriendRequestById(id);
    }

    @Override
    public java.util.List<ImFriendRequest> selectImFriendRequestList(ImFriendRequest imFriendRequest) {
        return imFriendRequestMapper.selectImFriendRequestList(imFriendRequest);
    }

    @Override
    public int insertImFriendRequest(ImFriendRequest imFriendRequest) {
        return imFriendRequestMapper.insertImFriendRequest(imFriendRequest);
    }

    @Override
    public int updateImFriendRequest(ImFriendRequest imFriendRequest) {
        return imFriendRequestMapper.updateImFriendRequest(imFriendRequest);
    }

    @Override
    public int deleteImFriendRequestByIds(Long[] ids) {
        return imFriendRequestMapper.deleteImFriendRequestByIds(ids);
    }

    @Override
    public int deleteImFriendRequestById(Long id) {
        return imFriendRequestMapper.deleteImFriendRequestById(id);
    }

    /**
     * 处理好友申请（原方法，保持兼容）
     */
    @Override
    public int handleFriendRequest(Long id, boolean approved) {
        return handleFriendRequestAndCreateFriend(id, approved);
    }

    /**
     * 处理好友申请并自动建立好友关系
     *
     * @param requestId 申请ID
     * @param approved 是否同意
     * @return 结果
     */
    @Override
    @Transactional
    public int handleFriendRequestAndCreateFriend(Long requestId, boolean approved) {
        // 1. 更新申请状态
        ImFriendRequest request = imFriendRequestMapper.selectImFriendRequestById(requestId);
        if (request == null) {
            return 0;
        }
        request.setStatus(approved ? "APPROVED" : "REJECTED");
        request.setHandledTime(LocalDateTime.now());
        int result = imFriendRequestMapper.updateImFriendRequest(request);

        if (result > 0 && approved) {
            // 2. 批准时创建双向好友关系
            Long fromUserId = request.getFromUserId();
            Long toUserId = request.getToUserId();

            if (fromUserId != null && toUserId != null && !fromUserId.equals(toUserId)) {
                // 创建双向好友关系
                createBidirectionalFriend(fromUserId, toUserId);
            }
        }
        return result;
    }

    /**
     * 创建双向好友关系
     *
     * @param userId1 用户1 ID
     * @param userId2 用户2 ID
     */
    private void createBidirectionalFriend(Long userId1, Long userId2) {
        // 检查是否已经存在好友关系
        if (imFriendService.selectImFriendByUsers(userId1, userId2) == null) {
            ImFriend friend1 = new ImFriend();
            friend1.setUserId(userId1);
            friend1.setFriendId(userId2);
            friend1.setRemark("");
            friend1.setGroupName("好友");
            imFriendService.insertImFriend(friend1);
        }

        if (imFriendService.selectImFriendByUsers(userId2, userId1) == null) {
            ImFriend friend2 = new ImFriend();
            friend2.setUserId(userId2);
            friend2.setFriendId(userId1);
            friend2.setRemark("");
            friend2.setGroupName("好友");
            imFriendService.insertImFriend(friend2);
        }
    }

    @Override
    public Map<String, Object> getFriendRequestStatistics() {
        return imFriendRequestMapper.getFriendRequestStatistics();
    }
}
