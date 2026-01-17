package com.ruoyi.web.mapper;

import com.ruoyi.web.domain.ImFriend;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

/**
 * 好友关系Mapper接口
 *
 * @author ruoyi
 */
@Mapper
public interface ImFriendMapper {

    /**
     * 查询好友关系
     *
     * @param id 好友关系主键
     * @return 好友关系
     */
    public ImFriend selectImFriendById(Long id);

    /**
     * 查询好友关系列表
     *
     * @param imFriend 好友关系
     * @return 好友关系集合
     */
    public List<ImFriend> selectImFriendList(ImFriend imFriend);

    /**
     * 根据用户ID查询好友列表
     *
     * @param userId 用户ID
     * @return 好友列表
     */
    public List<ImFriend> selectFriendsByUserId(Long userId);

    /**
     * 查询用户的好友分组列表
     *
     * @param userId 用户ID
     * @return 分组名称列表
     */
    public List<String> selectFriendGroups(Long userId);

    /**
     * 检查好友关系是否存在
     *
     * @param params 包含userId和friendId的参数
     * @return 存在数量
     */
    public Integer checkFriendExists(java.util.Map<String, Object> params);

    /**
     * 根据用户ID和好友ID查询关系
     *
     * @param params 包含userId和friendId的参数
     * @return 好友关系
     */
    public ImFriend selectFriendByUserAndFriend(java.util.Map<String, Object> params);

    /**
     * 新增好友关系
     *
     * @param imFriend 好友关系
     * @return 结果
     */
    public int insertImFriend(ImFriend imFriend);

    /**
     * 修改好友关系
     *
     * @param imFriend 好友关系
     * @return 结果
     */
    public int updateImFriend(ImFriend imFriend);

    /**
     * 删除好友关系
     *
     * @param id 好友关系主键
     * @return 结果
     */
    public int deleteImFriendById(Long id);

    /**
     * 批量删除好友关系
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteImFriendByIds(Long[] ids);

    /**
     * 删除指定好友关系
     *
     * @param params 包含userId和friendId的参数
     * @return 结果
     */
    public int deleteFriendByUserAndFriend(java.util.Map<String, Object> params);

    /**
     * 统计好友关系总数
     */
    int countTotalFriends();

    /**
     * 统计今日新增好友数
     */
    int countTodayAddedFriends();

    /**
     * 统计待处理好友申请数
     */
    int countPendingRequests();

    /**
     * 查询所有好友分组（去重）
     *
     * @return 分组名称列表
     */
    List<String> selectDistinctGroups();

    /**
     * 查询两个用户之间是否存在好友关系
     *
     * @param params 包含userId和friendId的参数
     * @return 好友关系，不存在返回null
     */
    ImFriend selectFriendByUsers(java.util.Map<String, Object> params);
}
