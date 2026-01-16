package com.ruoyi.web.service.impl;

import com.ruoyi.web.domain.ImAuditLog;
import com.ruoyi.web.mapper.ImAuditLogMapper;
import com.ruoyi.web.service.ImAuditLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 审计日志Service实现
 *
 * @author ruoyi
 */
@Service
public class ImAuditLogServiceImpl implements ImAuditLogService {

    @Autowired
    private ImAuditLogMapper imAuditLogMapper;

    @Override
    public ImAuditLog selectImAuditLogById(Long id) {
        return imAuditLogMapper.selectImAuditLogById(id);
    }

    @Override
    public List<ImAuditLog> selectImAuditLogList(ImAuditLog imAuditLog) {
        return imAuditLogMapper.selectImAuditLogList(imAuditLog);
    }

    @Override
    public List<ImAuditLog> selectAuditLogsByUserId(Long userId) {
        return imAuditLogMapper.selectAuditLogsByUserId(userId);
    }

    @Override
    public int insertImAuditLog(ImAuditLog imAuditLog) {
        return imAuditLogMapper.insertImAuditLog(imAuditLog);
    }

    @Override
    public Map<String, Object> getAuditLogStatistics() {
        return imAuditLogMapper.getAuditLogStatistics();
    }

    @Override
    public List<Map<String, Object>> countByOperationType() {
        return imAuditLogMapper.countByOperationType();
    }

    @Override
    public List<Map<String, Object>> countByUser() {
        return imAuditLogMapper.countByUser();
    }

    @Override
    public int deleteOldLogs(int days) {
        return imAuditLogMapper.deleteOldLogs(days);
    }

    @Override
    public int countFailedLogs(int hours) {
        return imAuditLogMapper.countFailedLogs(hours);
    }
}
