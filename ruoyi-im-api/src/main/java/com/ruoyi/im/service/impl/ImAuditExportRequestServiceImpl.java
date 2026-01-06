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
import com.ruoyi.im.mapper.ImAuditExportRequestMapper;
import com.ruoyi.im.domain.ImAuditExportRequest;
import com.ruoyi.im.service.ImAuditExportRequestService;
import com.ruoyi.im.common.Result;
import com.ruoyi.im.exception.BusinessException;
import com.ruoyi.im.utils.PerformanceMonitor;

/**
 * 瀹¤瀵煎嚭璇锋眰Service涓氬姟灞傚鐞?- 浼樺寲鐗堟湰
 * 浼樺寲鍐呭锛氭坊鍔犵紦瀛樻満鍒躲€佷簨鍔℃帶鍒躲€佹€ц兘鐩戞帶銆侀敊璇鐞? * 
 * @author ruoyi
 */
@Service
public class ImAuditExportRequestServiceImpl extends EnhancedBaseServiceImpl<ImAuditExportRequest, ImAuditExportRequestMapper> implements ImAuditExportRequestService {
    private static final Logger log = LoggerFactory.getLogger(ImAuditExportRequestServiceImpl.class);
    
    @Autowired
    private ImAuditExportRequestMapper imAuditExportRequestMapper;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    // 缂撳瓨閿墠缂€
    private static final String USER_EXPORT_REQUEST_CACHE_PREFIX = "im:export:request:user:";
    private static final String STATUS_EXPORT_REQUEST_CACHE_PREFIX = "im:export:request:status:";
    private static final String USER_STATUS_EXPORT_REQUEST_CACHE_PREFIX = "im:export:request:user:status:";
    private static final String ENTITY_CACHE_PREFIX = "im:export:request:entity:";
    
    // 缂撳瓨瓒呮椂鏃堕棿锛堝垎閽燂級
    private static final int CACHE_TIMEOUT_MINUTES = 60;

    /**
     * 瀹炵幇EnhancedBaseServiceImpl鐨勬娊璞℃柟娉?     * 
     * @return 瀹炰綋绫诲瀷鍚嶇О
     */
    @Override
    protected String getEntityType() {
        return "exportRequest";
    }
    
    /**
     * 瀹炵幇EnhancedBaseServiceImpl鐨勬娊璞℃柟娉?     * 
     * @param entity 瀵煎嚭璇锋眰瀹炰綋
     * @return 瀵煎嚭璇锋眰ID
     */
    @Override
    protected Long getEntityId(ImAuditExportRequest entity) {
        return entity != null ? entity.getId() : null;
    }
    
    /**
     * 瀹炵幇EnhancedBaseServiceImpl鐨勬娊璞℃柟娉?     * 
     * @param entity 瀵煎嚭璇锋眰瀹炰綋
     */
    @Override
    protected void setCreateTime(ImAuditExportRequest entity) {
        if (entity != null && entity.getCreateTime() == null) {
            entity.setCreateTime(LocalDateTime.now());
        }
    }
    
    /**
     * 瀹炵幇EnhancedBaseServiceImpl鐨勬娊璞℃柟娉?     * 
     * @param entity 瀵煎嚭璇锋眰瀹炰綋
     */
    @Override
    protected void setUpdateTime(ImAuditExportRequest entity) {
        if (entity != null) {
            entity.setUpdateTime(LocalDateTime.now());
        }
    }
    
