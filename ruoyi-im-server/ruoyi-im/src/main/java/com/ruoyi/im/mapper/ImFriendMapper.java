package com.ruoyi.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.im.domain.ImFriend;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 好友关系Mapper接口
 * 
 * @author ruoyi
 * @date 2024-01-01
 */
@Mapper
public interface ImFriendMapper extends BaseMapper<ImFriend> {

    /**
     * 查询用户的好友列表（包含好友详细信息）
     * 
     * @param userId 用户ID
     * @return 好友列表
     */
    List<ImFriend> selectFriendListWithDetails(@Param("userId") Long userId);

    /**
     * 查询互为好友的关系
     * 
     * @param userId1 用户1ID
     * @param userId2 用户2ID
     * @return 好友关系列表
     */
    List<ImFriend> selectMutualFriendship(@Param("userId1") Long userId1, @Param("userId2") Long userId2);

    /**
     * 检查是否为好友关系
     * 
     * @param userId 用户ID
     * @param friendId 好友ID
     * @return 是否为好友
     */
    boolean isFriend(@Param("userId") Long userId, @Param("friendId") Long friendId);

    /**
     * 根据用户ID和好友ID查询好友关系
     * 
     * @param userId 用户ID
     * @param friendId 好友ID
     * @return 好友关系
     */
    ImFriend selectByUserIdAndFriendId(@Param("userId") Long userId, @Param("friendId") Long friendId);

    /**
     * 批量删除好友关系
     * 
     * @param userId 用户ID
     * @param friendIds 好友ID列表
     * @return 删除数量
     */
    int deleteBatchByUserIdAndFriendIds(@Param("userId") Long userId, @Param("friendIds") List<Long> friendIds);

    /**
     * 搜索好友（根据用户名或昵称）
     * 
     * @param userId 用户ID
     * @param keyword 搜索关键词
     * @return 好友列表
     */
    List<ImFriend> searchFriends(@Param("userId") Long userId, @Param("keyword") String keyword);
}