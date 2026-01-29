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

    /**
     * 获取用户的所有好友分组名称
     *
     * @param userId 用户ID
     * @return 分组名称列表
     */
    List<String> getGroupNames(Long userId);

    /**
     * 重命名好友分组
     * 将指定旧分组名称的所有好友更新为新分组名称
     *
     * @param userId 用户ID
     * @param oldName 旧分组名称
     * @param newName 新分组名称
     */
    void renameGroup(Long userId, String oldName, String newName);

    /**
     * 删除好友分组
     * 将指定分组名称的所有好友的分组名清空（移至默认分组）
     *
     * @param userId 用户ID
     * @param groupName 分组名称
     */
    void deleteGroup(Long userId, String groupName);

    /**
     * 批量移动好友到分组
     * 将指定的好友移动到指定分组
     *
     * @param userId 用户ID
     * @param friendIds 好友关系ID列表
     * @param groupName 目标分组名称，为空则移至默认分组
     */
    void moveFriendsToGroup(Long userId, List<Long> friendIds, String groupName);

    /**
     * 获取用户的所有标签
     * 获取当前用户使用过的所有好友标签
     *
     * @param userId 用户ID
     * @return 标签列表
     */
    List<String> getUserTags(Long userId);

    /**
     * 更新好友标签
     * 为指定好友设置标签
     *
     * @param friendId 好友关系ID（或好友用户ID）
     * @param userId 当前用户ID
     * @param tags 标签列表
     */
    void updateFriendTags(Long friendId, Long userId, List<String> tags);

    /**
     * 按标签获取好友
     * 获取具有指定标签的所有好友
     *
     * @param userId 用户ID
     * @param tag 标签名称
     * @return 好友列表
     */
    List<ImFriendVO> getFriendsByTag(Long userId, String tag);
}
