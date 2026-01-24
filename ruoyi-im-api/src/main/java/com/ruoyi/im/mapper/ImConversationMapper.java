package com.ruoyi.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.im.domain.ImConversation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 会话Mapper接口
 *
 * 用于操作会话数据
 *
 * @author ruoyi
 */
@Mapper
public interface ImConversationMapper extends BaseMapper<ImConversation> {

    /**
     * 根据用户ID获取会话列表
     *
     * @param userId 用户ID
     * @return 会话列表
     */
    List<ImConversation> selectConversationsByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID获取会话列表（别名方法）
     *
     * @param userId 用户ID
     * @return 会话列表
     */
    List<ImConversation> selectByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID和目标ID获取会话
     *
     * @param userId 用户ID
     * @param targetId 目标ID
     * @return 会话
     */
    ImConversation selectConversationByUserIdAndTargetId(@Param("userId") Long userId, @Param("targetId") Long targetId);

    /**
     * 根据类型和目标ID获取会话
     *
     * @param type 会话类型
     * @param targetId 目标ID
     * @return 会话
     */
    ImConversation selectByTypeAndTarget(@Param("type") String type, @Param("targetId1") Long targetId1, @Param("targetId2") Long targetId2);

    /**
     * 更新会话最后消息信息
     *
     * @param conversationId 会话ID
     * @param lastMessageId 最后消息ID
     * @param lastMessageTime 最后消息时间
     * @return 更新行数
     */
    int updateLastMessage(@Param("conversationId") Long conversationId,
                          @Param("lastMessageId") Long lastMessageId,
                          @Param("lastMessageTime") java.time.LocalDateTime lastMessageTime);

    /**
     * 搜索会话
     *
     * @param userId 用户ID
     * @param keyword 搜索关键词
     * @return 会话列表
     */
    List<ImConversation> searchConversations(@Param("userId") Long userId, @Param("keyword") String keyword);

    /**
     * 获取用户未读消息总数
     *
     * @param userId 用户ID
     * @return 未读消息总数
     */
    Integer getTotalUnreadCount(@Param("userId") Long userId);

    /**
     * 查询会话列表
     *
     * @param imConversation 查询条件
     * @return 会话列表
     */
    List<ImConversation> selectImConversationList(ImConversation imConversation);
}