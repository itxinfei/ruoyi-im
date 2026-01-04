package com.ruoyi.im.mapper;

import com.ruoyi.im.domain.ImAuditLog;
import java.util.List;

/**
 * 审计日志Mapper接口
 * 
 * @author ruoyi
 */
public interface ImAuditLogMapper {
    /**
     * 查询审计日志
     * 
     * @param id 审计日志ID
     * @return 审计日志
     */
    public ImAuditLog selectImAuditLogById(Long id);

    /**
     * 查询审计日志列表
     * 
     * @param imAuditLog 审计日志
     * @return 审计日志集合
     */
    public List<ImAuditLog> selectImAuditLogList(ImAuditLog imAuditLog);

    /**
     * 新增审计日志
     * 
     * @param imAuditLog 审计日志
     * @return 结果
     */
    public int insertImAuditLog(ImAuditLog imAuditLog);

    /**
     * 修改审计日志
     * 
     * @param imAuditLog 审计日志
     * @return 结果
     */
    public int updateImAuditLog(ImAuditLog imAuditLog);

    /**
     * 删除审计日志
     * 
     * @param id 审计日志ID
     * @return 结果
     */
    public int deleteImAuditLogById(Long id);

    /**
     * 批量删除审计日志
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    public int deleteImAuditLogByIds(Long[] ids);
    
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
     * 批量删除指定时间之前的审计日志
     * 
     * @param beforeTime 时间
     * @return 结果
     */
    public int deleteImAuditLogByBeforeTime(java.time.LocalDateTime beforeTime);
}
