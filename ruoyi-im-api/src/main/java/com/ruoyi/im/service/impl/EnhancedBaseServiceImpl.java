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
import com.ruoyi.im.mapper.base.BaseMapper;
import com.ruoyi.im.service.BaseService;
import com.ruoyi.im.utils.CacheUtils;
import com.ruoyi.im.utils.ValidationUtils;

/**
 * 增强版基础Service实现类
 * 
 * 提供通用的缓存管理、参数验证、性能监控、异常处理等功能
 * 
 * @author ruoyi
 * @param <T> 实体类型
 * @param <M> Mapper类型
 */
@Service
public abstract class EnhancedBaseServiceImpl<T, M extends BaseMapper<T>> implements BaseService<T> {
    
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    @Autowired
    protected M baseMapper;
    
    @Autowired
    protected RedisTemplate<String, Object> redisTemplate;
    
    @Autowired
    protected CacheUtils cacheUtils;
    
    // 缓存相关常量
    protected static final String ENTITY_CACHE_PREFIX = "im:entity:";
    protected static final String LIST_CACHE_PREFIX = "im:list:";
    protected static final int DEFAULT_CACHE_TIMEOUT = 10; // 分钟
    
    /**
     * 查询实体 - 增强版（带缓存）
     * 
     * @param id 实体ID
     * @return 实体
     */
    @Override
    public T selectById(Long id) {
        long startTime = System.currentTimeMillis();
        String methodName = "selectById";
        
        try {
            // 参数验证
            validateId(id, methodName);
            
            // 生成缓存键
            String cacheKey = generateEntityCacheKey(id);
            
            // 检查缓存
            @SuppressWarnings("unchecked")
            T cachedEntity = (T) redisTemplate.opsForValue().get(cacheKey);
            if (cachedEntity != null) {
                logger.debug("从缓存获取实体: id={}, method={}", id, methodName);
                return cachedEntity;
            }
            
            // 查询数据库
            T entity = baseMapper.selectById(id);
            
            // 缓存结果
            if (entity != null) {
                cacheEntity(entity, cacheKey);
                logger.debug("实体已缓存: id={}, method={}", id, methodName);
            }
            
            return entity;
            
        } catch (Exception e) {
            logger.error("查询实体异常: id={}, method={}, error={}", id, methodName, e.getMessage(), e);
            throw new BusinessException("实体查询失败", e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            logger.info("查询实体耗时: {}ms, id={}, method={}", duration, id, methodName);
        }
    }
    
    /**
     * 查询实体列表 - 增强版（带分页和缓存）
     * 
     * @param entity 实体条件
     * @return 实体列表
     */
    @Override
    public List<T> selectList(T entity) {
        long startTime = System.currentTimeMillis();
        String methodName = "selectList";
        
        try {
            // 参数验证
            validateEntity(entity, methodName);
            
            // 查询数据库
            List<T> list = baseMapper.selectList(entity);
            
            logger.debug("查询实体列表完成: method={}, count={}", methodName, list != null ? list.size() : 0);
            return list;
            
        } catch (Exception e) {
            logger.error("查询实体列表异常: method={}, error={}", methodName, e.getMessage(), e);
            throw new BusinessException("实体列表查询失败", e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            logger.info("查询实体列表耗时: {}ms, method={}", duration, methodName);
        }
    }
    
    /**
     * 新增实体 - 增强版（带缓存清理）
     * 
     * @param entity 实体
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insert(T entity) {
        long startTime = System.currentTimeMillis();
        String methodName = "insert";
        
        try {
            // 参数验证
            validateEntity(entity, methodName);
            
            // 设置创建时间
            setCreateTime(entity);
            
            // 执行插入
            int result = baseMapper.insert(entity);
            
            if (result > 0) {
                // 清理相关缓存
                clearRelatedCache(entity);
                logger.debug("实体插入成功: method={}, result={}", methodName, result);
            }
            
            return result;
            
        } catch (Exception e) {
            logger.error("新增实体异常: method={}, error={}", methodName, e.getMessage(), e);
            // 事务回滚由Spring管理
            throw new BusinessException("实体新增失败", e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            logger.info("新增实体耗时: {}ms, method={}", duration, methodName);
        }
    }
    
    /**
     * 修改实体 - 增强版（带缓存更新）
     * 
     * @param entity 实体
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int update(T entity) {
        long startTime = System.currentTimeMillis();
        String methodName = "update";
        
        try {
            // 参数验证
            validateEntity(entity, methodName);
            
            // 设置更新时间
            setUpdateTime(entity);
            
            // 执行更新
            int result = baseMapper.update(entity);
            
            if (result > 0) {
                // 更新缓存
                updateCache(entity);
                logger.debug("实体更新成功: method={}, result={}", methodName, result);
            }
            
            return result;
            
        } catch (Exception e) {
            logger.error("修改实体异常: method={}, error={}", methodName, e.getMessage(), e);
            // 事务回滚由Spring管理
            throw new BusinessException("实体修改失败", e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            logger.info("修改实体耗时: {}ms, method={}", duration, methodName);
        }
    }
    
    /**
     * 删除实体信息 - 增强版（带缓存清理）
     * 
     * @param id 实体ID
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteById(Long id) {
        long startTime = System.currentTimeMillis();
        String methodName = "deleteById";
        
        try {
            // 参数验证
            validateId(id, methodName);
            
            // 先查询实体（用于缓存清理）
            T entity = selectById(id);
            
            // 执行删除
            int result = baseMapper.deleteById(id);
            
            if (result > 0 && entity != null) {
                // 清理缓存
                clearEntityCache(id);
                clearRelatedCache(entity);
                logger.debug("实体删除成功: method={}, id={}, result={}", methodName, id, result);
            }
            
            return result;
            
        } catch (Exception e) {
            logger.error("删除实体异常: method={}, id={}, error={}", methodName, id, e.getMessage(), e);
            // 事务回滚由Spring管理
            throw new BusinessException("实体删除失败", e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            logger.info("删除实体耗时: {}ms, method={}, id={}", duration, methodName, id);
        }
    }
    
    /**
     * 批量删除实体 - 增强版（带缓存清理）
     * 
     * @param ids 需要删除的实体ID
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteByIds(Long[] ids) {
        long startTime = System.currentTimeMillis();
        String methodName = "deleteByIds";
        
        try {
            // 参数验证
            validateIds(ids, methodName);
            
            // 执行批量删除
            int result = baseMapper.deleteByIds(ids);
            
            if (result > 0) {
                // 清理所有相关缓存
                for (Long id : ids) {
                    clearEntityCache(id);
                }
                logger.debug("批量删除实体成功: method={}, count={}, result={}", methodName, ids.length, result);
            }
            
            return result;
            
        } catch (Exception e) {
            logger.error("批量删除实体异常: method={}, count={}, error={}", methodName, ids.length, e.getMessage(), e);
            // 事务回滚由Spring管理
            throw new BusinessException("批量删除实体失败", e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            logger.info("批量删除实体耗时: {}ms, method={}, count={}", duration, methodName, ids.length);
        }
    }
    
    // ==================== 通用方法 ====================
    
    /**
     * 异步执行独立任务
     * 
     * @param task 任务
     * @return CompletableFuture
     */
    protected CompletableFuture<Void> executeAsync(Runnable task) {
        return CompletableFuture.runAsync(task).exceptionally(throwable -> {
            logger.error("异步任务执行失败: {}", throwable.getMessage(), throwable);
            return null;
        });
    }
    
    /**
     * 验证ID参数（Long类型）
     * 
     * @param id ID
     * @param methodName 方法名
     */
    protected void validateId(Long id, String methodName) {
        ValidationUtils.validateId(id, methodName);
    }
    
    /**
     * 验证ID参数（String类型）
     * 
     * @param id ID
     * @param methodName 方法名
     */
    protected void validateId(String id, String methodName) {
        if (id == null || id.trim().isEmpty()) {
            throw new BusinessException(methodName + "参数无效: ID不能为空");
        }
    }
    
    /**
     * 验证ID数组参数
     * 
     * @param ids ID数组
     * @param methodName 方法名
     */
    protected void validateIds(Long[] ids, String methodName) {
        ValidationUtils.validateIdArray(ids, methodName);
    }
    
    /**
     * 验证实体参数
     * 
     * @param entity 实体
     * @param methodName 方法名
     */
    protected void validateEntity(T entity, String methodName) {
        if (entity == null) {
            throw new BusinessException(methodName + "参数无效: 实体不能为空");
        }
    }
    
    // ==================== 缓存相关方法 ====================
    
    /**
     * 生成实体缓存键
     * 
     * @param id 实体ID
     * @return 缓存键
     */
    protected String generateEntityCacheKey(Long id) {
        return ENTITY_CACHE_PREFIX + getEntityType() + ":" + id;
    }
    
    /**
     * 生成列表缓存键
     * 
     * @param params 参数
     * @return 缓存键
     */
    protected String generateListCacheKey(Object... params) {
        StringBuilder key = new StringBuilder(LIST_CACHE_PREFIX + getEntityType() + ":");
        for (Object param : params) {
            key.append(param).append(":");
        }
        return key.toString();
    }
    
    /**
     * 缓存实体
     * 
     * @param entity 实体
     * @param cacheKey 缓存键
     */
    protected void cacheEntity(T entity, String cacheKey) {
        if (entity != null && cacheKey != null) {
            redisTemplate.opsForValue().set(cacheKey, entity, DEFAULT_CACHE_TIMEOUT, TimeUnit.MINUTES);
        }
    }
    
    /**
     * 更新缓存
     * 
     * @param entity 实体
     */
    protected void updateCache(T entity) {
        try {
            // 获取实体ID（需要子类实现）
            Long id = getEntityId(entity);
            if (id != null) {
                String cacheKey = generateEntityCacheKey(id);
                cacheEntity(entity, cacheKey);
            }
        } catch (Exception e) {
            logger.warn("更新缓存失败: {}", e.getMessage());
        }
    }
    
    /**
     * 清理实体缓存
     * 
     * @param id 实体ID
     */
    protected void clearEntityCache(Long id) {
        try {
            String cacheKey = generateEntityCacheKey(id);
            cacheUtils.clearCache(cacheKey);
        } catch (Exception e) {
            logger.warn("清理实体缓存失败: id={}, error={}", id, e.getMessage());
        }
    }
    
    /**
     * 清理相关缓存
     * 
     * @param entity 实体
     */
    protected void clearRelatedCache(T entity) {
        try {
            // 默认实现：清理实体缓存
            Long id = getEntityId(entity);
            if (id != null) {
                clearEntityCache(id);
            }
        } catch (Exception e) {
            logger.warn("清理相关缓存失败: {}", e.getMessage());
        }
    }
    
    // ==================== 抽象方法（需要子类实现） ====================
    
    /**
     * 获取实体类型名称
     * 
     * @return 实体类型名称
     */
    protected abstract String getEntityType();
    
    /**
     * 获取实体ID
     * 
     * @param entity 实体
     * @return 实体ID
     */
    protected abstract Long getEntityId(T entity);
    
    /**
     * 设置创建时间
     * 
     * @param entity 实体
     */
    protected abstract void setCreateTime(T entity);
    
    /**
     * 设置更新时间
     * 
     * @param entity 实体
     */
    protected abstract void setUpdateTime(T entity);
}