package com.ruoyi.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.im.domain.ImFriend;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 好友Mapper接口
 *
 * @author ruoyi
 */
@Mapper
public interface ImFriendMapper extends BaseMapper<ImFriend> {

    /**
     * 查询好友
     *
     * @param id 好友ID
     * @return 好友
     */
    ImFriend selectImFriendById(Long id);

    /**
     * 查询好友列表
     *
     * @param imFriend 好友
     * @return 好友集合
     */
    List<ImFriend> selectImFriendList(ImFriend imFriend);

    /**
     * 新增好友
     *
     * @param imFriend 好友
     * @return 结果
     */
    int insertImFriend(ImFriend imFriend);

    /**
     * 修改好友
     *
     * @param imFriend 好友
     * @return 结果
     */
    int updateImFriend(ImFriend imFriend);

    /**
     * 删除好友
     *
     * @param id 好友ID
     * @return 结果
     */
    int deleteImFriendById(Long id);

    /**
     * 批量删除好友
     *
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    int deleteImFriendByIds(Long[] ids);

    /**
     * 根据条件删除好友
     *
     * @param imFriend 条件
     * @return 结果
     */
    int deleteImFriendByCondition(ImFriend imFriend);

    /**
     * 根据用户ID查询好友列表
     *
     * @param userId 用户ID
     * @return 好友集合
     */
    List<ImFriend> selectImFriendListByUserId(Long userId);

    /**
     * 根据用户ID和好友ID查询好友关系
     *
     * @param userId 用户ID
     * @param friendId 好友ID
     * @return 好友关系
     */
    ImFriend selectImFriendByUserIdAndFriendId(Long userId, Long friendId);
}
