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
import com.ruoyi.im.util.PerformanceMonitor;

/**
 * 审计导出请求Service业务层处理 - 优化版本
 * 优化内容：添加缓存机制、事务控制、性能监控、错误处理
 * 
 * @author ruoyi
 */
@Service
public class ImAuditExportRequestServiceImpl extends EnhancedBaseServiceImpl<ImAuditExportRequest, ImAuditExportRequestMapper> implements ImAuditExportRequestService {
    private static final Logger log = LoggerFactory.getLogger(ImAuditExportRequestServiceImpl.class);
    
    @Autowired
    private ImAuditExportRequestMapper imAuditExportRequestMapper;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    // 缓存键前缀
    private static final String USER_EXPORT_REQUEST_CACHE_PREFIX = "im:export:request:user:";
    private static final String STATUS_EXPORT_REQUEST_CACHE_PREFIX = "im:export:request:status:";
    private static final String USER_STATUS_EXPORT_REQUEST_CACHE_PREFIX = "im:export:request:user:status:";
    private static final String ENTITY_CACHE_PREFIX = "im:export:request:entity:";
    
    // 缓存超时时间（分钟）
    private static final int CACHE_TIMEOUT_MINUTES = 60;

    /**
     * 实现EnhancedBaseServiceImpl的抽象方法
     * 
     * @return 实体类型名称
     */
    @Override
    protected String getEntityType() {
        return "exportRequest";
    }
    
    /**
     * 实现EnhancedBaseServiceImpl的抽象方法
     * 
     * @param entity 导出请求实体
     * @return 导出请求ID
     */
    @Override
    protected Long getEntityId(ImAuditExportRequest entity) {
        return entity != null ? entity.getId() : null;
    }
    
    /**
     * 实现EnhancedBaseServiceImpl的抽象方法
     * 
     * @param entity 导出请求实体
     */
    @Override
    protected void setCreateTime(ImAuditExportRequest entity) {
        if (entity != null && entity.getCreateTime() == null) {
            entity.setCreateTime(LocalDateTime.now());
        }
    }
    
    /**
     * 实现EnhancedBaseServiceImpl的抽象方法
     * 
     * @param entity 导出请求实体
     */
    @Override
    protected void setUpdateTime(ImAuditExportRequest entity) {
        if (entity != null) {
            entity.setUpdateTime(LocalDateTime.now());
        }
    }
    
    /**
     * 实现EnhancedBaseServiceImpl中的clearRelatedCache方法，提供导出请求特定缓存清理逻辑
     * 
     * @param entity 导出请求实体
     */
    @Override
    protected void clearRelatedCache(ImAuditExportRequest entity) {
        if (entity != null) {
            // 清除实体缓存
            clearEntityCache(entity.getId());
            
            // 清除用户导出请求缓存
            if (entity.getUserId() != null) {
                redisTemplate.delete(USER_EXPORT_REQUEST_CACHE_PREFIX + entity.getUserId());
            }
            
            // 清除状态导出请求缓存
            if (entity.getStatus() != null) {
                redisTemplate.delete(STATUS_EXPORT_REQUEST_CACHE_PREFIX + entity.getStatus());
            }
            
            // 清除用户和状态组合导出请求缓存
            if (entity.getUserId() != null && entity.getStatus() != null) {
                redisTemplate.delete(USER_STATUS_EXPORT_REQUEST_CACHE_PREFIX + entity.getUserId() + ":" + entity.getStatus());
            }
        }
    }
    
