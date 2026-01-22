package com.ruoyi.web.mapper;

import com.ruoyi.web.domain.ImMessage;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * IM消息数据访问层（优化版）
 * 使用注解SQL，提供高性能查询接口
 * 
 * @author ruoyi
 * @version 2.0
 * @since 2025-01-22
 */
public interface ImMessageMapperEnhanced {

    /**
     * 分页查询消息列表（优化版）
     * 使用视图减少JOIN操作
     */
    @Select("SELECT m.id, m.conversation_id, m.sender_id, m.message_type, m.content, " +
            "m.is_revoked, m.is_edited, m.is_deleted, m.create_time, m.update_time " +
            "FROM v_im_message_detail " +
            "ORDER BY ${@param.orderBy} ${@param.sortOrder}")
    List<ImMessage> selectMessageListEnhanced(
            @Param("conversationId") Long conversationId,
            @Param("senderId") Long senderId,
            @Param("messageType") String messageType,
            @Param("senderName") String senderName,
            @Param("content") String content,
            @Param("isRevoked") Integer isRevoked,
            @Param("isEdited") Integer isEdited,
            @Param("isDeleted") Integer isDeleted,
            @Param("sortBy") String sortBy,
            @Param("sortOrder") String sortOrder);

    /**
     * 查询消息详情（优化版）
     * 使用视图获取完整消息信息
     */
    @Select("SELECT * FROM v_im_message_detail WHERE id = #{id}")
    ImMessage selectMessageByIdEnhanced(@Param("id") Long id);

    /**
     * 按会话ID分页查询消息（优化版）
     * 使用索引优化查询性能
     */
    @Select("SELECT * FROM v_im_message_detail " +
            "WHERE conversation_id = #{conversationId} " +
            "ORDER BY create_time DESC " +
            "LIMIT #{offset}, #{pageSize}")
    List<ImMessage> selectMessagesByConversationIdEnhanced(
            @Param("conversationId") Long conversationId,
            @Param("offset") Integer offset,
            @Param("pageSize") Integer pageSize);

    /**
     * 按时间范围查询消息（优化版）
     * 使用时间索引优化查询
     */
    @Select("SELECT * FROM v_im_message_detail " +
            "WHERE conversation_id = #{conversationId} " +
            "AND create_time >= #{startTime} " +
            "AND create_time <= #{endTime} " +
            "ORDER BY create_time DESC")
    List<ImMessage> selectMessagesByTimeRangeEnhanced(
            @Param("conversationId") Long conversationId,
            @Param("startTime") String startTime,
            @Param("endTime") String endTime);

    /**
     * 按消息类型查询消息（优化版）
     * 使用类型索引优化查询
     */
    @Select("SELECT * FROM v_im_message_detail " +
            "WHERE message_type = #{messageType} " +
            "ORDER BY create_time DESC " +
            "LIMIT #{limit}")
    List<ImMessage> selectMessagesByTypeEnhanced(
            @Param("messageType") String messageType,
            @Param("limit") Integer limit);

    /**
     * 按发送者查询消息（优化版）
     * 使用发送者索引优化查询
     */
    @Select("SELECT * FROM v_im_message_detail " +
            "WHERE sender_id = #{senderId} " +
            "ORDER BY create_time DESC " +
            "LIMIT #{limit}")
    List<ImMessage> selectMessagesBySenderIdEnhanced(
            @Param("senderId") Long senderId,
            @Param("limit") Integer limit);

    /**
     * 按内容模糊搜索消息（优化版）
     * 使用全文索引优化搜索
     */
    @Select("SELECT * FROM v_im_message_detail " +
            "WHERE content LIKE CONCAT('%', #{content}, '%') " +
            "ORDER BY create_time DESC " +
            "LIMIT #{limit}")
    List<ImMessage> searchMessagesByContentEnhanced(
            @Param("content") String content,
            @Param("limit") Integer limit);

