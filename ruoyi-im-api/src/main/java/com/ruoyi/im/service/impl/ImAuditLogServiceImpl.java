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
 * 瀹¤鏃ュ織Service涓氬姟灞傚鐞?- 浼樺寲鐗堟湰
 * 浼樺寲鍐呭锛氭坊鍔犵紦瀛樻満鍒躲€佷簨鍔℃帶鍒躲€佹€ц兘鐩戞帶銆侀敊璇鐞? * 
 * @author ruoyi
 */
@Service
public class ImAuditLogServiceImpl extends EnhancedBaseServiceImpl<ImAuditLog, ImAuditLogMapper> implements ImAuditLogService {
    private static final Logger log = LoggerFactory.getLogger(ImAuditLogServiceImpl.class);
    
    @Autowired
    private ImAuditLogMapper imAuditLogMapper;
    
    // 缂撳瓨閿墠缂€
    private static final String USER_AUDIT_LOG_CACHE_PREFIX = "im:audit:log:user:";
    private static final String OPERATION_AUDIT_LOG_CACHE_PREFIX = "im:audit:log:operation:";
    private static final String TARGET_AUDIT_LOG_CACHE_PREFIX = "im:audit:log:target:";
    private static final String IP_AUDIT_LOG_CACHE_PREFIX = "im:audit:log:ip:";
    
    // 缂撳瓨瓒呮椂鏃堕棿锛堝垎閽燂級
    private static final int CACHE_TIMEOUT_MINUTES = 60;

    /**
     * 瀹炵幇EnhancedBaseServiceImpl鐨勬娊璞℃柟娉?     * 
     * @return 瀹炰綋绫诲瀷鍚嶇О
     */
    @Override
    protected String getEntityType() {
        return "auditLog";
    }
    
    /**
     * 瀹炵幇EnhancedBaseServiceImpl鐨勬娊璞℃柟娉?     * 
     * @param entity 瀹¤鏃ュ織瀹炰綋
     * @return 瀹¤鏃ュ織ID
     */
    @Override
    protected Long getEntityId(ImAuditLog entity) {
        return entity != null ? entity.getId() : null;
    }
    
    /**
     * 瀹炵幇EnhancedBaseServiceImpl鐨勬娊璞℃柟娉?     * 
     * @param entity 瀹¤鏃ュ織瀹炰綋
     */
    @Override
    protected void setCreateTime(ImAuditLog entity) {
        if (entity != null && entity.getCreateTime() == null) {
            entity.setCreateTime(LocalDateTime.now());
        }
    }
    
    /**
     * 瀹炵幇EnhancedBaseServiceImpl鐨勬娊璞℃柟娉?     * 
     * @param entity 瀹¤鏃ュ織瀹炰綋
     */
    @Override
    protected void setUpdateTime(ImAuditLog entity) {
        if (entity != null) {
            entity.setCreateTime(LocalDateTime.now());
        }
    }
    
    /**
     * 瀹炵幇EnhancedBaseServiceImpl涓殑clearRelatedCache鏂规硶锛屾彁渚涘璁℃棩蹇楃壒瀹氱紦瀛樻竻鐞嗛€昏緫
     * 
     * @param entity 瀹¤鏃ュ織瀹炰綋
     */
    @Override
    protected void clearRelatedCache(ImAuditLog entity) {
        if (entity != null) {
            // 娓呴櫎瀹炰綋缂撳瓨
            clearEntityCache(entity.getId());
            
            // 娓呴櫎鐢ㄦ埛瀹¤鏃ュ織缂撳瓨
            if (entity.getUserId() != null) {
                redisTemplate.delete(USER_AUDIT_LOG_CACHE_PREFIX + entity.getUserId());
            }
            
            // 娓呴櫎鎿嶄綔绫诲瀷瀹¤鏃ュ織缂撳瓨
            if (entity.getOperationType() != null) {
                redisTemplate.delete(OPERATION_AUDIT_LOG_CACHE_PREFIX + entity.getOperationType());
            }
            
            // 娓呴櫎鐩爣瀹¤鏃ュ織缂撳瓨
            if (entity.getTargetType() != null && entity.getTargetId() != null) {
                redisTemplate.delete(TARGET_AUDIT_LOG_CACHE_PREFIX + entity.getTargetType() + ":" + entity.getTargetId());
            }
            
            // 娓呴櫎IP鍦板潃瀹¤鏃ュ織缂撳瓨
            if (entity.getIpAddress() != null) {
                redisTemplate.delete(IP_AUDIT_LOG_CACHE_PREFIX + entity.getIpAddress());
            }
        }
    }
    
