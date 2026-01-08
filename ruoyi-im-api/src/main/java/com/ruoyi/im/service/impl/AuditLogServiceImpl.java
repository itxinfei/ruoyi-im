package com.ruoyi.im.service.impl;

import com.ruoyi.im.domain.ImAuditLog;
import com.ruoyi.im.mapper.ImAuditLogMapper;
import com.ruoyi.im.service.IAuditLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

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
        if (auditLog.getOperationTime() == null) {
            auditLog.setOperationTime(LocalDateTime.now());
        }
        auditLogMapper.insertImAuditLog(auditLog);
    }

    @Override
    @Async
    public void saveLog(Long userId, String userName, String module, String operationType, String description, String status) {
        ImAuditLog auditLog = new ImAuditLog();
        auditLog.setUserId(userId);
        auditLog.setUserName(userName);
        auditLog.setModule(module);
        auditLog.setOperationType(operationType);
        auditLog.setDescription(description);
        auditLog.setStatus(status);
        auditLog.setOperationTime(LocalDateTime.now());
        saveLog(auditLog);
    }

    @Override
    public List<ImAuditLog> selectLogList(ImAuditLog auditLog) {
        return auditLogMapper.selectImAuditLogList(auditLog);
    }

    @Override
    public List<ImAuditLog> selectUserLogs(Long userId, LocalDateTime startTime, LocalDateTime endTime) {
        return auditLogMapper.selectUserLogs(userId, startTime, endTime);
    }

    @Override
    public int deleteExpiredLogs(LocalDateTime beforeDate) {
        return auditLogMapper.deleteExpiredLogs(beforeDate);
    }
}
