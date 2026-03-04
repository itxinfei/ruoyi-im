package com.ruoyi.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.im.domain.ImUserSession;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户会话状态 Mapper
 */
@Mapper
public interface ImUserSessionMapper extends BaseMapper<ImUserSession> {

    ImUserSession selectByUserIdAndConversationId(@Param("userId") Long userId,
                                                  @Param("conversationId") Long conversationId);

    List<ImUserSession> selectByUserId(@Param("userId") Long userId);

    int updateUnreadCount(@Param("userId") Long userId,
                          @Param("conversationId") Long conversationId,
                          @Param("unreadCount") Integer unreadCount);

    /**
     * 原子性地增加未读消息数
     *
     * @param userId 用户ID
     * @param conversationId 会话ID
     * @param count 增加的数量
     * @return 更新行数
     */
    int incrementUnreadCount(@Param("userId") Long userId,
                             @Param("conversationId") Long conversationId,
                             @Param("count") Integer count);

    /**
     * 原子性地减少未读消息数（不会小于0）
     *
     * @param userId 用户ID
     * @param conversationId 会话ID
     * @param count 减少的数量
     * @return 更新行数
     */
    int decrementUnreadCount(@Param("userId") Long userId,
                             @Param("conversationId") Long conversationId,
                             @Param("count") Integer count);

    int updateLastReadMessage(@Param("userId") Long userId,
                              @Param("conversationId") Long conversationId,
                              @Param("lastReadMessageId") Long lastReadMessageId);

    int updatePinned(@Param("userId") Long userId,
                     @Param("conversationId") Long conversationId,
                     @Param("isPinned") Integer isPinned);

    int updateMuted(@Param("userId") Long userId,
                    @Param("conversationId") Long conversationId,
                    @Param("isMuted") Integer isMuted);

    int updateArchived(@Param("userId") Long userId,
                       @Param("conversationId") Long conversationId,
                       @Param("isArchived") Integer isArchived);

    int updateDraft(@Param("userId") Long userId,
                    @Param("conversationId") Long conversationId,
                    @Param("draftContent") String draftContent);

    int markDeleted(@Param("userId") Long userId,
                    @Param("conversationId") Long conversationId);
}

