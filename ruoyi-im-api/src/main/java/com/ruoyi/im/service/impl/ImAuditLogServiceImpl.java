package com.ruoyi.im.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.im.exception.BusinessException;
import com.ruoyi.im.mapper.ImAuditLogMapper;
import com.ruoyi.im.domain.ImAuditLog;
import com.ruoyi.im.service.ImAuditLogService;

/**
 * 审计日志Service业务层处理 - 优化版本
 * 优化内容：添加缓存机制、事务控制、性能监控、错误处理
 * 
 * @author ruoyi
 */
@Service
public class ImAuditLogServiceImpl extends EnhancedBaseServiceImpl<ImAuditLog, ImAuditLogMapper> implements ImAuditLogService {
    private static final Logger log = LoggerFactory.getLogger(ImAuditLogServiceImpl.class);
    
    @Autowired
    private ImAuditLogMapper imAuditLogMapper;
    
    // 缓存键前缀
    private static final String USER_AUDIT_LOG_CACHE_PREFIX = "im:audit:log:user:";
    private static final String OPERATION_AUDIT_LOG_CACHE_PREFIX = "im:audit:log:operation:";
    private static final String TARGET_AUDIT_LOG_CACHE_PREFIX = "im:audit:log:target:";
    private static final String IP_AUDIT_LOG_CACHE_PREFIX = "im:audit:log:ip:";
    
    // 缓存超时时间（分钟）
    private static final int CACHE_TIMEOUT_MINUTES = 60;

    /**
     * 实现EnhancedBaseServiceImpl的抽象方法
     * 
     * @return 实体类型名称
     */
    @Override
    protected String getEntityType() {
        return "auditLog";
    }
    
    /**
     * 实现EnhancedBaseServiceImpl的抽象方法
     * 
     * @param entity 审计日志实体
     * @return 审计日志ID
     */
    @Override
    protected Long getEntityId(ImAuditLog entity) {
        return entity != null ? entity.getId() : null;
    }
    
    /**
     * 实现EnhancedBaseServiceImpl的抽象方法
     * 
     * @param entity 审计日志实体
     */
    @Override
    protected void setCreateTime(ImAuditLog entity) {
        if (entity != null && entity.getCreateTime() == null) {
            entity.setCreateTime(LocalDateTime.now());
        }
    }
    
    /**
     * 实现EnhancedBaseServiceImpl的抽象方法
     * 
     * @param entity 审计日志实体
     */
    @Override
    protected void setUpdateTime(ImAuditLog entity) {
        if (entity != null) {
            entity.setCreateTime(LocalDateTime.now());
        }
    }
    
    /**
     * 实现EnhancedBaseServiceImpl中的clearRelatedCache方法，提供审计日志特定缓存清理逻辑
     * 
     * @param entity 审计日志实体
     */
    @Override
    protected void clearRelatedCache(ImAuditLog entity) {
        if (entity != null) {
            // 清除实体缓存
            clearEntityCache(entity.getId());
            
            // 清除用户审计日志缓存
            if (entity.getUserId() != null) {
                redisTemplate.delete(USER_AUDIT_LOG_CACHE_PREFIX + entity.getUserId());
            }
            
            // 清除操作类型审计日志缓存
            if (entity.getOperationType() != null) {
                redisTemplate.delete(OPERATION_AUDIT_LOG_CACHE_PREFIX + entity.getOperationType());
            }
            
            // 清除目标审计日志缓存
            if (entity.getTargetType() != null && entity.getTargetId() != null) {
                redisTemplate.delete(TARGET_AUDIT_LOG_CACHE_PREFIX + entity.getTargetType() + ":" + entity.getTargetId());
            }
            
            // 清除IP地址审计日志缓存
            if (entity.getIpAddress() != null) {
                redisTemplate.delete(IP_AUDIT_LOG_CACHE_PREFIX + entity.getIpAddress());
            }
        }
    }
    
    /**
     * 查询审计日志
     * 
     * @param id 审计日志ID
     * @return 审计日志
     */
    @Override
    public ImAuditLog selectImAuditLogById(Long id) {
        // 使用父类selectById方法，该方法已经包含缓存、验证和错误处理
        return selectById(id);
    }

    /**
     * 查询审计日志列表
     * 
     * @param imAuditLog 审计日志
     * @return 审计日志
     */
    @Override
    public List<ImAuditLog> selectImAuditLogList(ImAuditLog imAuditLog) {
        // 使用父类selectList方法，该方法已经包含验证和错误处理
        return selectList(imAuditLog);
    }

