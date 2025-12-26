package com.ruoyi.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.im.domain.ImFriendRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 好友请求Mapper接口
 * 
 * @author ruoyi
 * @date 2024-01-01
 */
@Mapper
public interface ImFriendRequestMapper extends BaseMapper<ImFriendRequest> {

    /**
     * 查询用户收到的好友请求列表（包含发送者详细信息）
     * 
     * @param receiverId 接收者用户ID
     * @return 好友请求列表
     */
    List<ImFriendRequest> selectReceivedRequestsWithDetails(@Param("receiverId") Long receiverId);

    /**
     * 查询用户发送的好友请求列表（包含接收者详细信息）
     * 
     * @param senderId 发送者用户ID
     * @return 好友请求列表
     */
    List<ImFriendRequest> selectSentRequestsWithDetails(@Param("senderId") Long senderId);

    /**
     * 查询两个用户之间的待处理好友请求
     * 
     * @param senderId 发送者用户ID
     * @param receiverId 接收者用户ID
     * @return 好友请求
     */
    ImFriendRequest selectPendingRequest(@Param("senderId") Long senderId, @Param("receiverId") Long receiverId);

    /**
     * 统计用户收到的待处理好友请求数量
     * 
     * @param receiverId 接收者用户ID
     * @return 待处理请求数量
     */
    int countPendingRequests(@Param("receiverId") Long receiverId);

    /**
     * 批量更新好友请求状态
     * 
     * @param requestIds 请求ID列表
     * @param status 新状态
     * @param handlerId 处理人ID
     * @return 更新数量
     */
    int updateStatusBatch(@Param("requestIds") List<Long> requestIds, 
                         @Param("status") String status, 
                         @Param("handlerId") Long handlerId);

    /**
     * 删除过期的好友请求（超过指定天数的已处理请求）
     * 
     * @param days 天数
     * @return 删除数量
     */
    int deleteExpiredRequests(@Param("days") int days);
}