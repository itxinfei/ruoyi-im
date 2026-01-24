package com.ruoyi.web.mapper;

import com.ruoyi.web.domain.ImAuditLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * IM审计日志数据访问层接口（Admin模块专用）
 *
 * <p>负责处理IM审计日志管理相关的数据库操作</p>
 * <p>主要功能包括：审计日志的增删改查、按操作类型/用户统计、旧日志清理等</p>
 *
 * @author ruoyi
 * @date 2025-01-07
 */
@Mapper
public interface ImAuditLogMapper {


    /**
     * 查询审计日志
     *
     * @param id 审计日志主键
     * @return 审计日志
     */
    ImAuditLog selectImAuditLogById(Long id);

    /**
     * 查询审计日志列表
     *
     * @param imAuditLog 审计日志
     * @return 审计日志集合
     */
    List<ImAuditLog> selectImAuditLogList(ImAuditLog imAuditLog);

    /**
     * 查询用户的审计日志
     *
     * @param userId 用户ID
     * @return 日志列表
     */
    List<ImAuditLog> selectAuditLogsByUserId(Long userId);

    /**
     * 新增审计日志
     *
     * @param imAuditLog 审计日志
     * @return 结果
     */
    int insertImAuditLog(ImAuditLog imAuditLog);

    /**
     * 获取审计日志统计数据
     *
     * @return 统计数据
     */
    Map<String, Object> getAuditLogStatistics();

    /**
     * 按操作类型统计
     *
     * @return 统计列表
     */
    List<Map<String, Object>> countByOperationType();

    /**
     * 按用户统计
     *
     * @return 统计列表
     */
    List<Map<String, Object>> countByUser();

    /**
     * 删除指定日期之前的日志
     *
     * @param days 保留天数
     * @return 删除数量
     */
    int deleteOldLogs(@Param("days") int days);

    /**
     * 查询操作失败日志数量
     *
     * @param hours 小时数
     * @return 数量
     */
    int countFailedLogs(@Param("hours") int hours);
}
