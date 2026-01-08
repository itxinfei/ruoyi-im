package com.ruoyi.im.mapper;

import com.ruoyi.im.domain.ImMessage;
import org.apache.ibatis.annotations.Param;

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
     * 获取会话最后一条消息
     *
     * @param conversationId 会话ID
     * @return 最后一条消息
     */
    ImMessage selectLastMessageByConversationId(@Param("conversationId") Long conversationId);

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

    // ==================== 消息搜索相关方法 ====================

    /**
     * 搜索消息（关键词搜索）
     *
     * @param conversationId 会话ID（可选）
     * @param keyword        搜索关键词
     * @param messageType    消息类型（可选）
     * @param senderId       发送者ID（可选）
     * @param startTime      开始时间（可选）
     * @param endTime        结束时间（可选）
     * @param includeRevoked 是否包含撤回的消息
     * @param exactMatch     是否精确匹配
     * @param offset         偏移量
     * @param limit          限制数量
     * @return 消息集合
     */
    List<ImMessage> searchMessages(@Param("conversationId") Long conversationId,
                                    @Param("keyword") String keyword,
                                    @Param("messageType") String messageType,
                                    @Param("senderId") Long senderId,
                                    @Param("startTime") java.time.LocalDateTime startTime,
                                    @Param("endTime") java.time.LocalDateTime endTime,
                                    @Param("includeRevoked") Boolean includeRevoked,
                                    @Param("exactMatch") Boolean exactMatch,
                                    @Param("offset") Integer offset,
                                    @Param("limit") Integer limit);

    /**
     * 统计搜索结果数量
     *
     * @param conversationId 会话ID（可选）
     * @param keyword        搜索关键词
     * @param messageType    消息类型（可选）
     * @param senderId       发送者ID（可选）
     * @param startTime      开始时间（可选）
     * @param endTime        结束时间（可选）
     * @param includeRevoked 是否包含撤回的消息
     * @param exactMatch     是否精确匹配
     * @return 结果数量
     */
    int countSearchResults(@Param("conversationId") Long conversationId,
                           @Param("keyword") String keyword,
                           @Param("messageType") String messageType,
                           @Param("senderId") Long senderId,
                           @Param("startTime") java.time.LocalDateTime startTime,
                           @Param("endTime") java.time.LocalDateTime endTime,
                           @Param("includeRevoked") Boolean includeRevoked,
                           @Param("exactMatch") Boolean exactMatch);

    /**
     * 获取搜索结果的高亮上下文
     * 返回包含关键词的上下文片段
     *
     * @param messageId 消息ID
     * @param keyword   关键词
     * @param contextLength 上下文长度
     * @return 高亮上下文
     */
    String getHighlightContext(@Param("messageId") Long messageId,
                               @Param("keyword") String keyword,
                               @Param("contextLength") Integer contextLength);
}
