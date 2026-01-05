package com.ruoyi.im.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.im.mapper.ImFriendMapper;
import com.ruoyi.im.domain.ImFriend;
import com.ruoyi.im.service.ImFriendService;

/**
 * 好友Service业务层处理
 * 
 * @author ruoyi
 */
@Service
public class ImFriendServiceImpl implements ImFriendService
{
    @Autowired
    private ImFriendMapper imFriendMapper;

    /**
     * 查询好友
     * 
     * @param id 好友ID
     * @return 好友
     */
    @Override
    public ImFriend selectImFriendById(Long id)
    {
        return imFriendMapper.selectImFriendById(id);
    }

    /**
     * 查询好友列表
     * 
     * @param imFriend 好友
     * @return 好友
     */
    @Override
    public List<ImFriend> selectImFriendList(ImFriend imFriend)
    {
        return imFriendMapper.selectImFriendList(imFriend);
    }

    /**
     * 新增好友
     * 
     * @param imFriend 好友
     * @return 结果
     */
    @Override
    public int insertImFriend(ImFriend imFriend)
    {
        return imFriendMapper.insertImFriend(imFriend);
    }

    /**
     * 修改好友
     * 
     * @param imFriend 好友
     * @return 结果
     */
    @Override
    public int updateImFriend(ImFriend imFriend)
    {
        return imFriendMapper.updateImFriend(imFriend);
    }

    /**
     * 批量删除好友
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    @Override
    public int deleteImFriendByIds(Long[] ids)
    {
        return imFriendMapper.deleteImFriendByIds(ids);
    }

    /**
     * 删除好友信息
     * 
     * @param id 好友ID
     * @return 结果
     */
    @Override
    public int deleteImFriendById(Long id)
    {
        return imFriendMapper.deleteImFriendById(id);
    }
    
    /**
     * 根据用户ID和好友ID查询好友关系
     * 
     * @param userId 用户ID
     * @param friendId 好友ID
     * @return 好友关系
     */
    @Override
    public ImFriend selectImFriendByUserIdAndFriendId(Long userId, Long friendId) {
        // 创建查询条件
        ImFriend condition = new ImFriend();
        condition.setUserId(userId);
        condition.setFriendId(friendId);
        List<ImFriend> friends = imFriendMapper.selectImFriendList(condition);
        return friends != null && !friends.isEmpty() ? friends.get(0) : null;
    }
    
    /**
     * 根据用户ID查询好友列表
     * 
     * @param userId 用户ID
     * @return 好友列表
     */
    @Override
    public List<ImFriend> selectImFriendListByUserId(Long userId) {
        // 创建查询条件
        ImFriend condition = new ImFriend();
        condition.setUserId(userId);
        return imFriendMapper.selectImFriendList(condition);
    }
    
    /**
     * 根据用户ID和好友ID删除好友关系
     * 
     * @param userId 用户ID
     * @param friendId 好友ID
     * @return 删除结果
     */
    @Override
    public int deleteImFriendByUserIdAndFriendId(Long userId, Long friendId) {
        ImFriend condition = new ImFriend();
        condition.setUserId(userId);
        condition.setFriendId(friendId);
        return imFriendMapper.deleteImFriendByCondition(condition);
    }
}