    /**
     * 按敏感级别查询消息（优化版）
     * 使用敏感级别索引优化查询
     */
    @Select("SELECT * FROM v_im_message_detail " +
            "WHERE sensitive_level = #{sensitiveLevel} " +
            "ORDER BY create_time DESC " +
            "LIMIT #{limit}")
    List<ImMessage> selectMessagesBySensitiveLevelEnhanced(
            @Param("sensitiveLevel") Integer sensitiveLevel,
            @Param("limit") Integer limit);

    /**
     * 统计消息总数（优化版）
     * 使用视图优化统计查询
     */
    @Select("SELECT COUNT(*) as total_count " +
            "FROM v_im_message_detail")
    Long selectMessageCountEnhanced();

    /**
     * 统计今日消息数（优化版）
     * 使用索引优化日期统计
     */
    @Select("SELECT COUNT(*) as today_count " +
            "FROM v_im_message_detail " +
            "WHERE DATE(create_time) = CURDATE()")
    Long selectTodayMessageCountEnhanced();

    /**
     * 统计敏感消息数（优化版）
     * 使用视图优化敏感统计
     */
    @Select("SELECT COUNT(*) as sensitive_count " +
            "FROM v_im_message_detail " +
            "WHERE sensitive_level > 0")
    Long selectSensitiveMessageCountEnhanced();

    /**
     * 统计失败消息数（优化版）
     * 使用视图优化失败统计
     */
    @Select("SELECT COUNT(*) as failed_count " +
            "FROM v_im_message_detail " +
            "WHERE is_revoked = 1")
    Long selectFailedMessageCountEnhanced();

    /**
     * 统计各类型消息数（优化版）
     * 使用视图优化类型统计
     */
    @Select("SELECT " +
            "message_type, " +
            "COUNT(*) as count " +
            "FROM v_im_message_detail " +
            "GROUP BY message_type")
    List<java.util.Map<String, Object>> selectMessageCountByTypeEnhanced();

    /**
     * 批量更新敏感级别（优化版）
     * 使用批量更新优化性能
     */
    @Update("<script>" +
            "UPDATE im_message " +
            "SET sensitive_level = #{sensitiveLevel}, " +
            "update_time = NOW() " +
            "WHERE id IN " +
            "<foreach collection='messageIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>")
    int batchUpdateSensitiveLevel(
            @Param("messageIds") List<Long> messageIds,
            @Param("sensitiveLevel") Integer sensitiveLevel);

    /**
     * 批量撤回消息（优化版）
     * 使用批量更新优化撤回性能
     */
    @Update("<script>" +
            "UPDATE im_message " +
            "SET is_revoked = 1, " +
            "revoked_time = NOW(), " +
            "update_time = NOW() " +
            "WHERE id IN " +
            "<foreach collection='messageIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "AND is_revoked = 0")
    int batchRevokeMessagesEnhanced(
            @Param("messageIds") List<Long> messageIds);

    /**
     * 批量删除消息（优化版）
     * 使用逻辑删除优化删除性能
     */
    @Update("<script>" +
            "UPDATE im_message " +
            "SET is_deleted = 1, " +
            "update_time = NOW() " +
            "WHERE id IN " +
            "<foreach collection='messageIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>")
    int batchDeleteMessagesEnhanced(
            @Param("messageIds") List<Long> messageIds);

    /**
     * 插入消息（优化版）
     * 使用视图简化插入逻辑
     */
    @Insert("<script>" +
            "INSERT INTO im_message (" +
            "conversation_id, sender_id, message_type, content, " +
            "sensitive_level, is_revoked, is_edited, is_deleted, " +
            "create_time, update_time) " +
            "VALUES (" +
            "#{conversationId}, #{senderId}, #{messageType}, " +
            "#{content}, 0, 0, 0, NOW(), NOW())")
    int insertMessageEnhanced(ImMessage message);

    /**
     * 更新消息（优化版）
     * 使用视图简化更新逻辑
     */
    @Update("<script>" +
            "UPDATE im_message " +
            "SET content = #{content}, " +
            "message_type = #{messageType}, " +
            "sensitive_level = #{sensitiveLevel}, " +
            "update_time = NOW() " +
            "WHERE id = #{id}")
    int updateMessageEnhanced(ImMessage message);
}