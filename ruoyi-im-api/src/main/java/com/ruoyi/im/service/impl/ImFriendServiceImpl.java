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
public class ImFriendServiceImpl implements ImFriendService {
    @Autowired
    private ImFriendMapper imFriendMapper;

    /**
     * 查询好友
     * 
     * @param id 好友ID
     * @return 好友
     */
    @Override
    public ImFriend selectImFriendById(Long id) {
        return imFriendMapper.selectImFriendById(id);
    }

    /**
     * 查询好友列表
     * 
     * @param imFriend 好友
     * @return 好友
     */
    @Override
    public List<ImFriend> selectImFriendList(ImFriend imFriend) {
        return imFriendMapper.selectImFriendList(imFriend);
    }

    /**
     * 新增好友
     * 
     * @param imFriend 好友
     * @return 结果
     */
    @Override
    public int insertImFriend(ImFriend imFriend) {
        return imFriendMapper.insertImFriend(imFriend);
    }

    /**
     * 修改好友
     * 
     * @param imFriend 好友
     * @return 结果
     */
    @Override
    public int updateImFriend(ImFriend imFriend) {
        return imFriendMapper.updateImFriend(imFriend);
    }

    /**
     * 批量删除好友
     * 
     * @param ids 需要删除的好友ID
     * @return 结果
     */
    @Override
    public int deleteImFriendByIds(Long[] ids) {
        return imFriendMapper.deleteImFriendByIds(ids);
    }

    /**
     * 删除好友信息
     * 
     * @param id 好友ID
     * @return 结果
     */
    @Override
    public int deleteImFriendById(Long id) {
        return imFriendMapper.deleteImFriendById(id);
    }
    
    /**
     * 根据用户ID查询好友列表
     * 
     * @param userId 用户ID
     * @return 好友集合
     */
    @Override
    public List<ImFriend> selectImFriendListByUserId(Long userId) {
        return imFriendMapper.selectImFriendListByUserId(userId);
    }
    
    /**
     * 根据用户ID和好友用户ID查询好友关系
     * 
     * @param userId 用户ID
     * @param friendUserId 好友用户ID
     * @return 好友关系
     */
    @Override
    public ImFriend selectImFriendByUserIdAndFriendUserId(Long userId, Long friendUserId) {
        return imFriendMapper.selectImFriendByUserIdAndFriendUserId(userId, friendUserId);
    }
    
    /**
     * 添加好友
     * 
     * @param userId 用户ID
     * @param friendUserId 好友用户ID
     * @param alias 别名
     * @param remark 备注
     * @return 结果
     */
    @Override
    public int addFriend(Long userId, Long friendUserId, String alias, String remark) {
        // 检查是否已经是好友
        ImFriend existingFriend = selectImFriendByUserIdAndFriendUserId(userId, friendUserId);
        if (existingFriend != null) {
            // 已经是好友，不需要重复添加
            return 0;
        }
        
        // 创建好友关系（双向）
        ImFriend friend1 = new ImFriend();
        friend1.setUserId(userId);
        friend1.setFriendUserId(friendUserId);
        friend1.setAlias(alias);
        friend1.setRemark(remark);
        friend1.setStatus("ACTIVE");
        
        ImFriend friend2 = new ImFriend();
        friend2.setUserId(friendUserId);
        friend2.setFriendUserId(userId);
        friend2.setAlias(null); // 对方不设置别名
        friend2.setRemark(null); // 对方不设置备注
        friend2.setStatus("ACTIVE");
        
        int result = insertImFriend(friend1);
        if (result > 0) {
            result += insertImFriend(friend2);
        }
        
        return result;
    }
    
    /**
     * 删除好友
     * 
     * @param userId 用户ID
     * @param friendUserId 好友用户ID
     * @return 结果
     */
    @Override
    public int deleteFriend(Long userId, Long friendUserId) {
        // 删除用户的好友关系
        ImFriend friend1 = selectImFriendByUserIdAndFriendUserId(userId, friendUserId);
        if (friend1 != null) {
            deleteImFriendById(friend1.getId());
        }
        
        // 删除对方的好友关系
        ImFriend friend2 = selectImFriendByUserIdAndFriendUserId(friendUserId, userId);
        if (friend2 != null) {
            deleteImFriendById(friend2.getId());
        }
        
        return 1; // 成功删除双向好友关系
    }
}