    /**
     * 瀹炵幇EnhancedBaseServiceImpl涓殑clearRelatedCache鏂规硶锛屾彁渚涘鍑鸿姹傜壒瀹氱紦瀛樻竻鐞嗛€昏緫
     * 
     * @param entity 瀵煎嚭璇锋眰瀹炰綋
     */
    @Override
    protected void clearRelatedCache(ImAuditExportRequest entity) {
        if (entity != null) {
            // 娓呴櫎瀹炰綋缂撳瓨
            clearEntityCache(entity.getId());
            
            // 娓呴櫎鐢ㄦ埛瀵煎嚭璇锋眰缂撳瓨
            if (entity.getUserId() != null) {
                redisTemplate.delete(USER_EXPORT_REQUEST_CACHE_PREFIX + entity.getUserId());
            }
            
            // 娓呴櫎鐘舵€佸鍑鸿姹傜紦瀛?            if (entity.getStatus() != null) {
                redisTemplate.delete(STATUS_EXPORT_REQUEST_CACHE_PREFIX + entity.getStatus());
            }
            
            // 娓呴櫎鐢ㄦ埛鍜岀姸鎬佺粍鍚堝鍑鸿姹傜紦瀛?            if (entity.getUserId() != null && entity.getStatus() != null) {
                redisTemplate.delete(USER_STATUS_EXPORT_REQUEST_CACHE_PREFIX + entity.getUserId() + ":" + entity.getStatus());
            }
        }
    }
    
    /**
     * 鏍规嵁鐢ㄦ埛ID鏌ヨ瀵煎嚭璇锋眰鍒楄〃
     * 
     * @param userId 鐢ㄦ埛ID
     * @return 瀵煎嚭璇锋眰闆嗗悎
     */
    @Override
    public List<ImAuditExportRequest> selectImAuditExportRequestByUserId(Long userId) {
        long startTime = System.currentTimeMillis();
        String methodName = "selectImAuditExportRequestByUserId";
        
        try {
            // 鍙傛暟楠岃瘉
            validateId(userId, methodName);
            
            log.debug("鏍规嵁鐢ㄦ埛ID鏌ヨ瀵煎嚭璇锋眰: userId={}, method={}", userId, methodName);
            
            // 鐢熸垚缂撳瓨閿?            String cacheKey = USER_EXPORT_REQUEST_CACHE_PREFIX + userId;
            
            // 妫€鏌ョ紦瀛?            @SuppressWarnings("unchecked")
            List<ImAuditExportRequest> cachedRequests = (List<ImAuditExportRequest>) redisTemplate.opsForValue().get(cacheKey);
            if (cachedRequests != null) {
                log.debug("浠庣紦瀛樿幏鍙栫敤鎴峰鍑鸿姹? userId={}, method={}", userId, methodName);
                return cachedRequests;
            }
            
            // 鏌ヨ鏁版嵁搴?            List<ImAuditExportRequest> requests = imAuditExportRequestMapper.selectImAuditExportRequestByUserId(userId);
            
            // 缂撳瓨缁撴灉
            if (requests != null && !requests.isEmpty()) {
                redisTemplate.opsForValue().set(cacheKey, requests, CACHE_TIMEOUT_MINUTES, TimeUnit.MINUTES);
                log.debug("鐢ㄦ埛瀵煎嚭璇锋眰宸茬紦瀛? userId={}, count={}, method={}", 
                          userId, requests.size(), methodName);
            }
            
            return requests;
            
        } catch (Exception e) {
            log.error("鏍规嵁鐢ㄦ埛ID鏌ヨ瀵煎嚭璇锋眰寮傚父: userId={}, error={}, method={}", 
                      userId, e.getMessage(), methodName, e);
            throw new BusinessException("鏍规嵁鐢ㄦ埛ID鏌ヨ瀵煎嚭璇锋眰澶辫触", e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("鏍规嵁鐢ㄦ埛ID鏌ヨ瀵煎嚭璇锋眰鑰楁椂: {}ms, userId={}, method={}", 
                     duration, userId, methodName);
        }
    }
    
    /**
     * 鏍规嵁鐘舵€佹煡璇㈠鍑鸿姹傚垪琛?     * 
     * @param status 鐘舵€?     * @return 瀵煎嚭璇锋眰闆嗗悎
     */
    @Override
    public List<ImAuditExportRequest> selectImAuditExportRequestByStatus(String status) {
        long startTime = System.currentTimeMillis();
        String methodName = "selectImAuditExportRequestByStatus";
        
        try {
            // 鍙傛暟楠岃瘉
            if (status == null || status.trim().isEmpty()) {
                throw new BusinessException(methodName + "鍙傛暟鏃犳晥: 鐘舵€佷笉鑳戒负绌?);
            }
            
            log.debug("鏍规嵁鐘舵€佹煡璇㈠鍑鸿姹? status={}, method={}", status, methodName);
            
            // 鐢熸垚缂撳瓨閿?            String cacheKey = STATUS_EXPORT_REQUEST_CACHE_PREFIX + status;
            
            // 妫€鏌ョ紦瀛?            @SuppressWarnings("unchecked")
            List<ImAuditExportRequest> cachedRequests = (List<ImAuditExportRequest>) redisTemplate.opsForValue().get(cacheKey);
            if (cachedRequests != null) {
                log.debug("浠庣紦瀛樿幏鍙栫姸鎬佸鍑鸿姹? status={}, method={}", status, methodName);
                return cachedRequests;
            }
            
            // 鏌ヨ鏁版嵁搴?            List<ImAuditExportRequest> requests = imAuditExportRequestMapper.selectImAuditExportRequestByStatus(status);
            
            // 缂撳瓨缁撴灉
            if (requests != null && !requests.isEmpty()) {
                redisTemplate.opsForValue().set(cacheKey, requests, CACHE_TIMEOUT_MINUTES, TimeUnit.MINUTES);
                log.debug("鐘舵€佸鍑鸿姹傚凡缂撳瓨: status={}, count={}, method={}", 
                          status, requests.size(), methodName);
            }
            
            return requests;
            
        } catch (Exception e) {
            log.error("鏍规嵁鐘舵€佹煡璇㈠鍑鸿姹傚紓甯? status={}, error={}, method={}", 
                      status, e.getMessage(), methodName, e);
            throw new BusinessException("鏍规嵁鐘舵€佹煡璇㈠鍑鸿姹傚け璐?, e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("鏍规嵁鐘舵€佹煡璇㈠鍑鸿姹傝€楁椂: {}ms, status={}, method={}", 
                     duration, status, methodName);
        }
    }
    
    /**
     * 鏍规嵁鐢ㄦ埛ID鍜岀姸鎬佹煡璇㈠鍑鸿姹傚垪琛?     * 
     * @param userId 鐢ㄦ埛ID
     * @param status 鐘舵€?     * @return 瀵煎嚭璇锋眰闆嗗悎
     */
    @Override
    public List<ImAuditExportRequest> selectImAuditExportRequestByUserIdAndStatus(Long userId, String status) {
        long startTime = System.currentTimeMillis();
        String methodName = "selectImAuditExportRequestByUserIdAndStatus";
        
        try {
            // 鍙傛暟楠岃瘉
            validateId(userId, methodName);
            if (status == null || status.trim().isEmpty()) {
                throw new BusinessException(methodName + "鍙傛暟鏃犳晥: 鐘舵€佷笉鑳戒负绌?);
            }
            
            log.debug("鏍规嵁鐢ㄦ埛ID鍜岀姸鎬佹煡璇㈠鍑鸿姹? userId={}, status={}, method={}", 
                      userId, status, methodName);
            
            // 鐢熸垚缂撳瓨閿?            String cacheKey = USER_STATUS_EXPORT_REQUEST_CACHE_PREFIX + userId + ":" + status;
            
            // 妫€鏌ョ紦瀛?            @SuppressWarnings("unchecked")
            List<ImAuditExportRequest> cachedRequests = (List<ImAuditExportRequest>) redisTemplate.opsForValue().get(cacheKey);
            if (cachedRequests != null) {
                log.debug("浠庣紦瀛樿幏鍙栫敤鎴风姸鎬佸鍑鸿姹? userId={}, status={}, method={}", 
                          userId, status, methodName);
                return cachedRequests;
            }
            
            // 鏌ヨ鏁版嵁搴?            List<ImAuditExportRequest> requests = imAuditExportRequestMapper.selectImAuditExportRequestByUserIdAndStatus(userId, status);
            
            // 缂撳瓨缁撴灉
            if (requests != null && !requests.isEmpty()) {
                redisTemplate.opsForValue().set(cacheKey, requests, CACHE_TIMEOUT_MINUTES, TimeUnit.MINUTES);
                log.debug("鐢ㄦ埛鐘舵€佸鍑鸿姹傚凡缂撳瓨: userId={}, status={}, count={}, method={}", 
                          userId, status, requests.size(), methodName);
            }
            
            return requests;
            
        } catch (Exception e) {
            log.error("鏍规嵁鐢ㄦ埛ID鍜岀姸鎬佹煡璇㈠鍑鸿姹傚紓甯? userId={}, status={}, error={}, method={}", 
                      userId, status, e.getMessage(), methodName, e);
            throw new BusinessException("鏍规嵁鐢ㄦ埛ID鍜岀姸鎬佹煡璇㈠鍑鸿姹傚け璐?, e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("鏍规嵁鐢ㄦ埛ID鍜岀姸鎬佹煡璇㈠鍑鸿姹傝€楁椂: {}ms, userId={}, status={}, method={}", 
                     duration, userId, status, methodName);
        }
    }
    
    /**
     * 鍒涘缓瀵煎嚭璇锋眰
     * 
     * @param userId 鐢ㄦ埛ID
     * @param startTime 寮€濮嬫椂闂?     * @param endTime 缁撴潫鏃堕棿
     * @param operationType 鎿嶄綔绫诲瀷
     * @param targetType 鐩爣绫诲瀷
     * @param targetId 鐩爣ID
     * @param format 鏍煎紡
     * @return 缁撴灉
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int createExportRequest(Long userId, LocalDateTime startTime, LocalDateTime endTime, String operationType, String targetType, Long targetId, String format) {
        long startTimeMs = System.currentTimeMillis();
        String methodName = "createExportRequest";
        
        try {
            // 鍙傛暟楠岃瘉
            validateId(userId, methodName);
            if (startTime == null) {
                throw new BusinessException(methodName + "鍙傛暟鏃犳晥: 寮€濮嬫椂闂翠笉鑳戒负绌?);
            }
            if (endTime == null) {
                throw new BusinessException(methodName + "鍙傛暟鏃犳晥: 缁撴潫鏃堕棿涓嶈兘涓虹┖");
            }
            if (startTime.isAfter(endTime)) {
                throw new BusinessException(methodName + "鍙傛暟鏃犳晥: 寮€濮嬫椂闂翠笉鑳芥櫄浜庣粨鏉熸椂闂?);
            }
            
            log.debug("鍒涘缓瀵煎嚭璇锋眰: userId={}, startTime={}, endTime={}, operationType={}, targetType={}, targetId={}, format={}, method={}", 
                      userId, startTime, endTime, operationType, targetType, targetId, format, methodName);
            
            ImAuditExportRequest request = new ImAuditExportRequest();
            request.setUserId(userId);
            request.setExportType(operationType);
            request.setExportStatus("PENDING");
            request.setCreateTime(LocalDateTime.now());
            request.setUpdateTime(LocalDateTime.now());
            
            // 灏嗗弬鏁板瓨鍌ㄥ湪瀵煎嚭鍙傛暟瀛楁涓?            StringBuilder params = new StringBuilder();
            params.append("startTime:").append(startTime)
                  .append(",endTime:").append(endTime)
                  .append(",targetType:").append(targetType)
                  .append(",targetId:").append(targetId)
                  .append(",format:").append(format != null ? format : "CSV");
            request.setExportParams(params.toString());
            
            // 浣跨敤鐖剁被鏂规硶鎻掑叆
            int result = insert(request);
            
            if (result > 0) {
                log.info("瀵煎嚭璇锋眰鍒涘缓鎴愬姛: userId={}, requestId={}, method={}", 
                         userId, request.getId(), methodName);
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("鍒涘缓瀵煎嚭璇锋眰寮傚父: userId={}, startTime={}, endTime={}, error={}, method={}", 
                      userId, startTime, endTime, e.getMessage(), methodName, e);
            throw new BusinessException("鍒涘缓瀵煎嚭璇锋眰澶辫触", e);
        } finally {
            long duration = System.currentTimeMillis() - startTimeMs;
            log.info("鍒涘缓瀵煎嚭璇锋眰鑰楁椂: {}ms, userId={}, method={}", 
                     duration, userId, methodName);
        }
    }
    
    /**
     * 鏇存柊瀵煎嚭璇锋眰鐘舵€?     * 
     * @param id 瀵煎嚭璇锋眰ID
     * @param status 鐘舵€?     * @param filePath 鏂囦欢璺緞
     * @param errorMessage 閿欒淇℃伅
     * @return 缁撴灉
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateExportStatus(Long id, String status, String filePath, String errorMessage) {
        long startTime = System.currentTimeMillis();
        String methodName = "updateExportStatus";
        
        try {
            // 鍙傛暟楠岃瘉
            validateId(id, methodName);
            if (status == null || status.trim().isEmpty()) {
                throw new BusinessException(methodName + "鍙傛暟鏃犳晥: 鐘舵€佷笉鑳戒负绌?);
            }
            
            log.debug("鏇存柊瀵煎嚭璇锋眰鐘舵€? id={}, status={}, filePath={}, errorMessage={}, method={}", 
                      id, status, filePath, errorMessage, methodName);
            
            ImAuditExportRequest request = selectById(id);
            if (request == null) {
                log.warn("瀵煎嚭璇锋眰涓嶅瓨鍦? id={}", id);
                return 0;
            }
            
            request.setExportStatus(status);
            request.setExportUrl(filePath);
            request.setUpdateTime(LocalDateTime.now());
            
            // 娣诲姞閿欒淇℃伅鍒板鍑哄弬鏁颁腑
            if (errorMessage != null) {
                String params = request.getExportParams();
                if (params != null) {
                    params += ",errorMessage:" + errorMessage;
                } else {
                    params = "errorMessage:" + errorMessage;
                }
                request.setExportParams(params);
            }
            
            int result = update(request);
            
            // 娓呴櫎鐩稿叧缂撳瓨
            clearEntityCache(id);
            redisTemplate.delete(STATUS_EXPORT_REQUEST_CACHE_PREFIX + status);
            
            if (result > 0) {
                log.info("瀵煎嚭璇锋眰鐘舵€佹洿鏂版垚鍔? id={}, status={}, method={}", 
                         id, status, methodName);
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("鏇存柊瀵煎嚭璇锋眰鐘舵€佸紓甯? id={}, status={}, error={}, method={}", 
                      id, status, e.getMessage(), methodName, e);
            throw new BusinessException("鏇存柊瀵煎嚭璇锋眰鐘舵€佸け璐?, e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("鏇存柊瀵煎嚭璇锋眰鐘舵€佽€楁椂: {}ms, id={}, method={}", 
                     duration, id, methodName);
        }
    }
    
    /**
     * 澶勭悊瀵煎嚭璇锋眰
     * 
     * @param id 瀵煎嚭璇锋眰ID
     * @return 缁撴灉
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int processExportRequest(Long id) {
        long startTime = System.currentTimeMillis();
        String methodName = "processExportRequest";
        
        try {
            // 鍙傛暟楠岃瘉
            validateId(id, methodName);
            
            log.debug("澶勭悊瀵煎嚭璇锋眰: id={}, method={}", id, methodName);
            
            // 浣跨敤鐖剁被鏂规硶鏌ヨ
            ImAuditExportRequest request = selectById(id);
            if (request == null) {
                throw new BusinessException("瀵煎嚭璇锋眰涓嶅瓨鍦? id=" + id);
            }
            
            if (!"PENDING".equals(request.getExportStatus())) {
                log.warn("瀵煎嚭璇锋眰鐘舵€佷笉鏄疨ENDING锛屾棤娉曞鐞? id={}, status={}, method={}", 
                         id, request.getExportStatus(), methodName);
                return 0;
            }
            
            try {
                // 鏇存柊鐘舵€佷负澶勭悊涓?                request.setExportStatus("PROCESSING");
                int updateResult = update(request);
                
                if (updateResult <= 0) {
                    throw new BusinessException("鏇存柊瀵煎嚭璇锋眰鐘舵€佸け璐?);
                }
                
                // 寮傛澶勭悊瀵煎嚭浠诲姟
                CompletableFuture.supplyAsync(() -> {
                    try {
                        // 鐢熸垚瀵煎嚭鏂囦欢
                        String filePath = generateExportFile(request);
                        
                        // 鏇存柊鐘舵€佷负瀹屾垚
                        request.setExportStatus("COMPLETED");
                        request.setExportUrl(filePath);
                        update(request);
                        
                        log.info("瀵煎嚭璇锋眰澶勭悊瀹屾垚: id={}, filePath={}, method={}", 
                                 id, filePath, methodName);
                        
                        return filePath;
                    } catch (Exception e) {
                        log.error("瀵煎嚭璇锋眰澶勭悊寮傚父: id={}, error={}, method={}", 
                                  id, e.getMessage(), methodName, e);
                        
                        // 鏇存柊鐘舵€佷负澶辫触
                        request.setExportStatus("FAILED");
                        String errorMsg = request.getExportParams();
                        if (errorMsg != null) {
                            errorMsg += ",errorMessage:" + e.getMessage();
                        } else {
                            errorMsg = "errorMessage:" + e.getMessage();
                        }
                        request.setExportParams(errorMsg);
                        update(request);
                        
                        return null;
                    }
                });
                
                log.info("瀵煎嚭璇锋眰宸叉彁浜ゅ鐞? id={}, method={}", id, methodName);
                
                return 1;
                
            } catch (Exception e) {
                // 鏇存柊鐘舵€佷负澶辫触
                request.setExportStatus("FAILED");
                String errorMsg = request.getExportParams();
                if (errorMsg != null) {
                    errorMsg += ",errorMessage:" + e.getMessage();
                } else {
                    errorMsg = "errorMessage:" + e.getMessage();
                }
                request.setExportParams(errorMsg);
                update(request);
                
                log.error("瀵煎嚭璇锋眰澶勭悊寮傚父: id={}, error={}, method={}", 
                          id, e.getMessage(), methodName, e);
                
                throw new BusinessException("瀵煎嚭璇锋眰澶勭悊澶辫触", e);
            }
            
        } catch (Exception e) {
            log.error("澶勭悊瀵煎嚭璇锋眰寮傚父: id={}, error={}, method={}", 
                      id, e.getMessage(), methodName, e);
            throw new BusinessException("澶勭悊瀵煎嚭璇锋眰澶辫触", e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("澶勭悊瀵煎嚭璇锋眰鑰楁椂: {}ms, id={}, method={}", 
                     duration, id, methodName);
        }
    }
    
    /**
     * 鐢熸垚瀵煎嚭鏂囦欢
     * 
     * @param request 瀵煎嚭璇锋眰
     * @return 鏂囦欢璺緞
     */
    private String generateExportFile(ImAuditExportRequest request) {
        // 杩欓噷瀹炵幇瀵煎嚭閫昏緫
        String filePath = "/exports/audit_" + request.getId() + "_" + System.currentTimeMillis() + "." + 
                          (request.getExportParams() != null ? request.getExportParams() : "csv"); // 浣跨敤鐜版湁鐨勫瓧娈垫浛浠?        
        // 妯℃嫙鏂囦欢鐢熸垚杩囩▼
        try {
            Thread.sleep(1000); // 妯℃嫙鑰楁椂鎿嶄綔
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("鐢熸垚瀵煎嚭鏂囦欢鏃剁嚎绋嬭涓柇", e);
        }
        
        log.debug("鐢熸垚瀵煎嚭鏂囦欢: requestId={}, filePath={}", request.getId(), filePath);
        
        return filePath;
    }

            @Override
            public int updateImAuditExportRequest(ImAuditExportRequest imAuditExportRequest) {
                return update(imAuditExportRequest);
            }
        
            @Override
            public int deleteImAuditExportRequestByIds(Long[] ids) {
                return deleteByIds(ids);
            }
        
            @Override
            public int deleteImAuditExportRequestById(Long id) {
                return deleteById(id);
            }
        
            @Override
            public int insertImAuditExportRequest(ImAuditExportRequest imAuditExportRequest) {
                return insert(imAuditExportRequest);
            }
        
            @Override
            public List<ImAuditExportRequest> selectImAuditExportRequestList(ImAuditExportRequest imAuditExportRequest) {
                return selectList(imAuditExportRequest);
            }
        }
