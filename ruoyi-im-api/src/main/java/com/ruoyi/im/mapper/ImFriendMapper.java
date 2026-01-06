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
}
