package com.ruoyi.im.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 缂撳瓨鎿嶄綔宸ュ叿绫? * 
 * 鎻愪緵缁熶竴鐨勭紦瀛樻搷浣滄柟娉曪紝娑堥櫎閲嶅鐨勭紦瀛樻搷浣滀唬鐮? * 
 * @author ruoyi
 */
@Component
public class CacheUtils {
    
    private static final Logger log = LoggerFactory.getLogger(CacheUtils.class);
    
    private RedisTemplate<String, Object> redisTemplate;
    
    /**
     * 璁剧疆 RedisTemplate 瀹炰緥
     * 
     * @param redisTemplate RedisTemplate 瀹炰緥
     */
    public void setRedisTemplate(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    
    /**
     * 娓呴櫎鍗曚釜缂撳瓨閿?     * 
     * @param cacheKey 缂撳瓨閿?     */
    public void clearCache(String cacheKey) {
        try {
            if (redisTemplate == null) {
                log.warn("RedisTemplate 鏈垵濮嬪寲锛屾棤娉曟竻闄ょ紦瀛? {}", cacheKey);
                return;
            }
            
            redisTemplate.delete(cacheKey);
            log.debug("宸叉竻闄ょ紦瀛? {}", cacheKey);
        } catch (Exception e) {
            log.warn("娓呴櫎缂撳瓨澶辫触: {}, error={}", cacheKey, e.getMessage());
        }
    }
    
    /**
     * 娓呴櫎澶氫釜缂撳瓨閿?     * 
     * @param cacheKeys 缂撳瓨閿泦鍚?     */
    public void clearCaches(Set<String> cacheKeys) {
        try {
            if (redisTemplate == null) {
                log.warn("RedisTemplate 鏈垵濮嬪寲锛屾棤娉曟竻闄ょ紦瀛? {}", cacheKeys);
                return;
            }
            
            if (cacheKeys != null && !cacheKeys.isEmpty()) {
                redisTemplate.delete(cacheKeys);
                log.debug("宸叉竻闄?{} 涓紦瀛橀敭", cacheKeys.size());
            }
        } catch (Exception e) {
            log.warn("娓呴櫎澶氫釜缂撳瓨澶辫触: {}, error={}", cacheKeys, e.getMessage());
        }
    }
    
    /**
     * 鐢熸垚瀹炰綋缂撳瓨閿?     * 
     * @param entityType 瀹炰綋绫诲瀷
     * @param id 瀹炰綋ID
     * @return 缂撳瓨閿?     */
    public String generateEntityCacheKey(String entityType, Long id) {
        return String.format("im:%s:entity:%d", entityType, id);
    }
    
    /**
     * 鐢熸垚鍒楄〃缂撳瓨閿?     * 
     * @param entityType 瀹炰綋绫诲瀷
     * @param listType 鍒楄〃绫诲瀷
     * @param params 闄勫姞鍙傛暟
     * @return 缂撳瓨閿?     */
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
     * 璁板綍缂撳瓨鎿嶄綔缁撴灉
     * 
     * @param methodName 鏂规硶鍚?     * @param cacheKey 缂撳瓨閿?     * @param success 鏄惁鎴愬姛
     */
    public void logCacheOperation(String methodName, String cacheKey, boolean success) {
        if (success) {
            log.debug("缂撳瓨鎿嶄綔鎴愬姛: {}.{}={}", methodName, "clearCache", cacheKey);
        } else {
            log.warn("缂撳瓨鎿嶄綔澶辫触: {}.{}={}", methodName, "clearCache", cacheKey);
        }
    }
    
    /**
     * 璁板綍澶氫釜缂撳瓨鎿嶄綔缁撴灉
     * 
     * @param methodName 鏂规硶鍚?     * @param cacheKeys 缂撳瓨閿泦鍚?     * @param success 鏄惁鎴愬姛
     */
    public void logCacheOperation(String methodName, Set<String> cacheKeys, boolean success) {
        if (success) {
            log.debug("缂撳瓨鎿嶄綔鎴愬姛: {}.{} ({} 涓紦瀛橀敭)", methodName, "clearCaches", 
                     cacheKeys != null ? cacheKeys.size() : 0);
        } else {
            log.warn("缂撳瓨鎿嶄綔澶辫触: {}.{} ({} 涓紦瀛橀敭)", methodName, "clearCaches", 
                     cacheKeys != null ? cacheKeys.size() : 0);
        }
    }
}
