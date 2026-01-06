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
}