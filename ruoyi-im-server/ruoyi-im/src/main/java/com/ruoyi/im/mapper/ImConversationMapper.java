package com.ruoyi.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.im.domain.ImConversation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 会话Mapper接口
 * 
 * @author ruoyi
 * @date 2024-01-01
 */
@Mapper
public interface ImConversationMapper extends BaseMapper<ImConversation> {

    /**
     * 查询用户的会话列表（包含详细信息）
     * 
     * @param userId 用户ID
     * @return 会话列表
     */
    List<ImConversation> selectUserConversationsWithDetails(@Param("userId") Long userId);

    /**
     * 根据类型和目标ID查询会话
     * 
     * @param type 会话类型
     * @param targetId 目标ID
     * @return 会话信息
     */
    ImConversation selectByTypeAndTargetId(@Param("type") String type, @Param("targetId") Long targetId);

    /**
     * 查询两个用户之间的私聊会话
     * 
     * @param userId1 用户1ID
     * @param userId2 用户2ID
     * @return 会话信息
     */
    ImConversation selectPrivateConversation(@Param("userId1") Long userId1, @Param("userId2") Long userId2);

    /**
     * 查询群组会话
     * 
     * @param groupId 群组ID
     * @return 会话信息
     */
    ImConversation selectGroupConversation(@Param("groupId") Long groupId);

    /**
     * 更新会话最后消息信息
     * 
     * @param conversationId 会话ID
     * @param lastMessageId 最后消息ID
     * @return 更新数量
     */
    int updateLastMessage(@Param("conversationId") Long conversationId, @Param("lastMessageId") Long lastMessageId);

    /**
     * 批量查询会话信息
     * 
     * @param conversationIds 会话ID列表
     * @return 会话列表
     */
    List<ImConversation> selectBatchByIds(@Param("conversationIds") List<Long> conversationIds);

    /**
     * 搜索会话（根据标题）
     * 
     * @param userId 用户ID
     * @param keyword 搜索关键词
     * @return 会话列表
     */
    List<ImConversation> searchConversations(@Param("userId") Long userId, @Param("keyword") String keyword);

    /**
     * 查询活跃会话（根据最近消息时间）
     * 
     * @param userId 用户ID
     * @param days 天数
     * @param limit 限制数量
     * @return 会话列表
     */
    List<ImConversation> selectActiveConversations(@Param("userId") Long userId, @Param("days") int days, @Param("limit") int limit);

    /**
     * 统计用户会话数量
     * 
     * @param userId 用户ID
     * @return 会话数量
     */
    int countUserConversations(@Param("userId") Long userId);

    /**
     * 删除用户相关的所有会话
     * 
     * @param userId 用户ID
     * @return 删除数量
     */
    int deleteByUserId(@Param("userId") Long userId);
}