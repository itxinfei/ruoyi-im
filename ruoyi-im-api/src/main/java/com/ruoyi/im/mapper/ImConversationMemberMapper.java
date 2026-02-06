package com.ruoyi.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.im.domain.ImConversationMember;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 会话成员Mapper接口
 *
 * 用于操作会话成员数据
 *
 * @author ruoyi
 */
@Mapper
public interface ImConversationMemberMapper extends BaseMapper<ImConversationMember> {

    /**
     * 根据会话ID和用户ID查询会话成员
     *
     * @param conversationId 会话ID
     * @param userId 用户ID
     * @return 会话成员
     */
    ImConversationMember selectByConversationIdAndUserId(@Param("conversationId") Long conversationId, @Param("userId") Long userId);

    /**
     * 根据用户ID查询会话成员列表
     *
     * @param userId 用户ID
     * @return 会话成员列表
     */
    List<ImConversationMember> selectByUserId(@Param("userId") Long userId);

    /**
     * 根据会话ID查询会话成员列表
     *
     * @param conversationId 会话ID
     * @return 会话成员列表
     */
    List<ImConversationMember> selectByConversationId(@Param("conversationId") Long conversationId);

    /**
     * 更新未读消息数
     *
     * @param conversationId 会话ID
     * @param userId 用户ID
     * @param unreadCount 未读消息数
     * @return 更新行数
     */
    int updateUnreadCount(@Param("conversationId") Long conversationId, @Param("userId") Long userId, @Param("unreadCount") Integer unreadCount);

    /**
     * 增加未读消息数
     *
     * @param conversationId 会话ID
     * @param userId 用户ID
     * @param count 增加的数量
     * @return 更新行数
     */
    int incrementUnreadCount(@Param("conversationId") Long conversationId, @Param("userId") Long userId, @Param("count") Integer count);

    /**
     * 减少未读消息数
     *
     * @param conversationId 会话ID
     * @param userId 用户ID
     * @param count 减少的数量
     * @return 更新行数
     */
    int decrementUnreadCount(@Param("conversationId") Long conversationId, @Param("userId") Long userId, @Param("count") Integer count);

    /**
     * 更新最后已读消息ID
     *
     * @param conversationId 会话ID
     * @param userId 用户ID
     * @param lastReadMessageId 最后已读消息ID
     * @return 更新行数
     */
    int updateLastReadMessageId(@Param("conversationId") Long conversationId, @Param("userId") Long userId, @Param("lastReadMessageId") Long lastReadMessageId);

    /**
     * 更新置顶状态
     *
     * @param conversationId 会话ID
     * @param userId 用户ID
     * @param isPinned 是否置顶，1为置顶，0为不置顶
     * @return 更新行数
     */
    int updatePinned(@Param("conversationId") Long conversationId, @Param("userId") Long userId, @Param("isPinned") Integer isPinned);

    /**
     * 更新免打扰状态
     *
     * @param conversationId 会话ID
     * @param userId 用户ID
     * @param isMuted 是否免打扰，1为免打扰，0为不免打扰
     * @return 更新行数
     */
    int updateMuted(@Param("conversationId") Long conversationId, @Param("userId") Long userId, @Param("isMuted") Integer isMuted);

    /**
     * 标记为已删除（软删除）
     *
     * @param conversationId 会话ID
     * @param userId 用户ID
     * @return 更新行数
     */
    int markAsDeleted(@Param("conversationId") Long conversationId, @Param("userId") Long userId);

    /**
     * 删除指定会话和用户之间的关系
     *
     * @param conversationId 会话ID
     * @param userId 用户ID
     * @return 删除行数
     */
    int deleteByConversationIdAndUserId(@Param("conversationId") Long conversationId, @Param("userId") Long userId);

    /**
     * 插入会话成员
     *
     * @param member 会话成员
     * @return 插入行数
     */
    int insertImConversationMember(ImConversationMember member);

    /**
     * 批量统计会话成员数量
     *
     * @param conversationIds 会话ID列表
     * @return Map<会话ID, 成员数量>
     */
    List<java.util.Map<String, Object>> countMembersByConversationIds(@Param("conversationIds") List<Long> conversationIds);

    /**
     * 查询用户归档的会话列表
     *
     * @param userId 用户ID
     * @return 归档的会话成员列表
     */
    List<ImConversationMember> selectArchivedByUserId(@Param("userId") Long userId);

    /**
     * 更新归档状态
     *
     * @param conversationId 会话ID
     * @param userId 用户ID
     * @param isArchived 是否归档，1为归档，0为不归档
     * @return 更新行数
     */
    int updateArchived(@Param("conversationId") Long conversationId, @Param("userId") Long userId, @Param("isArchived") Integer isArchived);
}