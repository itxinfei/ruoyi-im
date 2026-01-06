package com.ruoyi.im.mapper;

import com.ruoyi.im.domain.ImFriendRequest;
import com.ruoyi.im.mapper.base.BaseMapper;

import java.util.List;

/**
 * 好友申请Mapper接口
 *
 * @author ruoyi
 */
public interface ImFriendRequestMapper extends BaseMapper<ImFriendRequest> {

    /**
     * 查询好友申请
     *
     * @param id 好友申请ID
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
     * @param id 好友申请ID
     * @return 结果
     */
    int deleteImFriendRequestById(Long id);

    /**
     * 批量删除好友申请
     *
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    int deleteImFriendRequestByIds(Long[] ids);

    /**
     * 根据申请人ID和被申请人ID查询好友申请
     *
     * @param fromUserId 申请人ID
     * @param toUserId   被申请人ID
     * @return 好友申请
     */
    ImFriendRequest selectImFriendRequestByFromAndToUserId(Long fromUserId, Long toUserId);

    /**
     * 根据申请人ID查询好友申请列表
     *
     * @param fromUserId 申请人ID
     * @return 好友申请集合
     */
    List<ImFriendRequest> selectImFriendRequestListByFromUserId(Long fromUserId);

    /**
     * 根据被申请人ID查询好友申请列表
     *
     * @param toUserId 被申请人ID
     * @return 好友申请集合
     */
    List<ImFriendRequest> selectImFriendRequestListByToUserId(Long toUserId);
}
