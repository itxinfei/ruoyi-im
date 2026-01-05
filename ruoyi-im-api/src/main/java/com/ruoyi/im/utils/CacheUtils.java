package com.ruoyi.im.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 缓存操作工具类
 * 
 * 提供统一的缓存操作方法，消除重复的缓存操作代码
 * 
 * @author ruoyi
 */
@Component
public class CacheUtils {
    
    private static final Logger log = LoggerFactory.getLogger(CacheUtils.class);
    
    private RedisTemplate<String, Object> redisTemplate;
    
    /**
     * 设置 RedisTemplate 实例
     * 
     * @param redisTemplate RedisTemplate 实例
     */
    public void setRedisTemplate(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    
    /**
     * 清除单个缓存键
     * 
     * @param cacheKey 缓存键
     */
    public void clearCache(String cacheKey) {
        try {
            if (redisTemplate == null) {
                log.warn("RedisTemplate 未初始化，无法清除缓存: {}", cacheKey);
                return;
            }
            
            redisTemplate.delete(cacheKey);
            log.debug("已清除缓存: {}", cacheKey);
        } catch (Exception e) {
            log.warn("清除缓存失败: {}, error={}", cacheKey, e.getMessage());
        }
    }
    
    /**
     * 清除多个缓存键
     * 
     * @param cacheKeys 缓存键集合
     */
    public void clearCaches(Set<String> cacheKeys) {
        try {
            if (redisTemplate == null) {
                log.warn("RedisTemplate 未初始化，无法清除缓存: {}", cacheKeys);
                return;
            }
            
            if (cacheKeys != null && !cacheKeys.isEmpty()) {
                redisTemplate.delete(cacheKeys);
                log.debug("已清除 {} 个缓存键", cacheKeys.size());
            }
        } catch (Exception e) {
            log.warn("清除多个缓存失败: {}, error={}", cacheKeys, e.getMessage());
        }
    }
    
    /**
     * 生成实体缓存键
     * 
     * @param entityType 实体类型
     * @param id 实体ID
     * @return 缓存键
     */
    public String generateEntityCacheKey(String entityType, Long id) {
        return String.format("im:%s:entity:%d", entityType, id);
    }
    
    /**
     * 生成列表缓存键
     * 
     * @param entityType 实体类型
     * @param listType 列表类型
     * @param params 附加参数
     * @return 缓存键
     */
    public String generateListCacheKey(String entityType, String listType, Object... params) {
        StringBuilder sb = new StringBuilder();
        sb.append("im:").append(entityType).append(":").append(listType);
        
        if (params != null && params.length > 0) {
            sb.append(":");
            for (int i = 0; i < params.length; i++) {
                if (i > 0) {
                    sb.append(":");
                }
                sb.append(params[i] != null ? params[i].toString() : "null");
            }
        }
        
        return sb.toString();
    }
    
    /**
     * 记录缓存操作结果
     * 
     * @param methodName 方法名
     * @param cacheKey 缓存键
     * @param success 是否成功
     */
    public void logCacheOperation(String methodName, String cacheKey, boolean success) {
        if (success) {
            log.debug("缓存操作成功: {}.{}={}", methodName, "clearCache", cacheKey);
        } else {
            log.warn("缓存操作失败: {}.{}={}", methodName, "clearCache", cacheKey);
        }
    }
    
    /**
     * 记录多个缓存操作结果
     * 
     * @param methodName 方法名
     * @param cacheKeys 缓存键集合
     * @param success 是否成功
     */
    public void logCacheOperation(String methodName, Set<String> cacheKeys, boolean success) {
        if (success) {
            log.debug("缓存操作成功: {}.{} ({} 个缓存键)", methodName, "clearCaches", 
                     cacheKeys != null ? cacheKeys.size() : 0);
        } else {
            log.warn("缓存操作失败: {}.{} ({} 个缓存键)", methodName, "clearCaches", 
                     cacheKeys != null ? cacheKeys.size() : 0);
        }
    }
}