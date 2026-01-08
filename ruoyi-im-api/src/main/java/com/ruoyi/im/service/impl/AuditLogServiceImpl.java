package com.ruoyi.im.service.impl;

import cn.hutool.core.util.StrUtil;
import com.ruoyi.im.domain.ImAuditLog;
import com.ruoyi.im.mapper.ImAuditLogMapper;
import com.ruoyi.im.service.IAuditLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 操作审计日志服务实现
 *
 * @author ruoyi
 */
@Service
public class AuditLogServiceImpl implements IAuditLogService {

    @Autowired
    private ImAuditLogMapper auditLogMapper;

    @Override
    @Async
    public void saveLog(ImAuditLog auditLog) {
        if (auditLog.getCreateTime() == null) {
            auditLog.setCreateTime(LocalDateTime.now());
        }
        if (StrUtil.isBlank(auditLog.getOperationResult())) {
            auditLog.setOperationResult("SUCCESS");
        }
        auditLogMapper.insertImAuditLog(auditLog);
    }

    @Override
    @Async
    public void saveLog(Long userId, String userName, String module, String operationType, String description, String status) {
        ImAuditLog auditLog = new ImAuditLog();
        auditLog.setUserId(userId);
        auditLog.setOperationType(operationType);
        auditLog.setTargetType(module);
        auditLog.setOperationResult(status);
        auditLog.setErrorMessage(description);
        auditLog.setCreateTime(LocalDateTime.now());
        saveLog(auditLog);
    }

    @Override
    public java.util.List<ImAuditLog> selectLogList(ImAuditLog auditLog) {
        return auditLogMapper.selectImAuditLogList(auditLog);
    }

    @Override
    public java.util.List<ImAuditLog> selectUserLogs(Long userId, java.time.LocalDateTime startTime, java.time.LocalDateTime endTime) {
        return auditLogMapper.selectUserLogs(userId, startTime, endTime);
    }

    @Override
    public int deleteExpiredLogs(java.time.LocalDateTime beforeDate) {
        return auditLogMapper.deleteExpiredLogs(beforeDate);
    }
}
