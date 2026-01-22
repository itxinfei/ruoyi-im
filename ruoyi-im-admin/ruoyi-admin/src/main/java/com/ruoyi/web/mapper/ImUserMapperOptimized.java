package com.ruoyi.web.mapper;

import com.ruoyi.web.domain.ImUser;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

/**
 * IM用户数据访问层（优化版）
 * 
 * @author ruoyi
 * @version 1.0
 * @since 2025-01-22
 */
public interface ImUserMapperOptimized {

    @Select("SELECT * FROM v_im_user_stats WHERE username LIKE CONCAT('%', #{username}, '%') ORDER BY create_time DESC")
    List<ImUser> selectUsersByUsernameOptimized(@Param("username") String username);

    @Select("SELECT * FROM v_im_user_stats WHERE status = #{status} ORDER BY last_online_time DESC")
    List<ImUser> selectUsersByStatusOptimized(@Param("status") Integer status);

    @Select("SELECT * FROM v_im_user_stats WHERE is_online = #{isOnline} ORDER BY last_online_time DESC")
    List<ImUser> selectUsersByOnlineStatusOptimized(@Param("isOnline") Integer isOnline);

    @Select("SELECT COUNT(*) FROM v_im_user_stats WHERE status = 0")
    int selectActiveUserCount();

    @Select("SELECT COUNT(*) FROM v_im_user_stats WHERE is_online = 1")
    int selectOnlineUserCount();

    @Select("SELECT COUNT(*) FROM v_im_user_stats WHERE DATE(create_time) = CURDATE()")
    int selectTodayRegisteredUserCount();

    @Update("UPDATE im_user SET password = #{newPassword}, update_time = NOW() WHERE id = #{userId}")
    int resetPasswordOptimized(@Param("userId") Long userId, @Param("newPassword") String newPassword);

    @Update("UPDATE im_user SET status = #{status}, update_time = NOW() WHERE id IN " +
            "<foreach collection='userIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>")
    int batchUpdateUserStatusOptimized(@Param("userIds") String[] userIds, @Param("status") Integer status);

    @Update("UPDATE im_user SET password = #{newPassword}, update_time = NOW() WHERE id IN " +
            "<foreach collection='userIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>")
    int batchResetPasswordsOptimized(@Param("userIds") String[] userIds, @Param("newPassword") String newPassword);

    @Select("SELECT * FROM v_im_user_stats WHERE dept_id = #{deptId} ORDER BY create_time DESC")
    List<ImUser> selectUsersByDeptIdOptimized(@Param("deptId") Long deptId);

    @Select("SELECT * FROM v_im_user_stats WHERE " +
            "(username LIKE CONCAT('%', #{keyword}, '%') OR " +
            "nickname LIKE CONCAT('%', #{keyword}, '%') OR " +
            "email LIKE CONCAT('%', #{keyword}, '%') OR " +
            "mobile LIKE CONCAT('%', #{keyword}, '%')) " +
            "ORDER BY create_time DESC LIMIT #{limit}")
    List<ImUser> searchUsersOptimized(@Param("keyword") String keyword, @Param("limit") Integer limit);

    @Select({
        "SELECT COUNT(*) as total_count,",
        "       SUM(CASE WHEN status = 0 THEN 1 ELSE 0 END) as active_count,",
        "       SUM(CASE WHEN status = 1 THEN 1 ELSE 0 END) as disabled_count,",
        "       SUM(CASE WHEN is_online = 1 THEN 1 ELSE 0 END) as online_count,",
        "       SUM(CASE WHEN DATE(create_time) = CURDATE() THEN 1 ELSE 0 END) as today_registered_count",
        "       SUM(CASE WHEN DATE(last_online_time) = CURDATE() THEN 1 ELSE 0 END) as today_active_count",
        "       AVG(today_message_count) as avg_daily_messages",
        "       MAX(today_message_count) as max_daily_messages",
        "       AVG(sensitive_message_count) as avg_sensitive_messages",
        "       SUM(CASE WHEN conversation_count > 0 THEN 1 ELSE 0 END) as users_with_conversations",
        "       AVG(conversation_count) as avg_conversations_per_user",
        "FROM v_im_user_stats"
    })
    Map<String, Object> selectUserStatisticsOptimized();

    @Update("UPDATE im_user SET last_online_time = NULL WHERE id = #{userId}")
    int forceUserLogoutOptimized(@Param("userId") Long userId);

    @Select("SELECT * FROM v_im_user_stats WHERE id IN " +
            "<foreach collection='userIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>")
    List<ImUser> selectUsersByIdsOptimized(@Param("userIds") List<Long> userIds);

    @Update("DELETE FROM user_online WHERE user_id = #{userId}")
    int clearUserOnlineCacheOptimized(@Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM im_conversation_member WHERE user_id = #{userId}")
    int selectConversationCountByUserId(@Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM im_friend WHERE user_id = #{userId} OR friend_id = #{userId}")
    int selectFriendCountByUserId(@Param("userId") Long userId);
}