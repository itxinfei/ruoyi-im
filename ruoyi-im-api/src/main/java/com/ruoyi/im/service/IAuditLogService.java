package com.ruoyi.im.service;

import com.ruoyi.im.domain.ImAuditLog;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 操作审计日志服务接口
 *
 * @author ruoyi
 */
public interface IAuditLogService {

    /**
     * 记录操作日志
     *
     * @param auditLog 审计日志
     */
    void saveLog(ImAuditLog auditLog);

    /**
     * 记录操作日志（便捷方法）
     *
     * @param userId 用户ID
     * @param userName 用户名
     * @param module 操作模块
     * @param operationType 操作类型
     * @param description 操作描述
     * @param status 状态
     */
    void saveLog(Long userId, String userName, String module, String operationType, String description, String status);

    /**
     * 查询审计日志列表
     *
     * @param auditLog 查询条件
     * @return 日志列表
     */
    List<ImAuditLog> selectLogList(ImAuditLog auditLog);

    /**
     * 查询用户操作日志
     *
     * @param userId 用户ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 日志列表
     */
    List<ImAuditLog> selectUserLogs(Long userId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 删除过期日志
     *
     * @param beforeDate 删除此日期之前的日志
     * @return 删除数量
     */
    int deleteExpiredLogs(LocalDateTime beforeDate);
}
