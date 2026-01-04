package com.ruoyi.im.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.im.mapper.ImAuditLogMapper;
import com.ruoyi.im.domain.ImAuditLog;
import com.ruoyi.im.service.ImAuditLogService;

/**
 * 审计日志Service业务层处理
 * 
 * @author ruoyi
 */
@Service
public class ImAuditLogServiceImpl implements ImAuditLogService {
    @Autowired
    private ImAuditLogMapper imAuditLogMapper;

    /**
     * 查询审计日志
     * 
     * @param id 审计日志ID
     * @return 审计日志
     */
    @Override
    public ImAuditLog selectImAuditLogById(Long id) {
        return imAuditLogMapper.selectImAuditLogById(id);
    }

    /**
     * 查询审计日志列表
     * 
     * @param imAuditLog 审计日志
     * @return 审计日志
     */
    @Override
    public List<ImAuditLog> selectImAuditLogList(ImAuditLog imAuditLog) {
        return imAuditLogMapper.selectImAuditLogList(imAuditLog);
    }

    /**
     * 新增审计日志
     * 
     * @param imAuditLog 审计日志
     * @return 结果
     */
    @Override
    public int insertImAuditLog(ImAuditLog imAuditLog) {
        return imAuditLogMapper.insertImAuditLog(imAuditLog);
    }

    /**
     * 修改审计日志
     * 
     * @param imAuditLog 审计日志
     * @return 结果
     */
    @Override
    public int updateImAuditLog(ImAuditLog imAuditLog) {
        return imAuditLogMapper.updateImAuditLog(imAuditLog);
    }

    /**
     * 批量删除审计日志
     * 
     * @param ids 需要删除的审计日志ID
     * @return 结果
     */
    @Override
    public int deleteImAuditLogByIds(Long[] ids) {
        return imAuditLogMapper.deleteImAuditLogByIds(ids);
    }

    /**
     * 删除审计日志信息
     * 
     * @param id 审计日志ID
     * @return 结果
     */
    @Override
    public int deleteImAuditLogById(Long id) {
        return imAuditLogMapper.deleteImAuditLogById(id);
    }
    
    /**
     * 根据用户ID查询审计日志列表
     * 
     * @param userId 用户ID
     * @return 审计日志集合
     */
    @Override
    public List<ImAuditLog> selectImAuditLogByUserId(Long userId) {
        return imAuditLogMapper.selectImAuditLogByUserId(userId);
    }
    
    /**
     * 根据操作类型查询审计日志列表
     * 
     * @param operationType 操作类型
     * @return 审计日志集合
     */
    @Override
    public List<ImAuditLog> selectImAuditLogByOperationType(String operationType) {
        return imAuditLogMapper.selectImAuditLogByOperationType(operationType);
    }
    
    /**
     * 根据目标类型和目标ID查询审计日志列表
     * 
     * @param targetType 目标类型
     * @param targetId 目标ID
     * @return 审计日志集合
     */
    @Override
    public List<ImAuditLog> selectImAuditLogByTarget(String targetType, Long targetId) {
        return imAuditLogMapper.selectImAuditLogByTarget(targetType, targetId);
    }
    
    /**
     * 根据IP地址查询审计日志列表
     * 
     * @param ipAddress IP地址
     * @return 审计日志集合
     */
    @Override
    public List<ImAuditLog> selectImAuditLogByIpAddress(String ipAddress) {
        return imAuditLogMapper.selectImAuditLogByIpAddress(ipAddress);
    }
    
    /**
     * 记录审计日志
     * 
     * @param userId 用户ID
     * @param operationType 操作类型
     * @param targetType 目标类型
     * @param targetId 目标ID
     * @param operationResult 操作结果
     * @param errorMessage 错误信息
     * @param ipAddress IP地址
     * @param userAgent 用户代理
     * @return 结果
     */
    @Override
    public int logAudit(Long userId, String operationType, String targetType, Long targetId, String operationResult, String errorMessage, String ipAddress, String userAgent) {
        ImAuditLog auditLog = new ImAuditLog();
        auditLog.setUserId(userId);
        auditLog.setOperationType(operationType);
        auditLog.setTargetType(targetType);
        auditLog.setTargetId(targetId);
        auditLog.setOperationResult(operationResult);
        auditLog.setErrorMessage(errorMessage);
        auditLog.setIpAddress(ipAddress);
        auditLog.setUserAgent(userAgent);
        return insertImAuditLog(auditLog);
    }
    
    /**
     * 批量删除指定时间之前的审计日志
     * 
     * @param beforeTime 时间
     * @return 结果
     */
    @Override
    public int deleteImAuditLogByBeforeTime(LocalDateTime beforeTime) {
        return imAuditLogMapper.deleteImAuditLogByBeforeTime(beforeTime);
    }
}
