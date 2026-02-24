package com.ruoyi.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.im.domain.ImFriendRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 好友申请 Mapper 接口
 *
 * @author ruoyi
 */
@Mapper
public interface ImFriendRequestMapper extends BaseMapper<ImFriendRequest> {

    /**
     * 查询好友申请
     *
     * @param id 好友申请 ID
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
     * @param id 好友申请 ID
     * @return 结果
     */
    int deleteImFriendRequestById(Long id);

    /**
     * 批量删除好友申请
     *
     * @param ids 需要删除的数据 ID
     * @return 结果
     */
    int deleteImFriendRequestByIds(Long[] ids);

    /**
     * 根据申请人 ID 和被申请人 ID 查询好友申请
     *
     * @param fromUserId 申请人 ID
     * @param toUserId   被申请人 ID
     * @return 好友申请
     */
    ImFriendRequest selectImFriendRequestByFromAndToUserId(@Param("fromUserId") Long fromUserId, @Param("toUserId") Long toUserId);

    /**
     * 根据申请人 ID 查询好友申请列表
     *
     * @param fromUserId 申请人 ID
     * @return 好友申请集合
     */
    List<ImFriendRequest> selectImFriendRequestListByFromUserId(@Param("fromUserId") Long fromUserId);

    /**
     * 根据被申请人 ID 查询好友申请列表
     *
     * @param toUserId 被申请人 ID
     * @return 好友申请集合
     */
    List<ImFriendRequest> selectImFriendRequestListByToUserId(@Param("toUserId") Long toUserId);

    /**
     * 根据被申请人 ID 和状态查询好友申请列表
     *
     * @param toUserId 被申请人 ID
     * @param status   状态
     * @return 好友申请集合
     */
    List<ImFriendRequest> selectImFriendRequestListByToUserIdAndStatus(@Param("toUserId") Long toUserId, @Param("status") String status);
}
