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
 * 婢х偛宸遍悧鍫濈唨绾偓Service鐎圭偟骞囩猾? * 
 * 閹绘劒绶甸柅姘辨暏閻ㄥ嫮绱︾€涙顓搁悶鍡愨偓浣稿棘閺佷即鐛欑拠浣碘偓浣光偓褑鍏橀惄鎴炲付閵嗕礁绱撶敮绋款槱閻炲棛鐡戦崝鐔诲厴
 * 
 * @author ruoyi
 * @param <T> 鐎圭偘缍嬬猾璇茬€?
 * @param <M> Mapper缁鐎?
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
    
    // 缂傛挸鐡ㄩ惄绋垮彠鐢悂鍣?
    protected static final String ENTITY_CACHE_PREFIX = "im:entity:";
    protected static final String LIST_CACHE_PREFIX = "im:list:";
    protected static final int DEFAULT_CACHE_TIMEOUT = 10; // 閸掑棝鎸?
    
    /**
     * 閺屻儴顕楃€圭偘缍?- 婢х偛宸遍悧鍫礄鐢妇绱︾€涙﹫绱?
     * 
     * @param id 鐎圭偘缍婭D
     * @return 鐎圭偘缍?
     */
    @Override
    public T selectById(Long id) {
        long startTime = System.currentTimeMillis();
        String methodName = "selectById";
        
        try {
            // 閸欏倹鏆熸宀冪槈
            validateId(id, methodName);
            
            // 閻㈢喐鍨氱紓鎾崇摠闁?            String cacheKey = generateEntityCacheKey(id);
            
            // 濡偓閺屻儳绱︾€?            @SuppressWarnings("unchecked")
            T cachedEntity = (T) redisTemplate.opsForValue().get(cacheKey);
            if (cachedEntity != null) {
                logger.debug("娴犲海绱︾€涙骞忛崣鏍х杽娴? id={}, method={}", id, methodName);
                return cachedEntity;
            }
            
            // 閺屻儴顕楅弫鐗堝祦鎼?            T entity = baseMapper.selectById(id);
            
            // 缂傛挸鐡ㄧ紒鎾寸亯
            if (entity != null) {
                cacheEntity(entity, cacheKey);
                logger.debug("鐎圭偘缍嬪鑼处鐎? id={}, method={}", id, methodName);
            }
            
            return entity;
            
        } catch (Exception e) {
            logger.error("閺屻儴顕楃€圭偘缍嬪鍌氱埗: id={}, method={}, error={}", id, methodName, e.getMessage(), e);
            throw new BusinessException("鐎圭偘缍嬮弻銉嚄婢惰精瑙?, e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            logger.info("閺屻儴顕楃€圭偘缍嬮懓妤佹: {}ms, id={}, method={}", duration, id, methodName);
        }
    }
    
    /**
     * 閺屻儴顕楃€圭偘缍嬮崚妤勩€?- 婢х偛宸遍悧鍫礄鐢箑鍨庢い闈涙嫲缂傛挸鐡ㄩ敍?     * 
     * @param entity 鐎圭偘缍嬮弶鈥叉
     * @return 鐎圭偘缍嬮崚妤勩€?
     */
    @Override
    public List<T> selectList(T entity) {
        long startTime = System.currentTimeMillis();
        String methodName = "selectList";
        
        try {
            // 閸欏倹鏆熸宀冪槈
            validateEntity(entity, methodName);
            
            // 閺屻儴顕楅弫鐗堝祦鎼?            List<T> list = baseMapper.selectList(entity);
            
            logger.debug("閺屻儴顕楃€圭偘缍嬮崚妤勩€冪€瑰本鍨? method={}, count={}", methodName, list != null ? list.size() : 0);
            return list;
            
        } catch (Exception e) {
            logger.error("閺屻儴顕楃€圭偘缍嬮崚妤勩€冨鍌氱埗: method={}, error={}", methodName, e.getMessage(), e);
            throw new BusinessException("鐎圭偘缍嬮崚妤勩€冮弻銉嚄婢惰精瑙?, e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            logger.info("閺屻儴顕楃€圭偘缍嬮崚妤勩€冮懓妤佹: {}ms, method={}", duration, methodName);
        }
    }
    
    /**
     * 閺傛澘顤冪€圭偘缍?- 婢х偛宸遍悧鍫礄鐢妇绱︾€涙ɑ绔婚悶鍡礆
     * 
     * @param entity 鐎圭偘缍?
     * @return 缂佹挻鐏?
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insert(T entity) {
        long startTime = System.currentTimeMillis();
        String methodName = "insert";
        
        try {
            // 閸欏倹鏆熸宀冪槈
            validateEntity(entity, methodName);
            
            // 鐠佸墽鐤嗛崚娑樼紦閺冨爼妫?
            setCreateTime(entity);
            
            // 閹笛嗩攽閹绘帒鍙?
            int result = baseMapper.insert(entity);
            
            if (result > 0) {
                // 濞撳懐鎮婇惄绋垮彠缂傛挸鐡?
                clearRelatedCache(entity);
                logger.debug("鐎圭偘缍嬮幓鎺戝弳閹存劕濮? method={}, result={}", methodName, result);
            }
            
            return result;
            
        } catch (Exception e) {
            logger.error("閺傛澘顤冪€圭偘缍嬪鍌氱埗: method={}, error={}", methodName, e.getMessage(), e);
            // 娴滃濮熼崶鐐寸泊閻㈢洦pring缁狅紕鎮?
            throw new BusinessException("鐎圭偘缍嬮弬鏉款杻婢惰精瑙?, e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            logger.info("閺傛澘顤冪€圭偘缍嬮懓妤佹: {}ms, method={}", duration, methodName);
        }
    }
    
    /**
     * 娣囶喗鏁肩€圭偘缍?- 婢х偛宸遍悧鍫礄鐢妇绱︾€涙ɑ娲块弬甯礆
     * 
     * @param entity 鐎圭偘缍?
     * @return 缂佹挻鐏?
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int update(T entity) {
        long startTime = System.currentTimeMillis();
        String methodName = "update";
        
        try {
            // 閸欏倹鏆熸宀冪槈
            validateEntity(entity, methodName);
            
            // 鐠佸墽鐤嗛弴瀛樻煀閺冨爼妫?
            setUpdateTime(entity);
            
            // 閹笛嗩攽閺囧瓨鏌?
            int result = baseMapper.update(entity);
            
            if (result > 0) {
                // 閺囧瓨鏌婄紓鎾崇摠
                updateCache(entity);
                logger.debug("鐎圭偘缍嬮弴瀛樻煀閹存劕濮? method={}, result={}", methodName, result);
            }
            
            return result;
            
        } catch (Exception e) {
            logger.error("娣囶喗鏁肩€圭偘缍嬪鍌氱埗: method={}, error={}", methodName, e.getMessage(), e);
            // 娴滃濮熼崶鐐寸泊閻㈢洦pring缁狅紕鎮?
            throw new BusinessException("鐎圭偘缍嬫穱顔芥暭婢惰精瑙?, e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            logger.info("娣囶喗鏁肩€圭偘缍嬮懓妤佹: {}ms, method={}", duration, methodName);
        }
    }
    
    /**
     * 閸掔娀娅庣€圭偘缍嬫穱鈩冧紖 - 婢х偛宸遍悧鍫礄鐢妇绱︾€涙ɑ绔婚悶鍡礆
     * 
     * @param id 鐎圭偘缍婭D
     * @return 缂佹挻鐏?
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteById(Long id) {
        long startTime = System.currentTimeMillis();
        String methodName = "deleteById";
        
        try {
            // 閸欏倹鏆熸宀冪槈
            validateId(id, methodName);
            
            // 閸忓牊鐓＄拠銏犵杽娴ｆ搫绱欓悽銊ょ艾缂傛挸鐡ㄥ〒鍛倞閿?            T entity = selectById(id);
            
            // 閹笛嗩攽閸掔娀娅?
            int result = baseMapper.deleteById(id);
            
            if (result > 0 && entity != null) {
                // 濞撳懐鎮婄紓鎾崇摠
                clearEntityCache(id);
                clearRelatedCache(entity);
                logger.debug("鐎圭偘缍嬮崚鐘绘珟閹存劕濮? method={}, id={}, result={}", methodName, id, result);
            }
            
            return result;
            
        } catch (Exception e) {
            logger.error("閸掔娀娅庣€圭偘缍嬪鍌氱埗: method={}, id={}, error={}", methodName, id, e.getMessage(), e);
            // 娴滃濮熼崶鐐寸泊閻㈢洦pring缁狅紕鎮?
            throw new BusinessException("鐎圭偘缍嬮崚鐘绘珟婢惰精瑙?, e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            logger.info("閸掔娀娅庣€圭偘缍嬮懓妤佹: {}ms, method={}, id={}", duration, methodName, id);
        }
    }
    
    /**
     * 閹靛綊鍣洪崚鐘绘珟鐎圭偘缍?- 婢х偛宸遍悧鍫礄鐢妇绱︾€涙ɑ绔婚悶鍡礆
     * 
     * @param ids 闂団偓鐟曚礁鍨归梽銈囨畱鐎圭偘缍婭D
     * @return 缂佹挻鐏?
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteByIds(Long[] ids) {
        long startTime = System.currentTimeMillis();
        String methodName = "deleteByIds";
        
        try {
            // 閸欏倹鏆熸宀冪槈
            validateIds(ids, methodName);
            
            // 閹笛嗩攽閹靛綊鍣洪崚鐘绘珟
            int result = baseMapper.deleteByIds(ids);
            
            if (result > 0) {
                // 濞撳懐鎮婇幍鈧張澶屾祲閸忓磭绱︾€?                for (Long id : ids) {
                    clearEntityCache(id);
                }
                logger.debug("閹靛綊鍣洪崚鐘绘珟鐎圭偘缍嬮幋鎰: method={}, count={}, result={}", methodName, ids.length, result);
            }
            
            return result;
            
        } catch (Exception e) {
            logger.error("閹靛綊鍣洪崚鐘绘珟鐎圭偘缍嬪鍌氱埗: method={}, count={}, error={}", methodName, ids.length, e.getMessage(), e);
            // 娴滃濮熼崶鐐寸泊閻㈢洦pring缁狅紕鎮?
            throw new BusinessException("閹靛綊鍣洪崚鐘绘珟鐎圭偘缍嬫径杈Е", e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            logger.info("閹靛綊鍣洪崚鐘绘珟鐎圭偘缍嬮懓妤佹: {}ms, method={}, count={}", duration, methodName, ids.length);
        }
    }
    
    // ==================== 闁氨鏁ら弬瑙勭《 ====================
    
    /**
     * 瀵倹顒為幍褑顢戦悪顒傜彌娴犺濮?
     * 
     * @param task 娴犺濮?
     * @return CompletableFuture
     */
    protected CompletableFuture<Void> executeAsync(Runnable task) {
        return CompletableFuture.runAsync(task).exceptionally(throwable -> {
            logger.error("瀵倹顒炴禒璇插閹笛嗩攽婢惰精瑙? {}", throwable.getMessage(), throwable);
            return null;
        });
    }
    
    /**
     * 妤犲矁鐦塈D閸欏倹鏆熼敍鍦爋ng缁鐎烽敍?     * 
     * @param id ID
     * @param methodName 閺傝纭堕崥?     */
    protected void validateId(Long id, String methodName) {
        ValidationUtils.validateId(id, methodName);
    }
    
    /**
     * 妤犲矁鐦塈D閸欏倹鏆熼敍鍦玹ring缁鐎烽敍?     * 
     * @param id ID
     * @param methodName 閺傝纭堕崥?     */
    protected void validateId(String id, String methodName) {
        if (id == null || id.trim().isEmpty()) {
            throw new BusinessException(methodName + "閸欏倹鏆熼弮鐘虫櫏: ID娑撳秷鍏樻稉铏光敄");
        }
    }
    
    /**
     * 妤犲矁鐦塈D閺佹壆绮嶉崣鍌涙殶
     * 
     * @param ids ID閺佹壆绮?
     * @param methodName 閺傝纭堕崥?     */
    protected void validateIds(Long[] ids, String methodName) {
        ValidationUtils.validateIdArray(ids, methodName);
    }
    
    /**
     * 妤犲矁鐦夌€圭偘缍嬮崣鍌涙殶
     * 
     * @param entity 鐎圭偘缍?
     * @param methodName 閺傝纭堕崥?     */
    protected void validateEntity(T entity, String methodName) {
        if (entity == null) {
            throw new BusinessException(methodName + "閸欏倹鏆熼弮鐘虫櫏: 鐎圭偘缍嬫稉宥堝厴娑撹櫣鈹?);
        }
    }
    
    // ==================== 缂傛挸鐡ㄩ惄绋垮彠閺傝纭?====================
    
    /**
     * 閻㈢喐鍨氱€圭偘缍嬬紓鎾崇摠闁?     * 
     * @param id 鐎圭偘缍婭D
     * @return 缂傛挸鐡ㄩ柨?     */
    protected String generateEntityCacheKey(Long id) {
        return ENTITY_CACHE_PREFIX + getEntityType() + ":" + id;
    }
    
    /**
     * 閻㈢喐鍨氶崚妤勩€冪紓鎾崇摠闁?     * 
     * @param params 閸欏倹鏆?
     * @return 缂傛挸鐡ㄩ柨?     */
    protected String generateListCacheKey(Object... params) {
        StringBuilder key = new StringBuilder(LIST_CACHE_PREFIX + getEntityType() + ":");
        for (Object param : params) {
            key.append(param).append(":");
        }
        return key.toString();
    }
    
    /**
     * 缂傛挸鐡ㄧ€圭偘缍?
     * 
     * @param entity 鐎圭偘缍?
     * @param cacheKey 缂傛挸鐡ㄩ柨?     */
    protected void cacheEntity(T entity, String cacheKey) {
        if (entity != null && cacheKey != null) {
            redisTemplate.opsForValue().set(cacheKey, entity, DEFAULT_CACHE_TIMEOUT, TimeUnit.MINUTES);
        }
    }
    
    /**
     * 閺囧瓨鏌婄紓鎾崇摠
     * 
     * @param entity 鐎圭偘缍?
     */
    protected void updateCache(T entity) {
        try {
            // 閼惧嘲褰囩€圭偘缍婭D閿涘牓娓剁憰浣哥摍缁鐤勯悳甯礆
            Long id = getEntityId(entity);
            if (id != null) {
                String cacheKey = generateEntityCacheKey(id);
                cacheEntity(entity, cacheKey);
            }
        } catch (Exception e) {
            logger.warn("閺囧瓨鏌婄紓鎾崇摠婢惰精瑙? {}", e.getMessage());
        }
    }
    
    /**
     * 濞撳懐鎮婄€圭偘缍嬬紓鎾崇摠
     * 
     * @param id 鐎圭偘缍婭D
     */
    protected void clearEntityCache(Long id) {
        try {
            String cacheKey = generateEntityCacheKey(id);
            cacheUtils.clearCache(cacheKey);
        } catch (Exception e) {
            logger.warn("濞撳懐鎮婄€圭偘缍嬬紓鎾崇摠婢惰精瑙? id={}, error={}", id, e.getMessage());
        }
    }
    
    /**
     * 濞撳懐鎮婇惄绋垮彠缂傛挸鐡?
     * 
     * @param entity 鐎圭偘缍?
     */
    protected void clearRelatedCache(T entity) {
        try {
            // 姒涙顓荤€圭偟骞囬敍姘閻炲棗鐤勬担鎾剁处鐎?            Long id = getEntityId(entity);
            if (id != null) {
                clearEntityCache(id);
            }
        } catch (Exception e) {
            logger.warn("濞撳懐鎮婇惄绋垮彠缂傛挸鐡ㄦ径杈Е: {}", e.getMessage());
        }
    }
    
    // ==================== 閹跺€熻杽閺傝纭堕敍鍫ユ付鐟曚礁鐡欑猾璇茬杽閻滃府绱?====================
    
    /**
     * 閼惧嘲褰囩€圭偘缍嬬猾璇茬€烽崥宥囆?
     * 
     * @return 鐎圭偘缍嬬猾璇茬€烽崥宥囆?
     */
    protected abstract String getEntityType();
    
    /**
     * 閼惧嘲褰囩€圭偘缍婭D
     * 
     * @param entity 鐎圭偘缍?
     * @return 鐎圭偘缍婭D
     */
    protected abstract Long getEntityId(T entity);
    
    /**
     * 鐠佸墽鐤嗛崚娑樼紦閺冨爼妫?
     * 
     * @param entity 鐎圭偘缍?
     */
    protected abstract void setCreateTime(T entity);
    
    /**
     * 鐠佸墽鐤嗛弴瀛樻煀閺冨爼妫?
     * 
     * @param entity 鐎圭偘缍?
     */
    protected abstract void setUpdateTime(T entity);
}
