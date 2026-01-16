package com.ruoyi.web.service;

import com.ruoyi.web.domain.ImFriendRequest;
import java.util.List;
import java.util.Map;

/**
 * 好友申请Service接口
 *
 * @author ruoyi
 */
public interface ImFriendRequestService {

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
     * 批量删除好友申请
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    int deleteImFriendRequestByIds(Long[] ids);

    /**
     * 删除好友申请信息
     *
     * @param id 好友申请主键
     * @return 结果
     */
    int deleteImFriendRequestById(Long id);

    /**
     * 处理好友申请
     *
     * @param id 申请ID
     * @param approved 是否同意
     * @return 结果
     */
    int handleFriendRequest(Long id, boolean approved);

    /**
     * 获取好友申请统计数据
     *
     * @return 统计数据
     */
    Map<String, Object> getFriendRequestStatistics();
}
