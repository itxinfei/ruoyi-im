package com.ruoyi.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.im.domain.ImDingMessage;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DING消息Mapper接口
 *
 * @author ruoyi
 */
public interface ImDingMapper extends BaseMapper<ImDingMessage> {

    /**
     * 根据会话ID查询DING消息列表
     *
     * @param conversationId 会话ID
     * @return DING消息列表
     */
    List<ImDingMessage> selectByConversationId(@Param("conversationId") Long conversationId);

    /**
     * 根据发送者ID查询DING消息列表
     *
     * @param senderId 发送者ID
     * @return DING消息列表
     */
    List<ImDingMessage> selectBySenderId(@Param("senderId") Long senderId);

    /**
     * 查询用户收到的DING消息列表
     *
     * @param userId 用户ID
     * @return DING消息列表
     */
    List<ImDingMessage> selectByReceiverId(@Param("userId") Long userId);

    /**
     * 查询激活状态的DING消息列表
     *
     * @return 激活的DING消息列表
     */
    List<ImDingMessage> selectActiveDings();

    /**
     * 查询过期的DING消息列表
     *
     * @return 过期的DING消息列表
     */
    List<ImDingMessage> selectExpiredDings();

    /**
     * 更新DING消息状态
     *
     * @param id     DING消息ID
     * @param status 新状态
     * @return 影响行数
     */
    int updateStatus(@Param("id") Long id, @Param("status") String status);

    /**
     * 增加已读人数
     *
     * @param id DING消息ID
     * @return 影响行数
     */
    int incrementReadCount(@Param("id") Long id);

    /**
     * 统计用户未读DING消息数量
     *
     * @param userId 用户ID
     * @return 未读数量
     */
    int countUnreadByUserId(@Param("userId") Long userId);

    /**
     * 批量查询DING消息的已读状态
     *
     * @param dingId  DING消息ID
     * @param userIds 用户ID列表
     * @return 已读用户ID列表
     */
    List<Long> selectReadUserIds(@Param("dingId") Long dingId, @Param("userIds") List<Long> userIds);
}
