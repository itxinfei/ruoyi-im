package com.ruoyi.web.service;

import com.ruoyi.web.domain.ImFriend;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 好友关系Service接口
 *
 * @author ruoyi
 */
public interface ImFriendService {

    /**
     * 查询好友关系
     *
     * @param id 好友关系主键
     * @return 好友关系
     */
    ImFriend selectImFriendById(Long id);

    /**
     * 查询好友关系列表
     *
     * @param imFriend 好友关系
     * @return 好友关系集合
     */
    List<ImFriend> selectImFriendList(ImFriend imFriend);

    /**
     * 根据用户ID查询好友列表
     *
     * @param userId 用户ID
     * @return 好友列表
     */
    List<ImFriend> selectFriendsByUserId(Long userId);

    /**
     * 按分组查询好友
     *
     * @param userId 用户ID
     * @return 分组后的好友Map
     */
    Map<String, List<ImFriend>> selectFriendsGrouped(Long userId);

    /**
     * 查询用户的好友分组列表
     *
     * @param userId 用户ID
     * @return 分组名称列表
     */
    List<String> selectFriendGroups(Long userId);

    /**
     * 新增好友关系
     *
     * @param imFriend 好友关系
     * @return 结果
     */
    int insertImFriend(ImFriend imFriend);

    /**
     * 修改好友关系
     *
     * @param imFriend 好友关系
     * @return 结果
     */
    int updateImFriend(ImFriend imFriend);

    /**
     * 批量删除好友关系
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    int deleteImFriendByIds(Long[] ids);

    /**
     * 删除指定好友关系
     *
     * @param userId 用户ID
     * @param friendId 好友ID
     * @return 结果
     */
    int deleteFriendByUserAndFriend(Long userId, Long friendId);

    /**
     * 查询两个用户之间是否存在好友关系
     *
     * @param userId1 用户1 ID
     * @param userId2 用户2 ID
     * @return 好友关系，不存在返回null
     */
    ImFriend selectImFriendByUsers(Long userId1, Long userId2);

    /**
     * 获取好友统计数据
     */
    Map<String, Object> getUserStatistics();

    /**
     * 获取待处理好友请求数量
     *
     * @return 待处理请求数量
     */
    int getPendingRequestCount();

    /**
     * 获取分组数量
     *
     * @return 分组数量
     */
    int getGroupCount();




    /**
     * 根据用户ID查询分组列表
     *
     * @param userId 用户ID
     * @return 分组列表
     */
    List<Map<String, Object>> getGroupList(Long userId);
}