    /**
     * 根据用户ID查询导出请求列表
     * 
     * @param userId 用户ID
     * @return 导出请求集合
     */
    @Override
    public List<ImAuditExportRequest> selectImAuditExportRequestByUserId(Long userId) {
        long startTime = System.currentTimeMillis();
        String methodName = "selectImAuditExportRequestByUserId";
        
        try {
            // 参数验证
            validateId(userId, methodName);
            
            log.debug("根据用户ID查询导出请求: userId={}, method={}", userId, methodName);
            
            // 生成缓存键
            String cacheKey = USER_EXPORT_REQUEST_CACHE_PREFIX + userId;
            
            // 检查缓存
            @SuppressWarnings("unchecked")
            List<ImAuditExportRequest> cachedRequests = (List<ImAuditExportRequest>) redisTemplate.opsForValue().get(cacheKey);
            if (cachedRequests != null) {
                log.debug("从缓存获取用户导出请求: userId={}, method={}", userId, methodName);
                return cachedRequests;
            }
            
            // 查询数据库
            List<ImAuditExportRequest> requests = imAuditExportRequestMapper.selectImAuditExportRequestByUserId(userId);
            
            // 缓存结果
            if (requests != null && !requests.isEmpty()) {
                redisTemplate.opsForValue().set(cacheKey, requests, CACHE_TIMEOUT_MINUTES, TimeUnit.MINUTES);
                log.debug("用户导出请求已缓存: userId={}, count={}, method={}", 
                          userId, requests.size(), methodName);
            }
            
            return requests;
            
        } catch (Exception e) {
            log.error("根据用户ID查询导出请求异常: userId={}, error={}, method={}", 
                      userId, e.getMessage(), methodName, e);
            throw new BusinessException("根据用户ID查询导出请求失败", e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("根据用户ID查询导出请求耗时: {}ms, userId={}, method={}", 
                     duration, userId, methodName);
        }
    }
    
    /**
     * 根据状态查询导出请求列表
     * 
     * @param status 状态
     * @return 导出请求集合
     */
    @Override
    public List<ImAuditExportRequest> selectImAuditExportRequestByStatus(String status) {
        long startTime = System.currentTimeMillis();
        String methodName = "selectImAuditExportRequestByStatus";
        
        try {
            // 参数验证
            if (status == null || status.trim().isEmpty()) {
                throw new BusinessException(methodName + "参数无效: 状态不能为空");
            }
            
            log.debug("根据状态查询导出请求: status={}, method={}", status, methodName);
            
            // 生成缓存键
            String cacheKey = STATUS_EXPORT_REQUEST_CACHE_PREFIX + status;
            
            // 检查缓存
            @SuppressWarnings("unchecked")
            List<ImAuditExportRequest> cachedRequests = (List<ImAuditExportRequest>) redisTemplate.opsForValue().get(cacheKey);
            if (cachedRequests != null) {
                log.debug("从缓存获取状态导出请求: status={}, method={}", status, methodName);
                return cachedRequests;
            }
            
            // 查询数据库
            List<ImAuditExportRequest> requests = imAuditExportRequestMapper.selectImAuditExportRequestByStatus(status);
            
            // 缓存结果
            if (requests != null && !requests.isEmpty()) {
                redisTemplate.opsForValue().set(cacheKey, requests, CACHE_TIMEOUT_MINUTES, TimeUnit.MINUTES);
                log.debug("状态导出请求已缓存: status={}, count={}, method={}", 
                          status, requests.size(), methodName);
            }
            
            return requests;
            
        } catch (Exception e) {
            log.error("根据状态查询导出请求异常: status={}, error={}, method={}", 
                      status, e.getMessage(), methodName, e);
            throw new BusinessException("根据状态查询导出请求失败", e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("根据状态查询导出请求耗时: {}ms, status={}, method={}", 
                     duration, status, methodName);
        }
    }
    
    /**
     * 根据用户ID和状态查询导出请求列表
     * 
     * @param userId 用户ID
     * @param status 状态
     * @return 导出请求集合
     */
    @Override
    public List<ImAuditExportRequest> selectImAuditExportRequestByUserIdAndStatus(Long userId, String status) {
        long startTime = System.currentTimeMillis();
        String methodName = "selectImAuditExportRequestByUserIdAndStatus";
        
        try {
            // 参数验证
            validateId(userId, methodName);
            if (status == null || status.trim().isEmpty()) {
                throw new BusinessException(methodName + "参数无效: 状态不能为空");
            }
            
            log.debug("根据用户ID和状态查询导出请求: userId={}, status={}, method={}", 
                      userId, status, methodName);
            
            // 生成缓存键
            String cacheKey = USER_STATUS_EXPORT_REQUEST_CACHE_PREFIX + userId + ":" + status;
            
            // 检查缓存
            @SuppressWarnings("unchecked")
            List<ImAuditExportRequest> cachedRequests = (List<ImAuditExportRequest>) redisTemplate.opsForValue().get(cacheKey);
            if (cachedRequests != null) {
                log.debug("从缓存获取用户状态导出请求: userId={}, status={}, method={}", 
                          userId, status, methodName);
                return cachedRequests;
            }
            
            // 查询数据库
            List<ImAuditExportRequest> requests = imAuditExportRequestMapper.selectImAuditExportRequestByUserIdAndStatus(userId, status);
            
            // 缓存结果
            if (requests != null && !requests.isEmpty()) {
                redisTemplate.opsForValue().set(cacheKey, requests, CACHE_TIMEOUT_MINUTES, TimeUnit.MINUTES);
                log.debug("用户状态导出请求已缓存: userId={}, status={}, count={}, method={}", 
                          userId, status, requests.size(), methodName);
            }
            
            return requests;
            
        } catch (Exception e) {
            log.error("根据用户ID和状态查询导出请求异常: userId={}, status={}, error={}, method={}", 
                      userId, status, e.getMessage(), methodName, e);
            throw new BusinessException("根据用户ID和状态查询导出请求失败", e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("根据用户ID和状态查询导出请求耗时: {}ms, userId={}, status={}, method={}", 
                     duration, userId, status, methodName);
        }
    }
    
    /**
     * 创建导出请求
     * 
     * @param userId 用户ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param operationType 操作类型
     * @param targetType 目标类型
     * @param targetId 目标ID
     * @param format 格式
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int createExportRequest(Long userId, LocalDateTime startTime, LocalDateTime endTime, String operationType, String targetType, Long targetId, String format) {
        long startTimeMs = System.currentTimeMillis();
        String methodName = "createExportRequest";
        
        try {
            // 参数验证
            validateId(userId, methodName);
            if (startTime == null) {
                throw new BusinessException(methodName + "参数无效: 开始时间不能为空");
            }
            if (endTime == null) {
                throw new BusinessException(methodName + "参数无效: 结束时间不能为空");
            }
            if (startTime.isAfter(endTime)) {
                throw new BusinessException(methodName + "参数无效: 开始时间不能晚于结束时间");
            }
            
            log.debug("创建导出请求: userId={}, startTime={}, endTime={}, operationType={}, targetType={}, targetId={}, format={}, method={}", 
                      userId, startTime, endTime, operationType, targetType, targetId, format, methodName);
            
            ImAuditExportRequest request = new ImAuditExportRequest();
            request.setUserId(userId);
            request.setExportType(operationType);
            request.setExportStatus("PENDING");
            request.setCreateTime(LocalDateTime.now());
            request.setUpdateTime(LocalDateTime.now());
            
            // 将参数存储在导出参数字段中
            StringBuilder params = new StringBuilder();
            params.append("startTime:").append(startTime)
                  .append(",endTime:").append(endTime)
                  .append(",targetType:").append(targetType)
                  .append(",targetId:").append(targetId)
                  .append(",format:").append(format != null ? format : "CSV");
            request.setExportParams(params.toString());
            
            // 使用父类方法插入
            int result = insert(request);
            
            if (result > 0) {
                log.info("导出请求创建成功: userId={}, requestId={}, method={}", 
                         userId, request.getId(), methodName);
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("创建导出请求异常: userId={}, startTime={}, endTime={}, error={}, method={}", 
                      userId, startTime, endTime, e.getMessage(), methodName, e);
            throw new BusinessException("创建导出请求失败", e);
        } finally {
            long duration = System.currentTimeMillis() - startTimeMs;
            log.info("创建导出请求耗时: {}ms, userId={}, method={}", 
                     duration, userId, methodName);
        }
    }
    
    /**
     * 更新导出请求状态
     * 
     * @param id 导出请求ID
     * @param status 状态
     * @param filePath 文件路径
     * @param errorMessage 错误信息
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateExportStatus(Long id, String status, String filePath, String errorMessage) {
        long startTime = System.currentTimeMillis();
        String methodName = "updateExportStatus";
        
        try {
            // 参数验证
            validateId(id, methodName);
            if (status == null || status.trim().isEmpty()) {
                throw new BusinessException(methodName + "参数无效: 状态不能为空");
            }
            
            log.debug("更新导出请求状态: id={}, status={}, filePath={}, errorMessage={}, method={}", 
                      id, status, filePath, errorMessage, methodName);
            
            ImAuditExportRequest request = selectById(id);
            if (request == null) {
                log.warn("导出请求不存在: id={}", id);
                return 0;
            }
            
            request.setExportStatus(status);
            request.setExportUrl(filePath);
            request.setUpdateTime(LocalDateTime.now());
            
            // 添加错误信息到导出参数中
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
            
            // 清除相关缓存
            clearEntityCache(id);
            redisTemplate.delete(STATUS_EXPORT_REQUEST_CACHE_PREFIX + status);
            
            if (result > 0) {
                log.info("导出请求状态更新成功: id={}, status={}, method={}", 
                         id, status, methodName);
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("更新导出请求状态异常: id={}, status={}, error={}, method={}", 
                      id, status, e.getMessage(), methodName, e);
            throw new BusinessException("更新导出请求状态失败", e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("更新导出请求状态耗时: {}ms, id={}, method={}", 
                     duration, id, methodName);
        }
    }
    
    /**
     * 处理导出请求
     * 
     * @param id 导出请求ID
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int processExportRequest(Long id) {
        long startTime = System.currentTimeMillis();
        String methodName = "processExportRequest";
        
        try {
            // 参数验证
            validateId(id, methodName);
            
            log.debug("处理导出请求: id={}, method={}", id, methodName);
            
            // 使用父类方法查询
            ImAuditExportRequest request = selectById(id);
            if (request == null) {
                throw new BusinessException("导出请求不存在: id=" + id);
            }
            
            if (!"PENDING".equals(request.getExportStatus())) {
                log.warn("导出请求状态不是PENDING，无法处理: id={}, status={}, method={}", 
                         id, request.getExportStatus(), methodName);
                return 0;
            }
            
            try {
                // 更新状态为处理中
                request.setExportStatus("PROCESSING");
                int updateResult = update(request);
                
                if (updateResult <= 0) {
                    throw new BusinessException("更新导出请求状态失败");
                }
                
                // 异步处理导出任务
                CompletableFuture.supplyAsync(() -> {
                    try {
                        // 生成导出文件
                        String filePath = generateExportFile(request);
                        
                        // 更新状态为完成
                        request.setExportStatus("COMPLETED");
                        request.setExportUrl(filePath);
                        update(request);
                        
                        log.info("导出请求处理完成: id={}, filePath={}, method={}", 
                                 id, filePath, methodName);
                        
                        return filePath;
                    } catch (Exception e) {
                        log.error("导出请求处理异常: id={}, error={}, method={}", 
                                  id, e.getMessage(), methodName, e);
                        
                        // 更新状态为失败
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
                
                log.info("导出请求已提交处理: id={}, method={}", id, methodName);
                
                return 1;
                
            } catch (Exception e) {
                // 更新状态为失败
                request.setExportStatus("FAILED");
                String errorMsg = request.getExportParams();
                if (errorMsg != null) {
                    errorMsg += ",errorMessage:" + e.getMessage();
                } else {
                    errorMsg = "errorMessage:" + e.getMessage();
                }
                request.setExportParams(errorMsg);
                update(request);
                
                log.error("导出请求处理异常: id={}, error={}, method={}", 
                          id, e.getMessage(), methodName, e);
                
                throw new BusinessException("导出请求处理失败", e);
            }
            
        } catch (Exception e) {
            log.error("处理导出请求异常: id={}, error={}, method={}", 
                      id, e.getMessage(), methodName, e);
            throw new BusinessException("处理导出请求失败", e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("处理导出请求耗时: {}ms, id={}, method={}", 
                     duration, id, methodName);
        }
    }
    
    /**
     * 生成导出文件
     * 
     * @param request 导出请求
     * @return 文件路径
     */
    private String generateExportFile(ImAuditExportRequest request) {
        // 这里实现导出逻辑
        String filePath = "/exports/audit_" + request.getId() + "_" + System.currentTimeMillis() + "." + 
                          (request.getExportParams() != null ? request.getExportParams() : "csv"); // 使用现有的字段替代
        
        // 模拟文件生成过程
        try {
            Thread.sleep(1000); // 模拟耗时操作
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("生成导出文件时线程被中断", e);
        }
        
        log.debug("生成导出文件: requestId={}, filePath={}", request.getId(), filePath);
        
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
