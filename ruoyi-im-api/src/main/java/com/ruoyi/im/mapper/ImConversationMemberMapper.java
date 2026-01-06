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
     * 根据会话ID和用户ID获取会话成员关系
     *
     * @param conversationId 会话ID
     * @param userId 用户ID
     * @return 会话成员关系
     */
    ImConversationMember selectByConversationIdAndUserId(@Param("conversationId") Long conversationId,
                                                         @Param("userId") Long userId);

    /**
     * 根据用户ID获取会话成员列表
     *
     * @param userId 用户ID
     * @return 会话成员列表
     */
    List<ImConversationMember> selectByUserId(@Param("userId") Long userId);

    /**
     * 根据会话ID获取所有成员
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
    int updateUnreadCount(@Param("conversationId") Long conversationId,
                          @Param("userId") Long userId,
                          @Param("unreadCount") Integer unreadCount);

    /**
     * 更新最后已读消息ID
     *
     * @param conversationId 会话ID
     * @param userId 用户ID
     * @param lastReadMessageId 最后已读消息ID
     * @return 更新行数
     */
    int updateLastReadMessageId(@Param("conversationId") Long conversationId,
                                @Param("userId") Long userId,
                                @Param("lastReadMessageId") Long lastReadMessageId);

    /**
     * 设置会话置顶状态
     *
     * @param conversationId 会话ID
     * @param userId 用户ID
     * @param isPinned 是否置顶
     * @return 更新行数
     */
    int updatePinned(@Param("conversationId") Long conversationId,
                     @Param("userId") Long userId,
                     @Param("isPinned") Boolean isPinned);

    /**
     * 设置会话免打扰状态
     *
     * @param conversationId 会话ID
     * @param userId 用户ID
     * @param isMuted 是否免打扰
     * @return 更新行数
     */
    int updateMuted(@Param("conversationId") Long conversationId,
                    @Param("userId") Long userId,
                    @Param("isMuted") Boolean isMuted);

    /**
     * 标记会话为已删除（用户退出会话）
     *
     * @param conversationId 会话ID
     * @param userId 用户ID
     * @return 更新行数
     */
    int markAsDeleted(@Param("conversationId") Long conversationId,
                      @Param("userId") Long userId);

    /**
     * 删除会话成员关系
     *
     * @param conversationId 会话ID
     * @param userId 用户ID
     * @return 删除行数
     */
    int deleteByConversationIdAndUserId(@Param("conversationId") Long conversationId,
                                        @Param("userId") Long userId);

    /**
     * 更新置顶状态
     *
     * @param id 会话成员ID
     * @param isPinned 是否置顶
     * @return 更新行数
     */
    int updateIsPinned(@Param("id") Long id, @Param("isPinned") Boolean isPinned);

    /**
     * 更新免打扰状态
     *
     * @param id 会话成员ID
     * @param isMuted 是否免打扰
     * @return 更新行数
     */
    int updateIsMuted(@Param("id") Long id, @Param("isMuted") Boolean isMuted);
}