    /**
     * 鏌ヨ瀹¤鏃ュ織
     * 
     * @param id 瀹¤鏃ュ織ID
     * @return 瀹¤鏃ュ織
     */
    @Override
    public ImAuditLog selectImAuditLogById(Long id) {
        // 浣跨敤鐖剁被selectById鏂规硶锛岃鏂规硶宸茬粡鍖呭惈缂撳瓨銆侀獙璇佸拰閿欒澶勭悊
        return selectById(id);
    }

    /**
     * 鏌ヨ瀹¤鏃ュ織鍒楄〃
     * 
     * @param imAuditLog 瀹¤鏃ュ織
     * @return 瀹¤鏃ュ織
     */
    @Override
    public List<ImAuditLog> selectImAuditLogList(ImAuditLog imAuditLog) {
        // 浣跨敤鐖剁被selectList鏂规硶锛岃鏂规硶宸茬粡鍖呭惈楠岃瘉鍜岄敊璇鐞?        return selectList(imAuditLog);
    }

    /**
     * 鏂板瀹¤鏃ュ織
     * 
     * @param imAuditLog 瀹¤鏃ュ織
     * @return 缁撴灉
     */
    @Override
    public int insertImAuditLog(ImAuditLog imAuditLog) {
        // 浣跨敤鐖剁被insert鏂规硶锛岃鏂规硶宸茬粡鍖呭惈缂撳瓨銆佷簨鍔℃帶鍒跺拰閿欒澶勭悊
        return insert(imAuditLog);
    }

    /**
     * 淇敼瀹¤鏃ュ織
     * 
     * @param imAuditLog 瀹¤鏃ュ織
     * @return 缁撴灉
     */
    @Override
    public int updateImAuditLog(ImAuditLog imAuditLog) {
        // 浣跨敤鐖剁被update鏂规硶锛岃鏂规硶宸茬粡鍖呭惈缂撳瓨銆佷簨鍔℃帶鍒跺拰閿欒澶勭悊
        return update(imAuditLog);
    }

    /**
     * 鎵归噺鍒犻櫎瀹¤鏃ュ織
     * 
     * @param ids 闇€瑕佸垹闄ょ殑瀹¤鏃ュ織ID
     * @return 缁撴灉
     */
    @Override
    public int deleteImAuditLogByIds(Long[] ids) {
        // 浣跨敤鐖剁被deleteByIds鏂规硶锛岃鏂规硶宸茬粡鍖呭惈缂撳瓨銆佷簨鍔℃帶鍒跺拰閿欒澶勭悊
        return deleteByIds(ids);
    }

    /**
     * 鍒犻櫎瀹¤鏃ュ織淇℃伅
     * 
     * @param id 瀹¤鏃ュ織ID
     * @return 缁撴灉
     */
    @Override
    public int deleteImAuditLogById(Long id) {
        // 浣跨敤鐖剁被deleteById鏂规硶锛岃鏂规硶宸茬粡鍖呭惈缂撳瓨銆佷簨鍔℃帶鍒跺拰閿欒澶勭悊
        return deleteById(id);
    }
    
