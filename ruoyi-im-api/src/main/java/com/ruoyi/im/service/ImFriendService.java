package com.ruoyi.im.service;

import com.ruoyi.im.dto.contact.ImFriendAddRequest;
import com.ruoyi.im.dto.contact.ImFriendUpdateRequest;
import com.ruoyi.im.domain.ImFriendRequest;
import com.ruoyi.im.vo.contact.ImContactGroupVO;
import com.ruoyi.im.vo.contact.ImFriendVO;

import java.util.List;

/**
 * 好友服务接口
 *
 * @author ruoyi
 */
public interface ImFriendService {

    /**
     * 发送好友申请
     *
     * @param request 申请请求
     * @param userId 当前用户ID
     * @return 申请ID
     */
    Long sendFriendRequest(ImFriendAddRequest request, Long userId);

    /**
     * 获取收到的好友申请列表
     *
     * @param userId 用户ID
     * @return 申请列表
     */
    List<ImFriendRequest> getReceivedRequests(Long userId);

    /**
     * 获取发送的好友申请列表
     *
     * @param userId 用户ID
     * @return 申请列表
     */
    List<ImFriendRequest> getSentRequests(Long userId);

    /**
     * 处理好友申请
     *
     * @param requestId 申请ID
     * @param approved 是否同意
     * @param userId 当前用户ID
     */
    void handleFriendRequest(Long requestId, Boolean approved, Long userId);

    /**
     * 获取好友列表
     *
     * @param userId 用户ID
     * @return 好友列表
     */
    List<ImFriendVO> getFriendList(Long userId);

    /**
     * 获取分组好友列表
     *
     * @param userId 用户ID
     * @return 分组好友列表
     */
    List<ImContactGroupVO> getGroupedFriendList(Long userId);

    /**
     * 获取好友详情
     *
     * @param friendId 好友用户ID
     * @param userId 当前用户ID
     * @return 好友信息
     */
    ImFriendVO getFriendById(Long friendId, Long userId);

    /**
     * 更新好友信息
     *
     * @param friendId 好友用户ID
     * @param request 更新请求
     * @param userId 当前用户ID
     */
    void updateFriend(Long friendId, ImFriendUpdateRequest request, Long userId);

    /**
     * 删除好友
     *
     * @param friendId 好友用户ID
     * @param userId 当前用户ID
     */
    void deleteFriend(Long friendId, Long userId);

    /**
     * 拉黑/解除拉黑好友
     *
     * @param friendId 好友用户ID
     * @param blocked 是否拉黑
     * @param userId 当前用户ID
     */
    void blockFriend(Long friendId, Boolean blocked, Long userId);

    /**
     * 搜索用户
     *
     * @param keyword 搜索关键词（用户名或手机号）
     * @param userId 当前用户ID
     * @return 用户列表
     */
    List<com.ruoyi.im.vo.user.ImUserVO> searchUsers(String keyword, Long userId);
}
