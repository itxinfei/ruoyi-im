package com.ruoyi.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.im.domain.ImTypingStatus;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 输入状态 Mapper
 */
@Mapper
public interface ImTypingStatusMapper extends BaseMapper<ImTypingStatus> {

    ImTypingStatus selectByConversationIdAndUserId(@Param("conversationId") Long conversationId,
                                                   @Param("userId") Long userId);

    List<ImTypingStatus> selectByConversationId(@Param("conversationId") Long conversationId);

    int upsertTypingStatus(@Param("conversationId") Long conversationId,
                           @Param("userId") Long userId,
                           @Param("typingType") String typingType);

    int deleteByConversationIdAndUserId(@Param("conversationId") Long conversationId,
                                        @Param("userId") Long userId);
}