    /**
     * 鏍规嵁鐢ㄦ埛ID鏌ヨ瀹¤鏃ュ織鍒楄〃
     * 
     * @param userId 鐢ㄦ埛ID
     * @return 瀹¤鏃ュ織闆嗗悎
     */
    @Override
    public List<ImAuditLog> selectImAuditLogByUserId(Long userId) {
        long startTime = System.currentTimeMillis();
        String methodName = "selectImAuditLogByUserId";
        
        try {
            // 鍙傛暟楠岃瘉
            validateId(userId, methodName);
            
            log.debug("鏍规嵁鐢ㄦ埛ID鏌ヨ瀹¤鏃ュ織: userId={}, method={}", userId, methodName);
            
            // 鐢熸垚缂撳瓨閿?            String cacheKey = USER_AUDIT_LOG_CACHE_PREFIX + userId;
            
            // 妫€鏌ョ紦瀛?            @SuppressWarnings("unchecked")
            List<ImAuditLog> cachedLogs = (List<ImAuditLog>) redisTemplate.opsForValue().get(cacheKey);
            if (cachedLogs != null) {
                log.debug("浠庣紦瀛樿幏鍙栫敤鎴峰璁℃棩蹇? userId={}, method={}", userId, methodName);
                return cachedLogs;
            }
            
            // 鏌ヨ鏁版嵁搴?            List<ImAuditLog> logs = imAuditLogMapper.selectImAuditLogByUserId(userId);
            
            // 缂撳瓨缁撴灉
            if (logs != null && !logs.isEmpty()) {
                redisTemplate.opsForValue().set(cacheKey, logs, CACHE_TIMEOUT_MINUTES, TimeUnit.MINUTES);
                log.debug("鐢ㄦ埛瀹¤鏃ュ織宸茬紦瀛? userId={}, count={}, method={}", 
                          userId, logs.size(), methodName);
            }
            
            return logs;
            
        } catch (Exception e) {
            log.error("鏍规嵁鐢ㄦ埛ID鏌ヨ瀹¤鏃ュ織寮傚父: userId={}, error={}, method={}", 
                      userId, e.getMessage(), methodName, e);
            throw new BusinessException("鏍规嵁鐢ㄦ埛ID鏌ヨ瀹¤鏃ュ織澶辫触", e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("鏍规嵁鐢ㄦ埛ID鏌ヨ瀹¤鏃ュ織鑰楁椂: {}ms, userId={}, method={}", 
                     duration, userId, methodName);
        }
    }
    
    /**
     * 鏍规嵁鎿嶄綔绫诲瀷鏌ヨ瀹¤鏃ュ織鍒楄〃
     * 
     * @param operationType 鎿嶄綔绫诲瀷
     * @return 瀹¤鏃ュ織闆嗗悎
     */
    @Override
    public List<ImAuditLog> selectImAuditLogByOperationType(String operationType) {
        long startTime = System.currentTimeMillis();
        String methodName = "selectImAuditLogByOperationType";
        
        try {
            // 鍙傛暟楠岃瘉
            if (operationType == null || operationType.trim().isEmpty()) {
                throw new BusinessException(methodName + "鍙傛暟鏃犳晥: 鎿嶄綔绫诲瀷涓嶈兘涓虹┖");
            }
            
            log.debug("鏍规嵁鎿嶄綔绫诲瀷鏌ヨ瀹¤鏃ュ織: operationType={}, method={}", operationType, methodName);
            
            // 鐢熸垚缂撳瓨閿?            String cacheKey = OPERATION_AUDIT_LOG_CACHE_PREFIX + operationType;
            
            // 妫€鏌ョ紦瀛?            @SuppressWarnings("unchecked")
            List<ImAuditLog> cachedLogs = (List<ImAuditLog>) redisTemplate.opsForValue().get(cacheKey);
            if (cachedLogs != null) {
                log.debug("浠庣紦瀛樿幏鍙栨搷浣滅被鍨嬪璁℃棩蹇? operationType={}, method={}", operationType, methodName);
                return cachedLogs;
            }
            
            // 鏌ヨ鏁版嵁搴?            List<ImAuditLog> logs = imAuditLogMapper.selectImAuditLogByOperationType(operationType);
            
            // 缂撳瓨缁撴灉
            if (logs != null && !logs.isEmpty()) {
                redisTemplate.opsForValue().set(cacheKey, logs, CACHE_TIMEOUT_MINUTES, TimeUnit.MINUTES);
                log.debug("鎿嶄綔绫诲瀷瀹¤鏃ュ織宸茬紦瀛? operationType={}, count={}, method={}", 
                          operationType, logs.size(), methodName);
            }
            
            return logs;
            
        } catch (Exception e) {
            log.error("鏍规嵁鎿嶄綔绫诲瀷鏌ヨ瀹¤鏃ュ織寮傚父: operationType={}, error={}, method={}", 
                      operationType, e.getMessage(), methodName, e);
            throw new BusinessException("鏍规嵁鎿嶄綔绫诲瀷鏌ヨ瀹¤鏃ュ織澶辫触", e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("鏍规嵁鎿嶄綔绫诲瀷鏌ヨ瀹¤鏃ュ織鑰楁椂: {}ms, operationType={}, method={}", 
                     duration, operationType, methodName);
        }
    }
    
    /**
     * 鏍规嵁鐩爣绫诲瀷鍜岀洰鏍嘔D鏌ヨ瀹¤鏃ュ織鍒楄〃
     * 
     * @param targetType 鐩爣绫诲瀷
     * @param targetId 鐩爣ID
     * @return 瀹¤鏃ュ織闆嗗悎
     */
    @Override
    public List<ImAuditLog> selectImAuditLogByTarget(String targetType, Long targetId) {
        long startTime = System.currentTimeMillis();
        String methodName = "selectImAuditLogByTarget";
        
        try {
            // 鍙傛暟楠岃瘉
            if (targetType == null || targetType.trim().isEmpty()) {
                throw new BusinessException(methodName + "鍙傛暟鏃犳晥: 鐩爣绫诲瀷涓嶈兘涓虹┖");
            }
            validateId(targetId, methodName);
            
            log.debug("鏍规嵁鐩爣鏌ヨ瀹¤鏃ュ織: targetType={}, targetId={}, method={}", 
                      targetType, targetId, methodName);
            
            // 鐢熸垚缂撳瓨閿?            String cacheKey = TARGET_AUDIT_LOG_CACHE_PREFIX + targetType + ":" + targetId;
            
            // 妫€鏌ョ紦瀛?            @SuppressWarnings("unchecked")
            List<ImAuditLog> cachedLogs = (List<ImAuditLog>) redisTemplate.opsForValue().get(cacheKey);
            if (cachedLogs != null) {
                log.debug("浠庣紦瀛樿幏鍙栫洰鏍囧璁℃棩蹇? targetType={}, targetId={}, method={}", 
                          targetType, targetId, methodName);
                return cachedLogs;
            }
            
            // 鏌ヨ鏁版嵁搴?            List<ImAuditLog> logs = imAuditLogMapper.selectImAuditLogByTarget(targetType, targetId);
            
            // 缂撳瓨缁撴灉
            if (logs != null && !logs.isEmpty()) {
                redisTemplate.opsForValue().set(cacheKey, logs, CACHE_TIMEOUT_MINUTES, TimeUnit.MINUTES);
                log.debug("鐩爣瀹¤鏃ュ織宸茬紦瀛? targetType={}, targetId={}, count={}, method={}", 
                          targetType, targetId, logs.size(), methodName);
            }
            
            return logs;
            
        } catch (Exception e) {
            log.error("鏍规嵁鐩爣鏌ヨ瀹¤鏃ュ織寮傚父: targetType={}, targetId={}, error={}, method={}", 
                      targetType, targetId, e.getMessage(), methodName, e);
            throw new BusinessException("鏍规嵁鐩爣鏌ヨ瀹¤鏃ュ織澶辫触", e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("鏍规嵁鐩爣鏌ヨ瀹¤鏃ュ織鑰楁椂: {}ms, targetType={}, targetId={}, method={}", 
                     duration, targetType, targetId, methodName);
        }
    }
    
    /**
     * 鏍规嵁IP鍦板潃鏌ヨ瀹¤鏃ュ織鍒楄〃
     * 
     * @param ipAddress IP鍦板潃
     * @return 瀹¤鏃ュ織闆嗗悎
     */
    @Override
    public List<ImAuditLog> selectImAuditLogByIpAddress(String ipAddress) {
        long startTime = System.currentTimeMillis();
        String methodName = "selectImAuditLogByIpAddress";
        
        try {
            // 鍙傛暟楠岃瘉
            if (ipAddress == null || ipAddress.trim().isEmpty()) {
                throw new BusinessException(methodName + "鍙傛暟鏃犳晥: IP鍦板潃涓嶈兘涓虹┖");
            }
            
            log.debug("鏍规嵁IP鍦板潃鏌ヨ瀹¤鏃ュ織: ipAddress={}, method={}", ipAddress, methodName);
            
            // 鐢熸垚缂撳瓨閿?            String cacheKey = IP_AUDIT_LOG_CACHE_PREFIX + ipAddress;
            
            // 妫€鏌ョ紦瀛?            @SuppressWarnings("unchecked")
            List<ImAuditLog> cachedLogs = (List<ImAuditLog>) redisTemplate.opsForValue().get(cacheKey);
            if (cachedLogs != null) {
                log.debug("浠庣紦瀛樿幏鍙朓P鍦板潃瀹¤鏃ュ織: ipAddress={}, method={}", ipAddress, methodName);
                return cachedLogs;
            }
            
            // 鏌ヨ鏁版嵁搴?            List<ImAuditLog> logs = imAuditLogMapper.selectImAuditLogByIpAddress(ipAddress);
            
            // 缂撳瓨缁撴灉
            if (logs != null && !logs.isEmpty()) {
                redisTemplate.opsForValue().set(cacheKey, logs, CACHE_TIMEOUT_MINUTES, TimeUnit.MINUTES);
                log.debug("IP鍦板潃瀹¤鏃ュ織宸茬紦瀛? ipAddress={}, count={}, method={}", 
                          ipAddress, logs.size(), methodName);
            }
            
            return logs;
            
        } catch (Exception e) {
            log.error("鏍规嵁IP鍦板潃鏌ヨ瀹¤鏃ュ織寮傚父: ipAddress={}, error={}, method={}", 
                      ipAddress, e.getMessage(), methodName, e);
            throw new BusinessException("鏍规嵁IP鍦板潃鏌ヨ瀹¤鏃ュ織澶辫触", e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("鏍规嵁IP鍦板潃鏌ヨ瀹¤鏃ュ織鑰楁椂: {}ms, ipAddress={}, method={}", 
                     duration, ipAddress, methodName);
        }
    }
    
    /**
     * 璁板綍瀹¤鏃ュ織
     * 
     * @param userId 鐢ㄦ埛ID
     * @param operationType 鎿嶄綔绫诲瀷
     * @param targetType 鐩爣绫诲瀷
     * @param targetId 鐩爣ID
     * @param operationResult 鎿嶄綔缁撴灉
     * @param errorMessage 閿欒淇℃伅
     * @param ipAddress IP鍦板潃
     * @param userAgent 鐢ㄦ埛浠ｇ悊
     * @return 缁撴灉
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
     * 鎵归噺鍒犻櫎鎸囧畾鏃堕棿涔嬪墠鐨勫璁℃棩蹇?     * 
     * @param beforeTime 鏃堕棿
     * @return 缁撴灉
     */
    @Override
    public int deleteImAuditLogByBeforeTime(LocalDateTime beforeTime) {
        return imAuditLogMapper.deleteImAuditLogByBeforeTime(beforeTime);
    }
}
