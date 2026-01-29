package com.ruoyi.im.service.impl;

import com.ruoyi.im.constants.StatusConstants;
import com.ruoyi.im.domain.ImAuditLog;
import com.ruoyi.im.mapper.ImAuditLogMapper;
import com.ruoyi.im.service.ImAuditService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 审计日志服务实现
 *
 * @author ruoyi
 */
@Service
public class ImAuditServiceImpl implements ImAuditService {

    private final ImAuditLogMapper imAuditLogMapper;

    /**
     * 构造器注入依赖
     *
     * @param imAuditLogMapper 审计日志Mapper
     */
    public ImAuditServiceImpl(ImAuditLogMapper imAuditLogMapper) {
        this.imAuditLogMapper = imAuditLogMapper;
    }

    @Override
    public Map<String, Object> getAuditLogList(Integer pageNum, Integer pageSize, Long userId,
                                                 String operationType, String operationResult,
                                                 LocalDateTime startTime, LocalDateTime endTime) {
        ImAuditLog query = new ImAuditLog();
        query.setUserId(userId);
        query.setOperationType(operationType);
        query.setOperationResult(operationResult);

        List<ImAuditLog> list = imAuditLogMapper.selectImAuditLogList(query);
        int total = list.size();

        // 简单分页处理
        int fromIndex = (pageNum - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, total);
        if (fromIndex >= total) {
            list = java.util.Collections.emptyList();
        } else if (fromIndex > 0 || toIndex < total) {
            list = list.subList(fromIndex, toIndex);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        result.put("pageNum", pageNum);
        result.put("pageSize", pageSize);
        return result;
    }

    @Override
    public ImAuditLog getAuditLogById(Long id) {
        return imAuditLogMapper.selectImAuditLogById(id);
    }

    @Override
    public List<ImAuditLog> getUserLogs(Long userId, LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null) {
            startTime = LocalDateTime.now().minusDays(7); // 默认查询最近7天
        }
        if (endTime == null) {
            endTime = LocalDateTime.now();
        }
        return imAuditLogMapper.selectUserLogs(userId, startTime, endTime);
    }

    @Override
    public Map<String, Object> getStatistics(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null) {
            startTime = LocalDateTime.now().minusDays(7);
        }
        if (endTime == null) {
            endTime = LocalDateTime.now();
        }

        List<ImAuditLog> allLogs = imAuditLogMapper.selectImAuditLogList(new ImAuditLog());

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalOperations", allLogs.size());
        stats.put("successCount", allLogs.stream().filter(log -> StatusConstants.OperationResult.SUCCESS.equals(log.getOperationResult())).count());
        stats.put("failCount", allLogs.stream().filter(log -> StatusConstants.OperationResult.FAILED.equals(log.getOperationResult())).count());
        stats.put("startTime", startTime);
        stats.put("endTime", endTime);
        return stats;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteExpiredLogs(LocalDateTime beforeDate) {
        return imAuditLogMapper.deleteExpiredLogs(beforeDate);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveAuditLog(ImAuditLog auditLog) {
        if (auditLog.getCreateTime() == null) {
            auditLog.setCreateTime(LocalDateTime.now());
        }
        imAuditLogMapper.insertImAuditLog(auditLog);
    }
}
