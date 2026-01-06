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
 * 澧炲己鐗堝熀纭€Service瀹炵幇绫? * 
 * 鎻愪緵閫氱敤鐨勭紦瀛樼鐞嗐€佸弬鏁伴獙璇併€佹€ц兘鐩戞帶銆佸紓甯稿鐞嗙瓑鍔熻兘
 * 
 * @author ruoyi
 * @param <T> 瀹炰綋绫诲瀷
 * @param <M> Mapper绫诲瀷
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
    
    // 缂撳瓨鐩稿叧甯搁噺
    protected static final String ENTITY_CACHE_PREFIX = "im:entity:";
    protected static final String LIST_CACHE_PREFIX = "im:list:";
    protected static final int DEFAULT_CACHE_TIMEOUT = 10; // 鍒嗛挓
    
    /**
     * 鏌ヨ瀹炰綋 - 澧炲己鐗堬紙甯︾紦瀛橈級
     * 
     * @param id 瀹炰綋ID
     * @return 瀹炰綋
     */
    @Override
    public T selectById(Long id) {
        long startTime = System.currentTimeMillis();
        String methodName = "selectById";
        
        try {
            // 鍙傛暟楠岃瘉
            validateId(id, methodName);
            
            // 鐢熸垚缂撳瓨閿?            String cacheKey = generateEntityCacheKey(id);
            
            // 妫€鏌ョ紦瀛?            @SuppressWarnings("unchecked")
            T cachedEntity = (T) redisTemplate.opsForValue().get(cacheKey);
            if (cachedEntity != null) {
                logger.debug("浠庣紦瀛樿幏鍙栧疄浣? id={}, method={}", id, methodName);
                return cachedEntity;
            }
            
            // 鏌ヨ鏁版嵁搴?            T entity = baseMapper.selectById(id);
            
            // 缂撳瓨缁撴灉
            if (entity != null) {
                cacheEntity(entity, cacheKey);
                logger.debug("瀹炰綋宸茬紦瀛? id={}, method={}", id, methodName);
            }
            
            return entity;
            
        } catch (Exception e) {
            logger.error("鏌ヨ瀹炰綋寮傚父: id={}, method={}, error={}", id, methodName, e.getMessage(), e);
            throw new BusinessException("瀹炰綋鏌ヨ澶辫触", e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            logger.info("鏌ヨ瀹炰綋鑰楁椂: {}ms, id={}, method={}", duration, id, methodName);
        }
    }
    
    /**
     * 鏌ヨ瀹炰綋鍒楄〃 - 澧炲己鐗堬紙甯﹀垎椤靛拰缂撳瓨锛?     * 
     * @param entity 瀹炰綋鏉′欢
     * @return 瀹炰綋鍒楄〃
     */
    @Override
    public List<T> selectList(T entity) {
        long startTime = System.currentTimeMillis();
        String methodName = "selectList";
        
        try {
            // 鍙傛暟楠岃瘉
            validateEntity(entity, methodName);
            
            // 鏌ヨ鏁版嵁搴?            List<T> list = baseMapper.selectList(entity);
            
            logger.debug("鏌ヨ瀹炰綋鍒楄〃瀹屾垚: method={}, count={}", methodName, list != null ? list.size() : 0);
            return list;
            
        } catch (Exception e) {
            logger.error("鏌ヨ瀹炰綋鍒楄〃寮傚父: method={}, error={}", methodName, e.getMessage(), e);
            throw new BusinessException("瀹炰綋鍒楄〃鏌ヨ澶辫触", e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            logger.info("鏌ヨ瀹炰綋鍒楄〃鑰楁椂: {}ms, method={}", duration, methodName);
        }
    }
    
    /**
     * 鏂板瀹炰綋 - 澧炲己鐗堬紙甯︾紦瀛樻竻鐞嗭級
     * 
     * @param entity 瀹炰綋
     * @return 缁撴灉
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insert(T entity) {
        long startTime = System.currentTimeMillis();
        String methodName = "insert";
        
        try {
            // 鍙傛暟楠岃瘉
            validateEntity(entity, methodName);
            
            // 璁剧疆鍒涘缓鏃堕棿
            setCreateTime(entity);
            
            // 鎵ц鎻掑叆
            int result = baseMapper.insert(entity);
            
            if (result > 0) {
                // 娓呯悊鐩稿叧缂撳瓨
                clearRelatedCache(entity);
                logger.debug("瀹炰綋鎻掑叆鎴愬姛: method={}, result={}", methodName, result);
            }
            
            return result;
            
        } catch (Exception e) {
            logger.error("鏂板瀹炰綋寮傚父: method={}, error={}", methodName, e.getMessage(), e);
            // 浜嬪姟鍥炴粴鐢盨pring绠＄悊
            throw new BusinessException("瀹炰綋鏂板澶辫触", e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            logger.info("鏂板瀹炰綋鑰楁椂: {}ms, method={}", duration, methodName);
        }
    }
    
    /**
     * 淇敼瀹炰綋 - 澧炲己鐗堬紙甯︾紦瀛樻洿鏂帮級
     * 
     * @param entity 瀹炰綋
     * @return 缁撴灉
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int update(T entity) {
        long startTime = System.currentTimeMillis();
        String methodName = "update";
        
        try {
            // 鍙傛暟楠岃瘉
            validateEntity(entity, methodName);
            
            // 璁剧疆鏇存柊鏃堕棿
            setUpdateTime(entity);
            
            // 鎵ц鏇存柊
            int result = baseMapper.update(entity);
            
            if (result > 0) {
                // 鏇存柊缂撳瓨
                updateCache(entity);
                logger.debug("瀹炰綋鏇存柊鎴愬姛: method={}, result={}", methodName, result);
            }
            
            return result;
            
        } catch (Exception e) {
            logger.error("淇敼瀹炰綋寮傚父: method={}, error={}", methodName, e.getMessage(), e);
            // 浜嬪姟鍥炴粴鐢盨pring绠＄悊
            throw new BusinessException("瀹炰綋淇敼澶辫触", e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            logger.info("淇敼瀹炰綋鑰楁椂: {}ms, method={}", duration, methodName);
        }
    }
    
    /**
     * 鍒犻櫎瀹炰綋淇℃伅 - 澧炲己鐗堬紙甯︾紦瀛樻竻鐞嗭級
     * 
     * @param id 瀹炰綋ID
     * @return 缁撴灉
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteById(Long id) {
        long startTime = System.currentTimeMillis();
        String methodName = "deleteById";
        
        try {
            // 鍙傛暟楠岃瘉
            validateId(id, methodName);
            
            // 鍏堟煡璇㈠疄浣擄紙鐢ㄤ簬缂撳瓨娓呯悊锛?            T entity = selectById(id);
            
            // 鎵ц鍒犻櫎
            int result = baseMapper.deleteById(id);
            
            if (result > 0 && entity != null) {
                // 娓呯悊缂撳瓨
                clearEntityCache(id);
                clearRelatedCache(entity);
                logger.debug("瀹炰綋鍒犻櫎鎴愬姛: method={}, id={}, result={}", methodName, id, result);
            }
            
            return result;
            
        } catch (Exception e) {
            logger.error("鍒犻櫎瀹炰綋寮傚父: method={}, id={}, error={}", methodName, id, e.getMessage(), e);
            // 浜嬪姟鍥炴粴鐢盨pring绠＄悊
            throw new BusinessException("瀹炰綋鍒犻櫎澶辫触", e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            logger.info("鍒犻櫎瀹炰綋鑰楁椂: {}ms, method={}, id={}", duration, methodName, id);
        }
    }
    
    /**
     * 鎵归噺鍒犻櫎瀹炰綋 - 澧炲己鐗堬紙甯︾紦瀛樻竻鐞嗭級
     * 
     * @param ids 闇€瑕佸垹闄ょ殑瀹炰綋ID
     * @return 缁撴灉
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteByIds(Long[] ids) {
        long startTime = System.currentTimeMillis();
        String methodName = "deleteByIds";
        
        try {
            // 鍙傛暟楠岃瘉
            validateIds(ids, methodName);
            
            // 鎵ц鎵归噺鍒犻櫎
            int result = baseMapper.deleteByIds(ids);
            
            if (result > 0) {
                // 娓呯悊鎵€鏈夌浉鍏崇紦瀛?                for (Long id : ids) {
                    clearEntityCache(id);
                }
                logger.debug("鎵归噺鍒犻櫎瀹炰綋鎴愬姛: method={}, count={}, result={}", methodName, ids.length, result);
            }
            
            return result;
            
        } catch (Exception e) {
            logger.error("鎵归噺鍒犻櫎瀹炰綋寮傚父: method={}, count={}, error={}", methodName, ids.length, e.getMessage(), e);
            // 浜嬪姟鍥炴粴鐢盨pring绠＄悊
            throw new BusinessException("鎵归噺鍒犻櫎瀹炰綋澶辫触", e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            logger.info("鎵归噺鍒犻櫎瀹炰綋鑰楁椂: {}ms, method={}, count={}", duration, methodName, ids.length);
        }
    }
    
    // ==================== 閫氱敤鏂规硶 ====================
    
    /**
     * 寮傛鎵ц鐙珛浠诲姟
     * 
     * @param task 浠诲姟
     * @return CompletableFuture
     */
    protected CompletableFuture<Void> executeAsync(Runnable task) {
        return CompletableFuture.runAsync(task).exceptionally(throwable -> {
            logger.error("寮傛浠诲姟鎵ц澶辫触: {}", throwable.getMessage(), throwable);
            return null;
        });
    }
    
    /**
     * 楠岃瘉ID鍙傛暟锛圠ong绫诲瀷锛?     * 
     * @param id ID
     * @param methodName 鏂规硶鍚?     */
    protected void validateId(Long id, String methodName) {
        ValidationUtils.validateId(id, methodName);
    }
    
    /**
     * 楠岃瘉ID鍙傛暟锛圫tring绫诲瀷锛?     * 
     * @param id ID
     * @param methodName 鏂规硶鍚?     */
    protected void validateId(String id, String methodName) {
        if (id == null || id.trim().isEmpty()) {
            throw new BusinessException(methodName + "鍙傛暟鏃犳晥: ID涓嶈兘涓虹┖");
        }
    }
    
    /**
     * 楠岃瘉ID鏁扮粍鍙傛暟
     * 
     * @param ids ID鏁扮粍
     * @param methodName 鏂规硶鍚?     */
    protected void validateIds(Long[] ids, String methodName) {
        ValidationUtils.validateIdArray(ids, methodName);
    }
    
    /**
     * 楠岃瘉瀹炰綋鍙傛暟
     * 
     * @param entity 瀹炰綋
     * @param methodName 鏂规硶鍚?     */
    protected void validateEntity(T entity, String methodName) {
        if (entity == null) {
            throw new BusinessException(methodName + "鍙傛暟鏃犳晥: 瀹炰綋涓嶈兘涓虹┖");
        }
    }
    
    // ==================== 缂撳瓨鐩稿叧鏂规硶 ====================
    
    /**
     * 鐢熸垚瀹炰綋缂撳瓨閿?     * 
     * @param id 瀹炰綋ID
     * @return 缂撳瓨閿?     */
    protected String generateEntityCacheKey(Long id) {
        return ENTITY_CACHE_PREFIX + getEntityType() + ":" + id;
    }
    
    /**
     * 鐢熸垚鍒楄〃缂撳瓨閿?     * 
     * @param params 鍙傛暟
     * @return 缂撳瓨閿?     */
    protected String generateListCacheKey(Object... params) {
        StringBuilder key = new StringBuilder(LIST_CACHE_PREFIX + getEntityType() + ":");
        for (Object param : params) {
            key.append(param).append(":");
        }
        return key.toString();
    }
    
    /**
     * 缂撳瓨瀹炰綋
     * 
     * @param entity 瀹炰綋
     * @param cacheKey 缂撳瓨閿?     */
    protected void cacheEntity(T entity, String cacheKey) {
        if (entity != null && cacheKey != null) {
            redisTemplate.opsForValue().set(cacheKey, entity, DEFAULT_CACHE_TIMEOUT, TimeUnit.MINUTES);
        }
    }
    
    /**
     * 鏇存柊缂撳瓨
     * 
     * @param entity 瀹炰綋
     */
    protected void updateCache(T entity) {
        try {
            // 鑾峰彇瀹炰綋ID锛堥渶瑕佸瓙绫诲疄鐜帮級
            Long id = getEntityId(entity);
            if (id != null) {
                String cacheKey = generateEntityCacheKey(id);
                cacheEntity(entity, cacheKey);
            }
        } catch (Exception e) {
            logger.warn("鏇存柊缂撳瓨澶辫触: {}", e.getMessage());
        }
    }
    
    /**
     * 娓呯悊瀹炰綋缂撳瓨
     * 
     * @param id 瀹炰綋ID
     */
    protected void clearEntityCache(Long id) {
        try {
            String cacheKey = generateEntityCacheKey(id);
            cacheUtils.clearCache(cacheKey);
        } catch (Exception e) {
            logger.warn("娓呯悊瀹炰綋缂撳瓨澶辫触: id={}, error={}", id, e.getMessage());
        }
    }
    
    /**
     * 娓呯悊鐩稿叧缂撳瓨
     * 
     * @param entity 瀹炰綋
     */
    protected void clearRelatedCache(T entity) {
        try {
            // 榛樿瀹炵幇锛氭竻鐞嗗疄浣撶紦瀛?            Long id = getEntityId(entity);
            if (id != null) {
                clearEntityCache(id);
            }
        } catch (Exception e) {
            logger.warn("娓呯悊鐩稿叧缂撳瓨澶辫触: {}", e.getMessage());
        }
    }
    
    // ==================== 鎶借薄鏂规硶锛堥渶瑕佸瓙绫诲疄鐜帮級 ====================
    
    /**
     * 鑾峰彇瀹炰綋绫诲瀷鍚嶇О
     * 
     * @return 瀹炰綋绫诲瀷鍚嶇О
     */
    protected abstract String getEntityType();
    
    /**
     * 鑾峰彇瀹炰綋ID
     * 
     * @param entity 瀹炰綋
     * @return 瀹炰綋ID
     */
    protected abstract Long getEntityId(T entity);
    
    /**
     * 璁剧疆鍒涘缓鏃堕棿
     * 
     * @param entity 瀹炰綋
     */
    protected abstract void setCreateTime(T entity);
    
    /**
     * 璁剧疆鏇存柊鏃堕棿
     * 
     * @param entity 瀹炰綋
     */
    protected abstract void setUpdateTime(T entity);
}
