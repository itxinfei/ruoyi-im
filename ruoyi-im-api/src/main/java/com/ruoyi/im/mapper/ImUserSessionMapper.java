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

