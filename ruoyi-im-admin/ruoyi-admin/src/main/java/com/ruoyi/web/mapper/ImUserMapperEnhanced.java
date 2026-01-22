package com.ruoyi.web.mapper;

import com.ruoyi.web.domain.ImUser;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * IM用户数据访问层（优化版）
 * 使用注解SQL，提供高性能查询接口
 * 
 * @author ruoyi
 * @version 2.0
 * @since 2025-01-22
 */
public interface ImUserMapperEnhanced {

    /**
     * 分页查询用户列表（优化版）
     * 使用视图优化查询性能
     */
    @Select("SELECT u.id, u.username, u.nickname, u.email, u.mobile, u.avatar, " +
            "u.gender, u.signature, u.status, u.create_time, u.update_time, " +
            "u.last_online_time, d.name as dept_name " +
            "FROM v_im_user_stats u " +
            "LEFT JOIN sys_dept d ON u.dept_id = d.dept_id " +
            "ORDER BY ${@param.sortBy} ${@param.sortOrder}")
    List<ImUser> selectUserListEnhanced(
            @Param("username") String username,
            @Param("nickname") String nickname,
            @Param("email") String email,
            @Param("mobile") String mobile,
            @Param("deptId") Long deptId,
            @Param("status") Integer status,
            @Param("sortBy") String sortBy,
            @Param("sortOrder") String sortOrder);

    /**
     * 根据ID查询用户（优化版）
     * 使用主键查询
     */
    @Select("SELECT * FROM v_im_user_stats WHERE id = #{id}")
    ImUser selectUserByIdEnhanced(@Param("id") Long id);

    /**
     * 按用户名查询用户（优化版）
     * 使用用户名索引
     */
    @Select("SELECT * FROM v_im_user_stats " +
            "WHERE username LIKE CONCAT('%', #{username}, '%') " +
            "ORDER BY create_time DESC " +
            "LIMIT #{limit}")
    List<ImUser> selectUsersByUsernameEnhanced(
            @Param("username") String username,
            @Param("limit") Integer limit);

    /**
     * 按手机号查询用户（优化版）
     * 使用手机号索引
     */
    @Select("SELECT * FROM v_im_user_stats " +
            "WHERE mobile LIKE CONCAT('%', #{mobile}, '%') " +
            "ORDER BY create_time DESC " +
            "LIMIT #{limit}")
    List<ImUser> selectUsersByMobileEnhanced(
            @Param("mobile") String mobile,
            @Param("limit") Integer limit);

    /**
     * 按部门查询用户（优化版）
     * 使用部门索引
     */
    @Select("SELECT * FROM v_im_user_stats " +
            "WHERE dept_id = #{deptId} " +
            "ORDER BY create_time DESC " +
            "LIMIT #{limit}")
    List<ImUser> selectUsersByDeptIdEnhanced(
            @Param("deptId") Long deptId,
            @Param("limit") Integer limit);

    /**
     * 按状态查询用户（优化版）
     * 使用状态索引
     */
    @Select("SELECT * FROM v_im_user_stats " +
            "WHERE status = #{status} " +
            "ORDER BY create_time DESC " +
            "LIMIT #{limit}")
    List<ImUser> selectUsersByStatusEnhanced(
            @Param("status") Integer status,
            @Param("limit") Integer limit);

    /**
     * 按在线状态查询用户（优化版）
     * 使用在线状态索引
     */
    @Select("SELECT * FROM v_im_user_stats " +
            "WHERE is_online = #{isOnline} " +
            "ORDER BY last_online_time DESC " +
            "LIMIT #{limit}")
    List<ImUser> selectUsersByOnlineStatusEnhanced(
            @Param("isOnline") Integer isOnline,
            @Param("limit") Integer limit);

    /**
     * 统计用户总数（优化版）
     * 使用视图优化统计
     */
    @Select("SELECT COUNT(*) as total_count FROM v_im_user_stats")
    Long selectUserCountEnhanced();

    /**
     * 统计在线用户数（优化版）
     * 使用视图优化在线统计
     */
    @Select("SELECT COUNT(*) as online_count FROM v_im_user_stats WHERE is_online = 1")
    Long selectOnlineUserCountEnhanced();

    /**
     * 统计今日注册用户数（优化版）
     * 使用日期索引优化统计
     */
    @Select("SELECT COUNT(*) as today_registered_count FROM v_im_user_stats " +
            "WHERE DATE(create_time) = CURDATE()")
    Long selectTodayRegisteredUserCountEnhanced();