    /**
     * 新增审计日志
     * 
     * @param imAuditLog 审计日志
     * @return 结果
     */
    @Override
    public int insertImAuditLog(ImAuditLog imAuditLog) {
        // 使用父类insert方法，该方法已经包含缓存、事务控制和错误处理
        return insert(imAuditLog);
    }

    /**
     * 修改审计日志
     * 
     * @param imAuditLog 审计日志
     * @return 结果
     */
    @Override
    public int updateImAuditLog(ImAuditLog imAuditLog) {
        // 使用父类update方法，该方法已经包含缓存、事务控制和错误处理
        return update(imAuditLog);
    }

    /**
     * 批量删除审计日志
     * 
     * @param ids 需要删除的审计日志ID
     * @return 结果
     */
    @Override
    public int deleteImAuditLogByIds(Long[] ids) {
        // 使用父类deleteByIds方法，该方法已经包含缓存、事务控制和错误处理
        return deleteByIds(ids);
    }

    /**
     * 删除审计日志信息
     * 
     * @param id 审计日志ID
     * @return 结果
     */
    @Override
    public int deleteImAuditLogById(Long id) {
        // 使用父类deleteById方法，该方法已经包含缓存、事务控制和错误处理
        return deleteById(id);
    }
    
    /**
     * 根据用户ID查询审计日志列表
     * 
     * @param userId 用户ID
     * @return 审计日志集合
     */
    @Override
    public List<ImAuditLog> selectImAuditLogByUserId(Long userId) {
        long startTime = System.currentTimeMillis();
        String methodName = "selectImAuditLogByUserId";
        
        try {
            // 参数验证
            validateId(userId, methodName);
            
            log.debug("根据用户ID查询审计日志: userId={}, method={}", userId, methodName);
            
            // 生成缓存键
            String cacheKey = USER_AUDIT_LOG_CACHE_PREFIX + userId;
            
            // 检查缓存
            @SuppressWarnings("unchecked")
            List<ImAuditLog> cachedLogs = (List<ImAuditLog>) redisTemplate.opsForValue().get(cacheKey);
            if (cachedLogs != null) {
                log.debug("从缓存获取用户审计日志: userId={}, method={}", userId, methodName);
                return cachedLogs;
            }
            
            // 查询数据库
            List<ImAuditLog> logs = imAuditLogMapper.selectImAuditLogByUserId(userId);
            
            // 缓存结果
            if (logs != null && !logs.isEmpty()) {
                redisTemplate.opsForValue().set(cacheKey, logs, CACHE_TIMEOUT_MINUTES, TimeUnit.MINUTES);
                log.debug("用户审计日志已缓存: userId={}, count={}, method={}", 
                          userId, logs.size(), methodName);
            }
            
            return logs;
            
        } catch (Exception e) {
            log.error("根据用户ID查询审计日志异常: userId={}, error={}, method={}", 
                      userId, e.getMessage(), methodName, e);
            throw new BusinessException("根据用户ID查询审计日志失败", e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("根据用户ID查询审计日志耗时: {}ms, userId={}, method={}", 
                     duration, userId, methodName);
        }
    }
    
    /**
     * 根据操作类型查询审计日志列表
     * 
     * @param operationType 操作类型
     * @return 审计日志集合
     */
    @Override
    public List<ImAuditLog> selectImAuditLogByOperationType(String operationType) {
        long startTime = System.currentTimeMillis();
        String methodName = "selectImAuditLogByOperationType";
        
        try {
            // 参数验证
            if (operationType == null || operationType.trim().isEmpty()) {
                throw new BusinessException(methodName + "参数无效: 操作类型不能为空");
            }
            
            log.debug("根据操作类型查询审计日志: operationType={}, method={}", operationType, methodName);
            
            // 生成缓存键
            String cacheKey = OPERATION_AUDIT_LOG_CACHE_PREFIX + operationType;
            
            // 检查缓存
            @SuppressWarnings("unchecked")
            List<ImAuditLog> cachedLogs = (List<ImAuditLog>) redisTemplate.opsForValue().get(cacheKey);
            if (cachedLogs != null) {
                log.debug("从缓存获取操作类型审计日志: operationType={}, method={}", operationType, methodName);
                return cachedLogs;
            }
            
            // 查询数据库
            List<ImAuditLog> logs = imAuditLogMapper.selectImAuditLogByOperationType(operationType);
            
            // 缓存结果
            if (logs != null && !logs.isEmpty()) {
                redisTemplate.opsForValue().set(cacheKey, logs, CACHE_TIMEOUT_MINUTES, TimeUnit.MINUTES);
                log.debug("操作类型审计日志已缓存: operationType={}, count={}, method={}", 
                          operationType, logs.size(), methodName);
            }
            
            return logs;
            
        } catch (Exception e) {
            log.error("根据操作类型查询审计日志异常: operationType={}, error={}, method={}", 
                      operationType, e.getMessage(), methodName, e);
            throw new BusinessException("根据操作类型查询审计日志失败", e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("根据操作类型查询审计日志耗时: {}ms, operationType={}, method={}", 
                     duration, operationType, methodName);
        }
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
        long startTime = System.currentTimeMillis();
        String methodName = "selectImAuditLogByTarget";
        
        try {
            // 参数验证
            if (targetType == null || targetType.trim().isEmpty()) {
                throw new BusinessException(methodName + "参数无效: 目标类型不能为空");
            }
            validateId(targetId, methodName);
            
            log.debug("根据目标查询审计日志: targetType={}, targetId={}, method={}", 
                      targetType, targetId, methodName);
            
            // 生成缓存键
            String cacheKey = TARGET_AUDIT_LOG_CACHE_PREFIX + targetType + ":" + targetId;
            
            // 检查缓存
            @SuppressWarnings("unchecked")
            List<ImAuditLog> cachedLogs = (List<ImAuditLog>) redisTemplate.opsForValue().get(cacheKey);
            if (cachedLogs != null) {
                log.debug("从缓存获取目标审计日志: targetType={}, targetId={}, method={}", 
                          targetType, targetId, methodName);
                return cachedLogs;
            }
            
            // 查询数据库
            List<ImAuditLog> logs = imAuditLogMapper.selectImAuditLogByTarget(targetType, targetId);
            
            // 缓存结果
            if (logs != null && !logs.isEmpty()) {
                redisTemplate.opsForValue().set(cacheKey, logs, CACHE_TIMEOUT_MINUTES, TimeUnit.MINUTES);
                log.debug("目标审计日志已缓存: targetType={}, targetId={}, count={}, method={}", 
                          targetType, targetId, logs.size(), methodName);
            }
            
            return logs;
            
        } catch (Exception e) {
            log.error("根据目标查询审计日志异常: targetType={}, targetId={}, error={}, method={}", 
                      targetType, targetId, e.getMessage(), methodName, e);
            throw new BusinessException("根据目标查询审计日志失败", e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("根据目标查询审计日志耗时: {}ms, targetType={}, targetId={}, method={}", 
                     duration, targetType, targetId, methodName);
        }
    }
    
    /**
     * 根据IP地址查询审计日志列表
     * 
     * @param ipAddress IP地址
     * @return 审计日志集合
     */
    @Override
    public List<ImAuditLog> selectImAuditLogByIpAddress(String ipAddress) {
        long startTime = System.currentTimeMillis();
        String methodName = "selectImAuditLogByIpAddress";
        
        try {
            // 参数验证
            if (ipAddress == null || ipAddress.trim().isEmpty()) {
                throw new BusinessException(methodName + "参数无效: IP地址不能为空");
            }
            
            log.debug("根据IP地址查询审计日志: ipAddress={}, method={}", ipAddress, methodName);
            
            // 生成缓存键
            String cacheKey = IP_AUDIT_LOG_CACHE_PREFIX + ipAddress;
            
            // 检查缓存
            @SuppressWarnings("unchecked")
            List<ImAuditLog> cachedLogs = (List<ImAuditLog>) redisTemplate.opsForValue().get(cacheKey);
            if (cachedLogs != null) {
                log.debug("从缓存获取IP地址审计日志: ipAddress={}, method={}", ipAddress, methodName);
                return cachedLogs;
            }
            
            // 查询数据库
            List<ImAuditLog> logs = imAuditLogMapper.selectImAuditLogByIpAddress(ipAddress);
            
            // 缓存结果
            if (logs != null && !logs.isEmpty()) {
                redisTemplate.opsForValue().set(cacheKey, logs, CACHE_TIMEOUT_MINUTES, TimeUnit.MINUTES);
                log.debug("IP地址审计日志已缓存: ipAddress={}, count={}, method={}", 
                          ipAddress, logs.size(), methodName);
            }
            
            return logs;
            
        } catch (Exception e) {
            log.error("根据IP地址查询审计日志异常: ipAddress={}, error={}, method={}", 
                      ipAddress, e.getMessage(), methodName, e);
            throw new BusinessException("根据IP地址查询审计日志失败", e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("根据IP地址查询审计日志耗时: {}ms, ipAddress={}, method={}", 
                     duration, ipAddress, methodName);
        }
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
