package com.ruoyi.im.service;

import com.ruoyi.im.domain.ImFriend;
import java.util.List;

/**
 * 好友Service接口
 * 
 * @author ruoyi
 */
public interface ImFriendService extends BaseService<ImFriend> {
    
    @Override
    ImFriend selectById(Long id);
    
    @Override
    List<ImFriend> selectList(ImFriend imFriend);
    
    @Override
    int insert(ImFriend imFriend);
    
    @Override
    int update(ImFriend imFriend);
    
    @Override
    int deleteByIds(Long[] ids);
    
    @Override
    int deleteById(Long id);
    
    /**
     * 根据用户ID查询好友列表
     * 
     * @param userId 用户ID
     * @return 好友集合
     */
    public List<ImFriend> selectImFriendListByUserId(Long userId);
    
    /**
     * 根据用户ID和好友用户ID查询好友关系
     * 
     * @param userId 用户ID
     * @param friendUserId 好友用户ID
     * @return 好友关系
     */
    public ImFriend selectImFriendByUserIdAndFriendUserId(Long userId, Long friendUserId);
    
    /**
     * 添加好友
     * 
     * @param userId 用户ID
     * @param friendUserId 好友用户ID
     * @param alias 别名
     * @param remark 备注
     * @return 结果
     */
    public int addFriend(Long userId, Long friendUserId, String alias, String remark);
    
    /**
     * 删除好友
     * 
     * @param userId 用户ID
     * @param friendUserId 好友用户ID
     * @return 结果
     */
    public int deleteFriend(Long userId, Long friendUserId);
}