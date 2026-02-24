package com.ruoyi.im.service;

import com.ruoyi.im.dto.contact.ImFriendAddRequest;
import com.ruoyi.im.domain.ImFriendRequest;
import java.util.List;

/**
 * 好友申请服务接口
 */
public interface ImFriendRequestService {

    Long sendFriendRequest(ImFriendAddRequest request, Long userId);

    List<ImFriendRequest> getReceivedRequests(Long userId);

    List<ImFriendRequest> getSentRequests(Long userId);

    List<ImFriendRequest> getPendingRequests(Long userId);

    void processFriendRequest(Long requestId, Boolean approved, Long userId);
}
