package com.ruoyi.web.mapper;

import com.ruoyi.web.domain.ImFriendRequest;
import java.util.List;
import java.util.Map;

/**
 * 好友申请Mapper接口
 *
 * @author ruoyi
 */
public interface ImFriendRequestMapper {

    /**
     * 查询好友申请
     *
     * @param id 好友申请主键
     * @return 好友申请
     */
    ImFriendRequest selectImFriendRequestById(Long id);

    /**
     * 查询好友申请列表
     *
     * @param imFriendRequest 好友申请
     * @return 好友申请集合
     */
    List<ImFriendRequest> selectImFriendRequestList(ImFriendRequest imFriendRequest);

    /**
     * 新增好友申请
     *
     * @param imFriendRequest 好友申请
     * @return 结果
     */
    int insertImFriendRequest(ImFriendRequest imFriendRequest);

    /**
     * 修改好友申请
     *
     * @param imFriendRequest 好友申请
     * @return 结果
     */
    int updateImFriendRequest(ImFriendRequest imFriendRequest);

    /**
     * 删除好友申请
     *
     * @param id 好友申请主键
     * @return 结果
     */
    int deleteImFriendRequestById(Long id);

    /**
     * 批量删除好友申请
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    int deleteImFriendRequestByIds(Long[] ids);

    /**
     * 获取好友申请统计数据
     *
     * @return 统计数据
     */
    Map<String, Object> getFriendRequestStatistics();
}
