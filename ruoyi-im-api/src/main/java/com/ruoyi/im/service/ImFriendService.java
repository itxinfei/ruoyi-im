package com.ruoyi.im.service;

import com.ruoyi.im.dto.contact.ImFriendAddRequest;
import com.ruoyi.im.dto.contact.ImFriendUpdateRequest;
import com.ruoyi.im.domain.ImFriendRequest;
import com.ruoyi.im.vo.contact.ImContactGroupVO;
import com.ruoyi.im.vo.contact.ImFriendVO;
import com.ruoyi.im.vo.user.ImUserVO;

import java.util.List;
import java.util.Map;

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
     * @param userId  当前用户 ID
     * @return 申请 ID
     */
    Long sendFriendRequest(ImFriendAddRequest request, Long userId);

    /**
     * 获取收到的好友申请列表
     *
     * @param userId 用户 ID
     * @return 申请列表
     */
    List<ImFriendRequest> getReceivedRequests(Long userId);

    /**
     * 获取发送的好友申请列表
     *
     * @param userId 用户 ID
     * @return 申请列表
     */
    List<ImFriendRequest> getSentRequests(Long userId);

    /**
     * 处理好友申请
     *
     * @param requestId 申请 ID
     * @param approved  是否同意
     * @param userId    当前用户 ID
     */
    void processFriendRequest(Long requestId, Boolean approved, Long userId);

    /**
     * 获取好友列表
     *
     * @param userId 用户 ID
     * @return 好友列表
     */
    List<ImFriendVO> getFriendList(Long userId);

    /**
     * 获取分组好友列表
     *
     * @param userId 用户 ID
     * @return 分组好友列表
     */
    List<ImContactGroupVO> getGroupedFriendList(Long userId);

    /**
     * 获取好友详情
     *
     * @param friendId 好友用户 ID
     * @param userId   当前用户 ID
     * @return 好友信息
     */
    ImFriendVO getFriendById(Long friendId, Long userId);

    /**
     * 更新好友信息
     *
     * @param friendId 好友用户 ID
     * @param request  更新请求
     * @param userId   当前用户 ID
     */
    void updateFriend(Long friendId, ImFriendUpdateRequest request, Long userId);

    /**
     * 删除好友
     *
     * @param friendId 好友用户 ID
     * @param userId   当前用户 ID
     */
    void deleteFriend(Long friendId, Long userId);

    /**
     * 设置好友备注
     *
     * @param friendId 好友用户 ID
     * @param userId   当前用户 ID
     * @param remark   备注
     */
    void updateFriendRemark(Long friendId, Long userId, String remark);

    /**
     * 检查两人是否是好友
     *
     * @param userId1 用户 ID1
     * @param userId2 用户 ID2
     * @return true=是好友
     */
    boolean isFriend(Long userId1, Long userId2);

    /**
     * 添加好友（内部方法）
     *
     * @param userId1 用户 ID1
     * @param userId2 用户 ID2
     */
    void addFriend(Long userId1, Long userId2);

    /**
     * 拉黑/解除拉黑好友
     *
     * @param friendId 好友用户 ID
     * @param blocked  是否拉黑
     * @param userId   当前用户 ID
     */
    void blockFriend(Long friendId, Boolean blocked, Long userId);

    /**
     * 搜索用户
     *
     * @param keyword 搜索关键词（用户名或手机号）
     * @param userId  当前用户 ID
     * @return 用户列表
     */
    List<com.ruoyi.im.vo.user.ImUserVO> searchUsers(String keyword, Long userId);

    /**
     * 获取用户的所有好友分组名称
     *
     * @param userId 用户 ID
     * @return 分组名称列表
     */
    List<String> getGroupNames(Long userId);

    /**
     * 添加好友到分组
     *
     * @param friendId 好友用户 ID
     * @param userId   当前用户 ID
     * @param groupName 分组名称
     */
    void addToGroup(Long friendId, Long userId, String groupName);

    /**
     * 从分组移除好友
     *
     * @param friendId  好友用户 ID
     * @param userId    当前用户 ID
     * @param groupName 分组名称
     */
    void removeFromGroup(Long friendId, Long userId, String groupName);

    /**
     * 重命名好友分组
     *
     * @param userId      用户 ID
     * @param oldName     旧分组名称
     * @param newName     新分组名称
     */
    void renameGroup(Long userId, String oldName, String newName);

    /**
     * 删除好友分组
     *
     * @param userId    用户 ID
     * @param groupName 分组名称
     */
    void deleteGroup(Long userId, String groupName);

    /**
     * 获取好友统计信息
     *
     * @param userId 用户 ID
     * @return 统计信息
     */
    Map<String, Object> getFriendStats(Long userId);

    /**
     * 移动好友到分组
     *
     * @param userId    用户 ID
     * @param friendIds 好友 ID 列表
     * @param groupName 分组名称
     */
    void moveFriendsToGroup(Long userId, List<Long> friendIds, String groupName);

    /**
     * 获取用户标签
     *
     * @param userId 用户 ID
     * @return 标签列表
     */
    List<String> getUserTags(Long userId);

    /**
     * 更新好友标签
     *
     * @param friendId 好友用户 ID
     * @param userId   当前用户 ID
     * @param tags     标签列表
     */
    void updateFriendTags(Long friendId, Long userId, List<String> tags);

    /**
     * 根据标签获取好友
     *
     * @param userId 用户 ID
     * @param tag    标签
     * @return 好友列表
     */
    List<ImFriendVO> getFriendsByTag(Long userId, String tag);

    /**
     * 批量发送好友申请
     *
     * @param userIds 用户 ID 列表
     * @param message 申请消息
     * @param fromUserId 申请人 ID
     * @return 申请结果 Map<用户 ID, 申请 ID>
     */
    Map<Long, String> batchSendFriendRequest(List<Long> userIds, String message, Long fromUserId);

    /**
     * 获取推荐好友
     *
     * @param userId 用户 ID
     * @param type   推荐类型
     * @param limit  数量限制
     * @return 推荐用户列表
     */
    List<ImUserVO> getRecommendedContacts(Long userId, String type, Integer limit);

    /**
     * 批量删除好友
     *
     * @param friendIds 好友 ID 列表
     * @param userId    当前用户 ID
     */
    void batchDeleteFriends(List<Long> friendIds, Long userId);

    /**
     * 匹配通讯录联系人
     *
     * @param userId    用户 ID
     * @param contacts  联系人列表
     * @return 匹配的用户列表
     */
    List<com.ruoyi.im.vo.user.ImUserVO> matchAddressBookContacts(Long userId, List<Map<String, String>> contacts);

    /**
     * 获取通讯录匹配结果
     *
     * @param userId 用户 ID
     * @return 用户列表
     */
    List<com.ruoyi.im.vo.user.ImUserVO> getAddressBookMatches(Long userId);

    /**
     * 清除好友列表缓存
     *
     * @param userId 用户 ID
     */
    void clearFriendListCache(Long userId);

    /**
     * 批量清除好友列表缓存
     *
     * @param userIds 用户 ID 列表
     */
    void batchClearFriendListCache(List<Long> userIds);
}
