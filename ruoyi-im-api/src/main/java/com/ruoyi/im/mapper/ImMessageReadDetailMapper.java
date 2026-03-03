package com.ruoyi.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.im.domain.ImMessageReadDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 消息已读详情 Mapper
 */
@Mapper
public interface ImMessageReadDetailMapper extends BaseMapper<ImMessageReadDetail> {

    ImMessageReadDetail selectByMessageIdAndUserId(@Param("messageId") Long messageId,
                                                   @Param("userId") Long userId);

    List<ImMessageReadDetail> selectByMessageId(@Param("messageId") Long messageId);

    int upsertReadDetail(@Param("messageId") Long messageId,
                         @Param("conversationId") Long conversationId,
                         @Param("userId") Long userId);
}