    /**
     * 统计部门用户分布（优化版）
     * 使用视图优化部门统计
     */
    @Select("SELECT " +
            "d.dept_id, d.name as dept_name, " +
            "COUNT(*) as user_count " +
            "FROM v_im_user_stats u " +
            "LEFT JOIN sys_dept d ON u.dept_id = d.dept_id " +
            "GROUP BY d.dept_id, d.name " +
            "ORDER BY user_count DESC")
    List<java.util.Map<String, Object>> selectUserCountByDeptEnhanced();

    /**
     * 统计用户状态分布（优化版）
     * 使用视图优化状态统计
     */
    @Select("SELECT " +
            "status, " +
            "COUNT(*) as user_count " +
            "FROM v_im_user_stats " +
            "GROUP BY status " +
            "ORDER BY user_count DESC")
    List<java.util.Map<String, Object>> selectUserCountByStatusEnhanced();

    /**
     * 批量更新用户状态（优化版）
     * 使用批量更新优化性能
     */
    @Update("<script>" +
            "UPDATE im_user " +
            "SET status = #{status}, " +
            "update_time = NOW() " +
            "WHERE id IN " +
            "<foreach collection='userIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>")
    int batchUpdateUserStatusEnhanced(
            @Param("userIds") List<Long> userIds,
            @Param("status") Integer status);

    /**
     * 批量重置用户密码（优化版）
     * 使用批量更新优化密码重置
     */
    @Update("<script>" +
            "UPDATE im_user " +
            "SET password = #{newPassword}, " +
            "update_time = NOW() " +
            "WHERE id IN " +
            "<foreach collection='userIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>")
    int batchResetPasswordEnhanced(
            @Param("userIds") List<Long> userIds,
            @Param("newPassword") String newPassword);

    /**
     * 批量删除用户（优化版）
     * 使用批量删除逻辑优化
     */
    @Delete("<script>" +
            "DELETE FROM im_user " +
            "WHERE id IN " +
            "<foreach collection='userIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>")
    int batchDeleteUsersEnhanced(
            @Param("userIds") List<Long> userIds);

    /**
     * 强制用户下线（优化版）
     * 使用批量更新优化下线状态
     */
    @Update("<script>" +
            "UPDATE im_user " +
            "SET last_online_time = NULL, " +
            "update_time = NOW() " +
            "WHERE id IN " +
            "<foreach collection='userIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>")
    int batchForceLogoutEnhanced(
            @Param("userIds") List<Long> userIds);

    /**
     * 清除用户缓存（优化版）
     * 使用批量操作优化缓存清理
     */
    @Update("<script>" +
            "DELETE FROM user_online " +
            "WHERE user_id IN " +
            "<foreach collection='userIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>")
    int batchClearUserCacheEnhanced(
            @Param("userIds") List<Long> userIds);

    /**
     * 插入用户（优化版）
     * 使用视图简化插入逻辑
     */
    @Insert("<script>" +
            "INSERT INTO im_user (" +
            "username, password, nickname, email, mobile, avatar, " +
            "gender, signature, status, dept_id, " +
            "create_time, update_time) " +
            "VALUES (" +
            "#{username}, #{password}, #{nickname}, #{email}, " +
            "#{mobile}, #{avatar}, #{gender}, #{signature}, " +
            "#{status}, #{deptId}, NOW(), NOW())")
    int insertUserEnhanced(ImUser user);

    /**
     * 更新用户（优化版）
     * 使用视图简化更新逻辑
     */
    @Update("<script>" +
            "UPDATE im_user " +
            "SET nickname = #{nickname}, " +
            "email = #{email}, " +
            "mobile = #{mobile}, " +
            "avatar = #{avatar}, " +
            "gender = #{gender}, " +
            "signature = #{signature}, " +
            "status = #{status}, " +
            "update_time = NOW() " +
            "WHERE id = #{id}")
    int updateUserEnhanced(ImUser user);

    /**
     * 检查用户名是否可用（优化版）
     * 使用视图优化用户名检查
     */
    @Select("SELECT COUNT(*) > 0 FROM v_im_user_stats WHERE username = #{username}")
    Integer checkUsernameAvailabilityEnhanced(@Param("username") String username);

    /**
     * 获取用户统计数据（优化版）
     * 使用视图优化统计数据
     */
    @Select("SELECT " +
            "COUNT(*) as total_users, " +
            "COUNT(CASE WHEN is_online = 1 THEN 1 ELSE 0 END) as online_users, " +
            "COUNT(CASE WHEN status = 0 THEN 1 ELSE 0 END) as active_users, " +
            "COUNT(CASE WHEN DATE(create_time) = CURDATE() THEN 1 ELSE 0 END) as today_registered, " +
            "AVG(CASE WHEN last_online_time >= DATE_SUB(NOW(), INTERVAL 5 MINUTE) THEN 1 ELSE 0 END) as avg_session_duration " +
            "FROM v_im_user_stats")
    java.util.Map<String, Object> selectUserStatisticsEnhanced();
}