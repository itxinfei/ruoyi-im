package com.ruoyi.im.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.im.domain.ImFriend;

import java.util.List;

/**
 * 好友关系Service接口
 * 
 * @author ruoyi
 * @date 2024-01-01
 */
public interface IImFriendService extends IService<ImFriend> {

    /**
     * 查询用户的好友列表
     * 
     * @param userId 用户ID
     * @return 好友列表
     */
    List<ImFriend> selectFriendList(Long userId);

    /**
     * 添加好友
     * 
     * @param userId 用户ID
     * @param friendId 好友ID
     * @param alias 好友别名
     * @param remark 备注
     * @return 是否成功
     */
    boolean addFriend(Long userId, Long friendId, String alias, String remark);

    /**
     * 删除好友
     * 
     * @param userId 用户ID
     * @param friendId 好友ID
     * @return 是否成功
     */
    boolean deleteFriend(Long userId, Long friendId);

    /**
     * 批量删除好友
     * 
     * @param userId 用户ID
     * @param friendIds 好友ID列表
     * @return 删除数量
     */
    int deleteFriendBatch(Long userId, List<Long> friendIds);

    /**
     * 更新好友信息
     * 
     * @param userId 用户ID
     * @param friendId 好友ID
     * @param alias 好友别名
     * @param remark 备注
     * @return 是否成功
     */
    boolean updateFriend(Long userId, Long friendId, String alias, String remark);

    /**
     * 检查是否为好友关系
     * 
     * @param userId 用户ID
     * @param friendId 好友ID
     * @return 是否为好友
     */
    boolean isFriend(Long userId, Long friendId);

    /**
     * 搜索好友
     * 
     * @param userId 用户ID
     * @param keyword 搜索关键词
     * @return 好友列表
     */
    List<ImFriend> searchFriends(Long userId, String keyword);

    /**
     * 获取好友详细信息
     * 
     * @param userId 用户ID
     * @param friendId 好友ID
     * @return 好友信息
     */
    ImFriend getFriendInfo(Long userId, Long friendId);

    /**
     * 获取互为好友的关系
     * 
     * @param userId1 用户1ID
     * @param userId2 用户2ID
     * @return 好友关系列表
     */
    List<ImFriend> getMutualFriendship(Long userId1, Long userId2);

    /**
     * 统计用户好友数量
     * 
     * @param userId 用户ID
     * @return 好友数量
     */
    int countUserFriends(Long userId);

    /**
     * 删除用户的所有好友关系
     * 
     * @param userId 用户ID
     * @return 删除数量
     */
    int deleteUserAllFriends(Long userId);
}