package com.ruoyi.im.service.impl;

import com.ruoyi.im.domain.ImAuditLog;
import com.ruoyi.im.mapper.ImAuditLogMapper;
import com.ruoyi.im.service.ImAuditService;
import com.ruoyi.im.vo.audit.AuditLogListVO;
import com.ruoyi.im.vo.audit.AuditStatisticsVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * 审计日志服务实现
 *
 * @author ruoyi
 */
@Service
public class ImAuditServiceImpl implements ImAuditService {

    @Autowired
    private ImAuditLogMapper imAuditLogMapper;

    @Override
    public AuditLogListVO getAuditLogList(Integer pageNum, Integer pageSize, Long userId,
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
            list = Collections.emptyList();
        } else if (fromIndex > 0 || toIndex < total) {
            list = list.subList(fromIndex, toIndex);
        }

        AuditLogListVO result = new AuditLogListVO();
        result.setList(list);
        result.setTotal(total);
        result.setPageNum(pageNum);
        result.setPageSize(pageSize);
        return result;
    }

    @Override
    public ImAuditLog getAuditLogById(Long id) {
        return imAuditLogMapper.selectImAuditLogById(id);
    }

    @Override
    public List<ImAuditLog> getUserLogs(Long userId, LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null) {
            startTime = LocalDateTime.now().minusDays(7);
        }
        if (endTime == null) {
            endTime = LocalDateTime.now();
        }
        return imAuditLogMapper.selectUserLogs(userId, startTime, endTime);
    }

    @Override
    public AuditStatisticsVO getStatistics(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null) {
            startTime = LocalDateTime.now().minusDays(7);
        }
        if (endTime == null) {
            endTime = LocalDateTime.now();
        }

        List<ImAuditLog> allLogs = imAuditLogMapper.selectImAuditLogList(new ImAuditLog());

        AuditStatisticsVO stats = new AuditStatisticsVO();
        stats.setTotalOperations(allLogs.size());
        stats.setSuccessCount(allLogs.stream().filter(log -> "SUCCESS".equals(log.getOperationResult())).count());
        stats.setFailCount(allLogs.stream().filter(log -> "FAILED".equals(log.getOperationResult())).count());
        stats.setStartTime(startTime);
        stats.setEndTime(endTime);
        return stats;
    }

    @Override
    public int deleteExpiredLogs(LocalDateTime beforeDate) {
        return imAuditLogMapper.deleteExpiredLogs(beforeDate);
    }

    @Override
    public void saveAuditLog(ImAuditLog auditLog) {
        if (auditLog.getCreateTime() == null) {
            auditLog.setCreateTime(LocalDateTime.now());
        }
        imAuditLogMapper.insertImAuditLog(auditLog);
    }
}
