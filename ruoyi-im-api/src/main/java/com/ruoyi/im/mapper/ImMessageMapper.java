package com.ruoyi.im.mapper;

import com.ruoyi.im.domain.ImMessage;

import java.util.List;

/**
 * 消息Mapper接口
 *
 * @author ruoyi
 */
public interface ImMessageMapper {

    /**
     * 查询消息
     *
     * @param id 消息ID
     * @return 消息
     */
    ImMessage selectImMessageById(Long id);

    /**
     * 查询消息列表
     *
     * @param imMessage 消息
     * @return 消息集合
     */
    List<ImMessage> selectImMessageList(ImMessage imMessage);

    /**
     * 新增消息
     *
     * @param imMessage 消息
     * @return 结果
     */
    int insertImMessage(ImMessage imMessage);

    /**
     * 修改消息
     *
     * @param imMessage 消息
     * @return 结果
     */
    int updateImMessage(ImMessage imMessage);

    /**
     * 删除消息
     *
     * @param id 消息ID
     * @return 结果
     */
    int deleteImMessageById(Long id);

    /**
     * 批量删除消息
     *
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    int deleteImMessageByIds(Long[] ids);

    /**
     * 根据会话ID查询消息列表
     *
     * @param conversationId 会话ID
     * @return 消息集合
     */
    List<ImMessage> selectImMessageListByConversationId(Long conversationId);

    /**
     * 根据会话ID和时间范围查询消息列表
     *
     * @param conversationId 会话ID
     * @param startTime      开始时间
     * @param endTime        结束时间
     * @return 消息集合
     */
    List<ImMessage> selectImMessageListByConversationIdAndTimeRange(Long conversationId,
                                                                     java.time.LocalDateTime startTime,
                                                                     java.time.LocalDateTime endTime);
}
