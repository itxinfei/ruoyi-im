package com.ruoyi.im.mapper;

import com.ruoyi.im.domain.ImMessageRead;

import java.util.List;

/**
 * 消息已读回执Mapper接口
 *
 * @author ruoyi
 */
public interface ImMessageReadMapper {

    /**
     * 查询消息已读回执
     *
     * @param id 回执ID
     * @return 消息已读回执
     */
    ImMessageRead selectImMessageReadById(Long id);

    /**
     * 根据消息ID和用户ID查询已读回执
     *
     * @param messageId 消息ID
     * @param userId    用户ID
     * @return 消息已读回执
     */
    ImMessageRead selectByMessageIdAndUserId(Long messageId, Long userId);

    /**
     * 查询消息已读回执列表
     *
     * @param imMessageRead 消息已读回执
     * @return 消息已读回执集合
     */
    List<ImMessageRead> selectImMessageReadList(ImMessageRead imMessageRead);

    /**
     * 根据消息ID查询已读用户列表
     *
     * @param messageId 消息ID
     * @return 已读用户ID列表
     */
    List<Long> selectReadUserIdsByMessageId(Long messageId);

    /**
     * 根据会话ID和用户ID查询已读消息列表
     *
     * @param conversationId 会话ID
     * @param userId         用户ID
     * @return 消息已读回执集合
     */
    List<ImMessageRead> selectByConversationIdAndUserId(Long conversationId, Long userId);

    /**
     * 查询会话中所有消息的已读回执
     *
     * @param conversationId 会话ID
     * @return 消息已读回执集合
     */
    List<ImMessageRead> selectByConversationId(Long conversationId);

    /**
     * 新增消息已读回执
     *
     * @param imMessageRead 消息已读回执
     * @return 结果
     */
    int insertImMessageRead(ImMessageRead imMessageRead);

    /**
     * 批量新增消息已读回执
     *
     * @param list 消息已读回执集合
     * @return 结果
     */
    int batchInsertImMessageRead(List<ImMessageRead> list);

    /**
     * 删除消息已读回执
     *
     * @param id 回执ID
     * @return 结果
     */
    int deleteImMessageReadById(Long id);

    /**
     * 根据消息ID删除已读回执
     *
     * @param messageId 消息ID
     * @return 结果
     */
    int deleteImMessageReadByMessageId(Long messageId);

    /**
     * 统计消息已读人数
     *
     * @param messageId 消息ID
     * @return 已读人数
     */
    int countReadUsersByMessageId(Long messageId);

    /**
     * 批量获取消息的已读用户ID列表
     *
     * @param messageIds 消息ID列表
     * @return Map<messageId, 已读用户ID列表>
     */
    java.util.Map<Long, List<Long>> batchSelectReadUserIds(java.util.List<Long> messageIds);
}
