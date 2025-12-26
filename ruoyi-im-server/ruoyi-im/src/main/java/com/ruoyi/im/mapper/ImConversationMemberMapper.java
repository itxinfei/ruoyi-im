package com.ruoyi.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.im.domain.ImConversationMember;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 会话成员Mapper接口
 * 
 * @author ruoyi
 * @date 2024-01-01
 */
@Mapper
public interface ImConversationMemberMapper extends BaseMapper<ImConversationMember> {

    /**
     * 查询会话成员列表（包含用户详细信息）
     * 
     * @param conversationId 会话ID
     * @return 会话成员列表
     */
    List<ImConversationMember> selectConversationMembersWithDetails(@Param("conversationId") Long conversationId);

    /**
     * 查询用户在会话中的成员信息
     * 
     * @param conversationId 会话ID
     * @param userId 用户ID
     * @return 会话成员信息
     */
    ImConversationMember selectConversationMember(@Param("conversationId") Long conversationId, @Param("userId") Long userId);

    /**
     * 检查用户是否为会话成员
     * 
     * @param conversationId 会话ID
     * @param userId 用户ID
     * @return 是否为会话成员
     */
    boolean isConversationMember(@Param("conversationId") Long conversationId, @Param("userId") Long userId);

    /**
     * 统计会话成员数量
     * 
     * @param conversationId 会话ID
     * @return 成员数量
     */
    int countConversationMembers(@Param("conversationId") Long conversationId);

    /**
     * 批量添加会话成员
     * 
     * @param members 成员列表
     * @return 添加数量
     */
    int insertBatch(@Param("members") List<ImConversationMember> members);

    /**
     * 批量删除会话成员
     * 
     * @param conversationId 会话ID
     * @param userIds 用户ID列表
     * @return 删除数量
     */
    int deleteBatchByConversationIdAndUserIds(@Param("conversationId") Long conversationId, @Param("userIds") List<Long> userIds);

    /**
     * 更新用户最后已读消息
     * 
     * @param conversationId 会话ID
     * @param userId 用户ID
     * @param lastReadMessageId 最后已读消息ID
     * @return 更新数量
     */
    int updateLastReadMessage(@Param("conversationId") Long conversationId, 
                             @Param("userId") Long userId, 
                             @Param("lastReadMessageId") Long lastReadMessageId);

    /**
     * 更新会话置顶状态
     * 
     * @param conversationId 会话ID
     * @param userId 用户ID
     * @param pinned 是否置顶
     * @return 更新数量
     */
    int updatePinned(@Param("conversationId") Long conversationId, 
                    @Param("userId") Long userId, 
                    @Param("pinned") Boolean pinned);

    /**
     * 更新会话免打扰状态
     * 
     * @param conversationId 会话ID
     * @param userId 用户ID
     * @param muted 是否免打扰
     * @return 更新数量
     */
    int updateMuted(@Param("conversationId") Long conversationId, 
                   @Param("userId") Long userId, 
                   @Param("muted") Boolean muted);

    /**
     * 查询用户的会话ID列表
     * 
     * @param userId 用户ID
     * @return 会话ID列表
     */
    List<Long> selectUserConversationIds(@Param("userId") Long userId);

    /**
     * 统计用户未读消息总数
     * 
     * @param userId 用户ID
     * @return 未读消息总数
     */
    int countUserUnreadMessages(@Param("userId") Long userId);

    /**
     * 查询用户置顶的会话列表
     * 
     * @param userId 用户ID
     * @return 会话成员列表
     */
    List<ImConversationMember> selectPinnedConversations(@Param("userId") Long userId);

    /**
     * 删除用户在所有会话中的成员关系
     * 
     * @param userId 用户ID
     * @return 删除数量
     */
    int deleteByUserId(@Param("userId") Long userId);

    /**
     * 删除会话的所有成员
     * 
     * @param conversationId 会话ID
     * @return 删除数量
     */
    int deleteByConversationId(@Param("conversationId") Long conversationId);
}