package com.ruoyi.im.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.im.domain.ImFriend;
import com.ruoyi.im.mapper.ImFriendMapper;
import com.ruoyi.im.service.IImFriendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;

/**
 * 好友关系Service业务层处理
 * 
 * @author ruoyi
 * @date 2024-01-01
 */
@Service
public class ImFriendServiceImpl extends ServiceImpl<ImFriendMapper, ImFriend> implements IImFriendService {

    @Autowired
    private ImFriendMapper imFriendMapper;
    
    public ImFriendServiceImpl() {
        super();
    }
    
    public ImFriendServiceImpl(ImFriendMapper imFriendMapper) {
        super();
        this.imFriendMapper = imFriendMapper;
    }

    /**
     * 查询用户的好友列表
     * 
     * @param userId 用户ID
     * @return 好友列表
     */
    @Override
    public List<ImFriend> selectFriendList(Long userId) {
        return imFriendMapper.selectFriendListWithDetails(userId);
    }

    /**
     * 添加好友
     * 
     * @param userId 用户ID
     * @param friendId 好友ID
     * @param alias 好友别名
     * @param remark 备注
     * @return 是否成功
     */
    @Override
    public boolean addFriend(Long userId, Long friendId, String alias, String remark) {
        // 检查是否已经是好友
        if (isFriend(userId, friendId)) {
            return false;
        }
        
        ImFriend friend = new ImFriend();
        friend.setUserId(userId);
        friend.setFriendUserId(friendId);
        friend.setAlias(alias);
        friend.setRemark(remark);
        friend.setStatus("NORMAL");
        friend.setCreateTime(new Date());
        
        return save(friend);
    }

    /**
     * 删除好友
     * 
     * @param userId 用户ID
     * @param friendId 好友ID
     * @return 是否成功
     */
    @Override
    public boolean deleteFriend(Long userId, Long friendId) {
        QueryWrapper<ImFriend> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                   .eq("friend_user_id", friendId);
        return remove(queryWrapper);
    }

    /**
     * 批量删除好友
     * 
     * @param userId 用户ID
     * @param friendIds 好友ID列表
     * @return 删除数量
     */
    @Override
    public int deleteFriendBatch(Long userId, List<Long> friendIds) {
        return imFriendMapper.deleteBatchByUserIdAndFriendIds(userId, friendIds);
    }

    /**
     * 更新好友信息
     * 
     * @param userId 用户ID
     * @param friendId 好友ID
     * @param alias 好友别名
     * @param remark 备注
     * @return 是否成功
     */
    @Override
    public boolean updateFriend(Long userId, Long friendId, String alias, String remark) {
        ImFriend friend = imFriendMapper.selectByUserIdAndFriendId(userId, friendId);
        if (friend == null) {
            return false;
        }
        
        friend.setAlias(alias);
        friend.setRemark(remark);
        friend.setUpdateTime(new Date());
        
        return updateById(friend);
    }

    /**
     * 检查是否为好友关系
     * 
     * @param userId 用户ID
     * @param friendId 好友ID
     * @return 是否为好友
     */
    @Override
    public boolean isFriend(Long userId, Long friendId) {
        return imFriendMapper.isFriend(userId, friendId);
    }

    /**
     * 搜索好友
     * 
     * @param userId 用户ID
     * @param keyword 搜索关键词
     * @return 好友列表
     */
    @Override
    public List<ImFriend> searchFriends(Long userId, String keyword) {
        return imFriendMapper.searchFriends(userId, keyword);
    }

    /**
     * 获取好友详细信息
     * 
     * @param userId 用户ID
     * @param friendId 好友ID
     * @return 好友信息
     */
    @Override
    public ImFriend getFriendInfo(Long userId, Long friendId) {
        return imFriendMapper.selectByUserIdAndFriendId(userId, friendId);
    }

    /**
     * 获取互为好友的关系
     * 
     * @param userId1 用户1ID
     * @param userId2 用户2ID
     * @return 好友关系列表
     */
    @Override
    public List<ImFriend> getMutualFriendship(Long userId1, Long userId2) {
        return imFriendMapper.selectMutualFriendship(userId1, userId2);
    }

    /**
     * 统计用户好友数量
     * 
     * @param userId 用户ID
     * @return 好友数量
     */
    @Override
    public int countUserFriends(Long userId) {
        QueryWrapper<ImFriend> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                   .eq("status", "NORMAL");
        return Math.toIntExact(count(queryWrapper));
    }

    /**
     * 删除用户的所有好友关系
     * 
     * @param userId 用户ID
     * @return 删除数量
     */
    @Override
    public int deleteUserAllFriends(Long userId) {
        QueryWrapper<ImFriend> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                   .or()
                   .eq("friend_user_id", userId);
        return Math.toIntExact(count(queryWrapper));
    }
}