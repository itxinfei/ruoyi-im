package com.ruoyi.im.service;

import com.ruoyi.im.domain.ImAuditLog;
import java.util.List;

/**
 * 审计日志Service接口
 * 
 * @author ruoyi
 */
public interface ImAuditLogService extends BaseService<ImAuditLog> {
    
    @Override
    ImAuditLog selectById(Long id);
    
    @Override
    List<ImAuditLog> selectList(ImAuditLog imAuditLog);
    
    @Override
    int insert(ImAuditLog imAuditLog);
    
    @Override
    int update(ImAuditLog imAuditLog);
    
    @Override
    int deleteByIds(Long[] ids);
    
    @Override
    int deleteById(Long id);
    
    /**
     * 根据用户ID查询审计日志列表
     * 
     * @param userId 用户ID
     * @return 审计日志集合
     */
    public List<ImAuditLog> selectImAuditLogByUserId(Long userId);
    
    /**
     * 根据操作类型查询审计日志列表
     * 
     * @param operationType 操作类型
     * @return 审计日志集合
     */
    public List<ImAuditLog> selectImAuditLogByOperationType(String operationType);
    
    /**
     * 根据目标类型和目标ID查询审计日志列表
     * 
     * @param targetType 目标类型
     * @param targetId 目标ID
     * @return 审计日志集合
     */
    public List<ImAuditLog> selectImAuditLogByTarget(String targetType, Long targetId);
    
    /**
     * 根据IP地址查询审计日志列表
     * 
     * @param ipAddress IP地址
     * @return 审计日志集合
     */
    public List<ImAuditLog> selectImAuditLogByIpAddress(String ipAddress);
    
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
    public int logAudit(Long userId, String operationType, String targetType, Long targetId, String operationResult, String errorMessage, String ipAddress, String userAgent);
    
    /**
     * 批量删除指定时间之前的审计日志
     * 
     * @param beforeTime 时间
     * @return 结果
     */
    public int deleteImAuditLogByBeforeTime(java.time.LocalDateTime beforeTime);
}
