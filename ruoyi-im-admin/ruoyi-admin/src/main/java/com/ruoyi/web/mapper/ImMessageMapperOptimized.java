package com.ruoyi.web.mapper;

import com.ruoyi.web.domain.ImMessage;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

/**
 * IM消息数据访问层（优化版）
 * 使用视图和优化查询提升性能
 * 
 * @author ruoyi
 * @version 1.0
 * @since 2025-01-22
 */
public interface ImMessageMapperOptimized {

    @Select("SELECT * FROM v_im_message_detail WHERE conversation_id = #{conversationId} ORDER BY create_time DESC")
    List<ImMessage> selectMessagesByConversationIdOptimized(@Param("conversationId") Long conversationId);

    @Select("SELECT * FROM v_im_message_detail WHERE sender_id = #{senderId} ORDER BY create_time DESC LIMIT #{limit}")
    List<ImMessage> selectRecentMessagesBySenderIdOptimized(@Param("senderId") Long senderId, @Param("limit") Integer limit);

    @Select("SELECT COUNT(*) FROM v_im_message_detail WHERE conversation_id = #{conversationId} AND DATE(create_time) = CURDATE()")
    int selectTodayMessageCountByConversationId(@Param("conversationId") Long conversationId);

    @Select("SELECT COUNT(*) FROM v_im_message_detail WHERE sender_id = #{senderId} AND DATE(create_time) = CURDATE()")
    int selectTodayMessageCountBySenderId(@Param("senderId") Long senderId);

    @Select("SELECT COUNT(*) FROM v_im_message_detail WHERE sensitive_level > 0 AND DATE(create_time) = CURDATE()")
    int selectTodaySensitiveMessageCount();

    @Select({
        "SELECT COUNT(*) as total_count,",
        "       SUM(CASE WHEN DATE(create_time) = CURDATE() THEN 1 ELSE 0 END) as today_count,",
        "       SUM(CASE WHEN sensitive_level > 0 THEN 1 ELSE 0 END) as sensitive_count,",
        "       SUM(CASE WHEN is_revoked = 1 THEN 1 ELSE 0 END) as failed_count",
        "FROM v_im_message_detail"
    })
    Map<String, Object> selectMessageStatisticsOptimized();

    @Select({
        "SELECT COUNT(*) as total_count,",
        "       SUM(CASE WHEN sensitive_level = 0 THEN 1 ELSE 0 END) as normal_count,",
        "       SUM(CASE WHEN sensitive_level = 1 THEN 1 ELSE 0 END) as sensitive_count,",
        "       SUM(CASE WHEN sensitive_level = 2 THEN 1 ELSE 0 END) as high_count",
        "FROM v_im_message_detail"
    })
    Map<String, Object> selectSensitiveMessageStatisticsOptimized();

    @Update("UPDATE im_message SET sensitive_level = #{sensitiveLevel}, update_time = NOW() WHERE id IN " +
            "<foreach collection='messageIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>")
    int batchUpdateSensitiveLevelOptimized(@Param("messageIds") List<Long> messageIds, @Param("sensitiveLevel") Integer sensitiveLevel);

    @Update("UPDATE im_message SET is_revoked = 1, revoked_time = NOW() WHERE id = #{messageId} AND is_revoked = 0")
    int revokeMessageOptimized(@Param("messageId") Long messageId);

    @Update("UPDATE im_message SET is_revoked = 1, revoked_time = NOW() WHERE id IN " +
            "<foreach collection='messageIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach> AND is_revoked = 0")
    int batchRevokeMessagesOptimized(@Param("messageIds") List<Long> messageIds);

    @Select("SELECT * FROM v_im_message_detail WHERE conversation_id = #{conversationId} " +
            "AND create_time >= #{startTime} AND create_time <= #{endTime} ORDER BY create_time DESC")
    List<ImMessage> selectMessagesByTimeRangeOptimized(@Param("conversationId") Long conversationId,
                                                   @Param("startTime") String startTime,
                                                   @Param("endTime") String endTime);

    @Select("SELECT * FROM v_im_message_detail WHERE content LIKE CONCAT('%', #{keyword}, '%') " +
            "OR sender_name LIKE CONCAT('%', #{keyword}, '%') " +
            "ORDER BY create_time DESC LIMIT #{limit}")
    List<ImMessage> searchMessagesOptimized(@Param("keyword") String keyword, @Param("limit") Integer limit);

    @Select("SELECT * FROM im_message WHERE conversation_id = #{conversationId} " +
            "AND create_time >= DATE_SUB(NOW(), INTERVAL 7 DAY) " +
            "ORDER BY create_time DESC")
    List<ImMessage> selectRecentMessagesByConversationId(@Param("conversationId") Long conversationId);

    @Update("UPDATE im_message SET is_deleted = 1, update_time = NOW() WHERE id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>")
    int batchDeleteMessagesOptimized(@Param("ids") List<Long> ids);
}