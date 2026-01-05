package com.ruoyi.im.service;

import com.ruoyi.im.domain.ImFriend;

import java.util.List;

/**
 * 好友Service接口
 * 
 * @author ruoyi
 */
public interface ImFriendService 
{
    /**
     * 查询好友
     * 
     * @param id 好友ID
     * @return 好友
     */
    public ImFriend selectImFriendById(Long id);

    /**
     * 查询好友列表
     * 
     * @param imFriend 好友
     * @return 好友集合
     */
    public List<ImFriend> selectImFriendList(ImFriend imFriend);

    /**
     * 新增好友
     * 
     * @param imFriend 好友
     * @return 结果
     */
    public int insertImFriend(ImFriend imFriend);

    /**
     * 修改好友
     * 
     * @param imFriend 好友
     * @return 结果
     */
    public int updateImFriend(ImFriend imFriend);

    /**
     * 批量删除好友
     * 
     * @param ids 需要删除的好友ID
     * @return 结果
     */
    public int deleteImFriendByIds(Long[] ids);

    /**
     * 删除好友信息
     * 
     * @param id 好友ID
     * @return 结果
     */
    public int deleteImFriendById(Long id);
    
    /**
     * 根据用户ID和好友ID查询好友关系
     * 
     * @param userId 用户ID
     * @param friendId 好友ID
     * @return 好友关系
     */
    default ImFriend selectImFriendByUserIdAndFriendId(Long userId, Long friendId) {
        return selectImFriendByUserIdAndFriendId(userId, friendId);
    }
    
    /**
     * 根据用户ID查询好友列表
     * 
     * @param userId 用户ID
     * @return 好友列表
     */
    default List<ImFriend> selectImFriendListByUserId(Long userId) {
        return selectImFriendListByUserId(userId);
    }
    
    /**
     * 根据用户ID和好友ID删除好友关系
     * 
     * @param userId 用户ID
     * @param friendId 好友ID
     * @return 删除结果
     */
    default int deleteImFriendByUserIdAndFriendId(Long userId, Long friendId) {
        return deleteImFriendByUserIdAndFriendId(userId, friendId);
    }
}