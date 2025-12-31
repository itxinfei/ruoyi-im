package com.ruoyi.im.mapper;

import com.ruoyi.im.domain.ImFriend;
import java.util.List;

/**
 * 好友Mapper接口
 * 
 * @author ruoyi
 */
public interface ImFriendMapper {
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
     * 删除好友
     * 
     * @param id 好友ID
     * @return 结果
     */
    public int deleteImFriendById(Long id);

    /**
     * 批量删除好友
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    public int deleteImFriendByIds(Long[] ids);
    
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
}