package com.ruoyi.im.service.impl;

import com.ruoyi.im.domain.ImAuditLog;
import com.ruoyi.im.mapper.ImAuditLogMapper;
import com.ruoyi.im.service.IAuditLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 操作审计日志服务实现
 *
 * @author ruoyi
 */
@Service
public class ImAuditLogServiceImpl implements IAuditLogService {

    private static final Logger log = LoggerFactory.getLogger(ImAuditLogServiceImpl.class);

    private final ImAuditLogMapper auditLogMapper;

    /**
     * 构造器注入依赖
     *
     * @param auditLogMapper 审计日志Mapper
     */
    public ImAuditLogServiceImpl(ImAuditLogMapper auditLogMapper) {
        this.auditLogMapper = auditLogMapper;
    }

    /**
     * 记录操作日志
     *
     * @param auditLog 审计日志
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveLog(ImAuditLog auditLog) {
        try {
            auditLogMapper.insertImAuditLog(auditLog);
        } catch (Exception e) {
            log.error("保存审计日志失败: userId={}, operation={}", auditLog.getUserId(), auditLog.getOperationType(), e);
        }
    }

    /**
     * 记录操作日志（便捷方法）
     *
     * @param userId         用户ID
     * @param userName       用户名
     * @param module         操作模块
     * @param operationType  操作类型
     * @param description    操作描述
     * @param status         状态
     */
    @Override
    public void saveLog(Long userId, String userName, String module, String operationType, String description, String status) {
        ImAuditLog auditLog = new ImAuditLog();
        auditLog.setUserId(userId);
        auditLog.setOperationType(operationType);
        auditLog.setOperationResult(status);
        auditLog.setCreateTime(LocalDateTime.now());
        saveLog(auditLog);
    }

    /**
     * 查询审计日志列表
     *
     * @param auditLog 查询条件
     * @return 日志列表
     */
    @Override
    public List<ImAuditLog> selectLogList(ImAuditLog auditLog) {
        try {
            return auditLogMapper.selectImAuditLogList(auditLog);
        } catch (Exception e) {
            log.error("查询审计日志列表失败", e);
            return Collections.emptyList();
        }
    }

    /**
     * 查询用户操作日志
     *
     * @param userId    用户ID
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 日志列表
     */
    @Override
    public List<ImAuditLog> selectUserLogs(Long userId, LocalDateTime startTime, LocalDateTime endTime) {
        try {
            return auditLogMapper.selectUserLogs(userId, startTime, endTime);
        } catch (Exception e) {
            log.error("查询用户操作日志失败: userId={}", userId, e);
            return Collections.emptyList();
        }
    }

    /**
     * 删除过期日志
     *
     * @param beforeDate 删除此日期之前的日志
     * @return 删除数量
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteExpiredLogs(LocalDateTime beforeDate) {
        try {
            return auditLogMapper.deleteExpiredLogs(beforeDate);
        } catch (Exception e) {
            log.error("删除过期日志失败: beforeDate={}", beforeDate, e);
            return 0;
        }
    }
}
