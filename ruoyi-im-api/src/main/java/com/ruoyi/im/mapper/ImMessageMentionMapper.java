package com.ruoyi.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.im.domain.ImMessageMention;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 消息@提及Mapper接口
 *
 * @author ruoyi
 */
@Mapper
public interface ImMessageMentionMapper extends BaseMapper<ImMessageMention> {

    /**
     * 查询消息的所有@提及
     *
     * @param messageId 消息ID
     * @return @提及列表
     */
    List<ImMessageMention> selectMentionsByMessageId(@Param("messageId") Long messageId);

    /**
     * 查询用户收到的未读@提及
     *
     * @param userId 用户ID
     * @return 未读@提及列表
     */
    List<ImMessageMention> selectUnreadMentions(@Param("userId") Long userId);

    /**
     * 查询用户收到的@提及数量
     *
     * @param userId 用户ID
     * @return 数量
     */
    int countUnreadMentions(@Param("userId") Long userId);

    /**
     * 标记@提及为已读
     *
     * @param messageId 消息ID
     * @param userId 用户ID
     * @return 更新行数
     */
    int markAsRead(@Param("messageId") Long messageId, @Param("userId") Long userId);

    /**
     * 批量标记@提及为已读
     *
     * @param mentionIds 提及ID列表
     * @return 更新行数
     */
    int batchMarkAsRead(@Param("mentionIds") List<Long> mentionIds);

    /**
     * 查询会话中的@提及
     *
     * @param conversationId 会话ID
     * @param userId 用户ID
     * @return @提及列表
     */
    List<ImMessageMention> selectMentionsByConversation(@Param("conversationId") Long conversationId,
                                                        @Param("userId") Long userId);
}
