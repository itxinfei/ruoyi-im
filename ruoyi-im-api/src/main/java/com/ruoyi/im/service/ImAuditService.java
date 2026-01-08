package com.ruoyi.im.service;

import com.ruoyi.im.domain.ImAuditLog;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 审计日志服务接口
 *
 * @author ruoyi
 */
public interface ImAuditService {

    /**
     * 获取审计日志列表（分页）
     *
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @param userId 用户ID（可选）
     * @param module 模块名称（可选）
     * @param operationType 操作类型（可选）
     * @param status 操作状态（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @return 分页结果
     */
    Map<String, Object> getAuditLogList(Integer pageNum, Integer pageSize, Long userId,
                                         String module, String operationType, String status,
                                         LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 根据ID获取审计日志详情
     *
     * @param id 日志ID
     * @return 日志详情
     */
    ImAuditLog getAuditLogById(Long id);

    /**
     * 获取用户操作日志
     *
     * @param userId 用户ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 日志列表
     */
    List<ImAuditLog> getUserLogs(Long userId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取审计统计信息
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计信息
     */
    Map<String, Object> getStatistics(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 删除过期日志
     *
     * @param beforeDate 删除此日期之前的日志
     * @return 删除数量
     */
    int deleteExpiredLogs(LocalDateTime beforeDate);

    /**
     * 记录审计日志
     *
     * @param auditLog 审计日志
     */
    void saveAuditLog(ImAuditLog auditLog);
}
