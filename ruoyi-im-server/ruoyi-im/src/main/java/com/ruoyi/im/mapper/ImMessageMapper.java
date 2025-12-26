package com.ruoyi.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.im.domain.ImMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * 消息Mapper接口
 * 
 * @author ruoyi
 * @date 2024-01-01
 */
@Mapper
public interface ImMessageMapper extends BaseMapper<ImMessage> {

    /**
     * 查询会话消息列表（包含发送者详细信息）
     * 
     * @param conversationId 会话ID
     * @param lastMessageId 最后消息ID（用于分页）
     * @param limit 限制数量
     * @return 消息列表
     */
    List<ImMessage> selectConversationMessagesWithDetails(@Param("conversationId") Long conversationId, 
                                                         @Param("lastMessageId") Long lastMessageId, 
                                                         @Param("limit") Integer limit);

    /**
     * 查询消息详细信息（包含发送者和回复消息信息）
     * 
     * @param messageId 消息ID
     * @return 消息详细信息
     */
    ImMessage selectMessageWithDetails(@Param("messageId") Long messageId);

    /**
     * 统计会话消息数量
     * 
     * @param conversationId 会话ID
     * @return 消息数量
     */
    int countConversationMessages(@Param("conversationId") Long conversationId);

    /**
     * 统计用户未读消息数量
     * 
     * @param conversationId 会话ID
     * @param userId 用户ID
     * @param lastReadMessageId 最后已读消息ID
     * @return 未读消息数量
     */
    int countUnreadMessages(@Param("conversationId") Long conversationId, 
                           @Param("userId") Long userId, 
                           @Param("lastReadMessageId") Long lastReadMessageId);

    /**
     * 查询会话最后一条消息
     * 
     * @param conversationId 会话ID
     * @return 最后一条消息
     */
    ImMessage selectLastMessage(@Param("conversationId") Long conversationId);

    /**
     * 搜索消息（根据内容）
     * 
     * @param conversationId 会话ID
     * @param keyword 搜索关键词
     * @param limit 限制数量
     * @return 消息列表
     */
    List<ImMessage> searchMessages(@Param("conversationId") Long conversationId, 
                                  @Param("keyword") String keyword, 
                                  @Param("limit") Integer limit);

    /**
     * 查询用户发送的消息列表
     * 
     * @param senderId 发送者用户ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param limit 限制数量
     * @return 消息列表
     */
    List<ImMessage> selectUserMessages(@Param("senderId") Long senderId, 
                                      @Param("startTime") Date startTime, 
                                      @Param("endTime") Date endTime, 
                                      @Param("limit") Integer limit);

    /**
     * 批量更新消息状态
     * 
     * @param messageIds 消息ID列表
     * @param status 新状态
     * @return 更新数量
     */
    int updateStatusBatch(@Param("messageIds") List<Long> messageIds, @Param("status") String status);

    /**
     * 撤回消息
     * 
     * @param messageId 消息ID
     * @param revokedTime 撤回时间
     * @return 更新数量
     */
    int revokeMessage(@Param("messageId") Long messageId, @Param("revokedTime") Date revokedTime);

    /**
     * 查询敏感消息列表
     * 
     * @param sensitiveLevel 敏感级别
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param limit 限制数量
     * @return 消息列表
     */
    List<ImMessage> selectSensitiveMessages(@Param("sensitiveLevel") String sensitiveLevel, 
                                           @Param("startTime") Date startTime, 
                                           @Param("endTime") Date endTime, 
                                           @Param("limit") Integer limit);

    /**
     * 查询文件消息列表
     * 
     * @param conversationId 会话ID
     * @param limit 限制数量
     * @return 消息列表
     */
    List<ImMessage> selectFileMessages(@Param("conversationId") Long conversationId, @Param("limit") Integer limit);

    /**
     * 统计用户发送消息数量
     * 
     * @param senderId 发送者用户ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 消息数量
     */
    int countUserSentMessages(@Param("senderId") Long senderId, 
                             @Param("startTime") Date startTime, 
                             @Param("endTime") Date endTime);

    /**
     * 删除过期消息（超过指定天数的消息）
     * 
     * @param days 天数
     * @return 删除数量
     */
    int deleteExpiredMessages(@Param("days") int days);

    /**
     * 查询消息统计信息（按日期分组）
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计信息列表
     */
    List<Object> selectMessageStatistics(@Param("startTime") Date startTime, @Param("endTime") Date endTime);

    /**
     * 删除会话的所有消息
     * 
     * @param conversationId 会话ID
     * @return 删除数量
     */
    int deleteByConversationId(@Param("conversationId") Long conversationId);
    
    /**
     * 查询会话消息列表（包含发送者详细信息）
     * 
     * @param conversationId 会话ID
     * @param userId 查询用户ID
     * @param lastMessageId 最后消息ID（用于分页）
     * @param limit 限制数量
     * @return 消息列表
     */
    List<ImMessage> selectConversationMessages(@Param("conversationId") Long conversationId, 
                                           @Param("userId") Long userId, 
                                           @Param("lastMessageId") Long lastMessageId, 
                                           @Param("limit") Integer limit);
    
    /**
     * 查询消息详细信息（包含发送者和回复消息信息）
     * 
     * @param messageId 消息ID
     * @param userId 查询用户ID
     * @return 消息详细信息
     */
    ImMessage selectMessageDetail(@Param("messageId") Long messageId, @Param("userId") Long userId);
    
    /**
     * 搜索消息（根据内容）
     * 
     * @param conversationId 会话ID
     * @param userId 用户ID
     * @param keyword 搜索关键词
     * @param messageType 消息类型
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 消息列表
     */
    List<ImMessage> searchMessages(@Param("conversationId") Long conversationId, 
                                  @Param("userId") Long userId, 
                                  @Param("keyword") String keyword, 
                                  @Param("messageType") String messageType, 
                                  @Param("startTime") Date startTime, 
                                  @Param("endTime") Date endTime);
    
    /**
     * 查询用户发送的消息列表
     * 
     * @param senderId 发送者用户ID
     * @param messageType 消息类型
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 消息列表
     */
    List<ImMessage> selectUserSentMessages(@Param("senderId") Long senderId, 
                                         @Param("messageType") String messageType, 
                                         @Param("startTime") Date startTime, 
                                         @Param("endTime") Date endTime);
    
    /**
     * 批量更新消息状态
     * 
     * @param messageIds 消息ID列表
     * @param status 新状态
     * @return 更新数量
     */
    int updateMessageStatusBatch(@Param("messageIds") List<Long> messageIds, @Param("status") String status);
}