package com.ruoyi.im.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.im.mapper.ImFileAssetMapper;
import com.ruoyi.im.domain.ImFileAsset;
import com.ruoyi.im.service.ImFileAssetService;
import com.ruoyi.im.exception.BusinessException;

/**
 * 鏂囦欢璧勬簮Service涓氬姟灞傚鐞?- 浼樺寲鐗堟湰
 * 浼樺寲鍐呭锛氭坊鍔犵紦瀛樻満鍒躲€佷簨鍔℃帶鍒躲€佹€ц兘鐩戞帶銆侀敊璇鐞? * 
 * @author ruoyi
 */
@Service("imFileAssetServiceImpl")
public class ImFileAssetServiceImpl extends EnhancedBaseServiceImpl<ImFileAsset, ImFileAssetMapper> implements ImFileAssetService {
    private static final Logger log = LoggerFactory.getLogger(ImFileAssetServiceImpl.class);
    
    @Autowired
    private ImFileAssetMapper imFileAssetMapper;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    // 缂撳瓨閿墠缂€
    private static final String FILE_ASSET_CACHE_PREFIX = "im:file:asset:";
    private static final String FILE_MD5_CACHE_PREFIX = "im:file:md5:";
    private static final String USER_FILES_CACHE_PREFIX = "im:user:files:";
    
    // 缂撳瓨瓒呮椂鏃堕棿锛堝垎閽燂級
    private static final int CACHE_TIMEOUT_MINUTES = 60; // 鏂囦欢璧勬簮缂撳瓨鏃堕棿鐩稿杈冮暱
    
    /**
     * 鏌ヨ鏂囦欢璧勬簮鍒楄〃锛堟敮鎸佸垎椤碉級
     * 
     * @param imFileAsset 鏂囦欢璧勬簮鏉′欢
     * @return 鏂囦欢璧勬簮闆嗗悎
     */
    @Override
    public List<ImFileAsset> selectList(ImFileAsset imFileAsset) {
        long startTime = System.currentTimeMillis();
        String methodName = "selectList";
        
        try {
            log.debug("鏌ヨ鏂囦欢璧勬簮鍒楄〃: params={}, method={}", imFileAsset, methodName);
            
            // 鍩虹鍒楄〃鏌ヨ閫昏緫鐢辩埗绫诲疄鐜?            List<ImFileAsset> result = super.selectList(imFileAsset);
            
            log.debug("鏌ヨ鏂囦欢璧勬簮鍒楄〃瀹屾垚: count={}, method={}", result != null ? result.size() : 0, methodName);
            return result;
            
        } catch (Exception e) {
            log.error("鏌ヨ鏂囦欢璧勬簮鍒楄〃寮傚父: params={}, error={}, method={}", imFileAsset, e.getMessage(), methodName, e);
            throw new BusinessException("鏌ヨ鏂囦欢璧勬簮鍒楄〃澶辫触", e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("鏌ヨ鏂囦欢璧勬簮鍒楄〃鑰楁椂: {}ms, method={}", duration, methodName);
        }
    }
    
    /**
     * 鏂板鏂囦欢璧勬簮
     * 
     * @param imFileAsset 鏂囦欢璧勬簮淇℃伅
     * @return 缁撴灉
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insert(ImFileAsset imFileAsset) {
        long startTime = System.currentTimeMillis();
        String methodName = "insert";
        
        try {
            log.debug("鏂板鏂囦欢璧勬簮: fileName={}, method={}", imFileAsset != null ? imFileAsset.getFileName() : null, methodName);
            
            // 璁剧疆鍒涘缓鏃堕棿
            setCreateTime(imFileAsset);
            
            // 鍩虹鎻掑叆閫昏緫鐢辩埗绫诲疄鐜?            int result = super.insert(imFileAsset);
            
            if (result > 0) {
                log.info("鏂板鏂囦欢璧勬簮鎴愬姛: fileId={}, fileName={}, fileSize={}, result={}, method={}", 
                         imFileAsset.getId(), imFileAsset.getFileName(), imFileAsset.getFileSize(), result, methodName);
                
                // 娓呴櫎鐢ㄦ埛鏂囦欢鍒楄〃缂撳瓨
                if (imFileAsset.getUploaderId() != null) {
                    clearUserFilesCache(imFileAsset.getUploaderId());
                }
                
                // 缂撳瓨MD5淇℃伅
                if (imFileAsset.getMd5() != null) {
                    cacheFileByMd5(imFileAsset);
                }
            } else {
                log.warn("鏂板鏂囦欢璧勬簮澶辫触: fileName={}, result={}, method={}", imFileAsset.getFileName(), result, methodName);
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("鏂板鏂囦欢璧勬簮寮傚父: fileName={}, error={}, method={}", imFileAsset.getFileName(), e.getMessage(), methodName, e);
            throw new BusinessException("鏂板鏂囦欢璧勬簮澶辫触", e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("鏂板鏂囦欢璧勬簮鑰楁椂: {}ms, fileName={}, method={}", duration, imFileAsset.getFileName(), methodName);
        }
    }
    
    /**
     * 淇敼鏂囦欢璧勬簮
     * 
     * @param imFileAsset 鏂囦欢璧勬簮淇℃伅
     * @return 缁撴灉
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int update(ImFileAsset imFileAsset) {
        long startTime = System.currentTimeMillis();
        String methodName = "update";
        
        try {
            log.debug("淇敼鏂囦欢璧勬簮: fileId={}, fileName={}, method={}", 
                      imFileAsset.getId(), imFileAsset.getFileName(), methodName);
            
            // 璁剧疆鏇存柊鏃堕棿
            setUpdateTime(imFileAsset);
            
            // 鍩虹淇敼閫昏緫鐢辩埗绫诲疄鐜?            int result = super.update(imFileAsset);
            
            if (result > 0) {
                log.info("淇敼鏂囦欢璧勬簮鎴愬姛: fileId={}, fileName={}, result={}, method={}", 
                         imFileAsset.getId(), imFileAsset.getFileName(), result, methodName);
                
                // 娓呴櫎鐩稿叧缂撳瓨
                clearRelatedCache(imFileAsset);
            } else {
                log.warn("淇敼鏂囦欢璧勬簮澶辫触: fileId={}, fileName={}, result={}, method={}", 
                         imFileAsset.getId(), imFileAsset.getFileName(), result, methodName);
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("淇敼鏂囦欢璧勬簮寮傚父: fileId={}, fileName={}, error={}, method={}", 
                      imFileAsset.getId(), imFileAsset.getFileName(), e.getMessage(), methodName, e);
            throw new BusinessException("淇敼鏂囦欢璧勬簮澶辫触", e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("淇敼鏂囦欢璧勬簮鑰楁椂: {}ms, fileId={}, method={}", duration, imFileAsset.getId(), methodName);
        }
    }
    
    /**
     * 鎵归噺鍒犻櫎鏂囦欢璧勬簮
     * 
     * @param ids 闇€瑕佸垹闄ょ殑鏂囦欢璧勬簮ID
     * @return 缁撴灉
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteByIds(Long[] ids) {
        long startTime = System.currentTimeMillis();
        String methodName = "deleteByIds";
        
        try {
            log.debug("鎵归噺鍒犻櫎鏂囦欢璧勬簮: ids={}, count={}, method={}", ids, ids != null ? ids.length : 0, methodName);
            
            if (ids == null || ids.length == 0) {
                log.warn("鎵归噺鍒犻櫎鏂囦欢璧勬簮鍙傛暟鏃犳晥: ids={}, method={}", ids, methodName);
                throw new BusinessException("鎵归噺鍒犻櫎鏂囦欢璧勬簮鍙傛暟鏃犳晥");
            }
            
            // 鎵归噺鍒犻櫎鍓嶅厛鏌ヨ鐩稿叧鏂囦欢淇℃伅锛岀敤浜庣紦瀛樻竻鐞?            List<ImFileAsset> fileAssets = null;
            for (Long id : ids) {
                ImFileAsset fileAsset = selectById(id);
                if (fileAsset != null) {
                    if (fileAssets == null) {
                        fileAssets = new java.util.ArrayList<>();
                    }
                    fileAssets.add(fileAsset);
                }
            }
            
            // 鍩虹鎵归噺鍒犻櫎閫昏緫鐢辩埗绫诲疄鐜?            int result = super.deleteByIds(ids);
            
            if (result > 0) {
                log.info("鎵归噺鍒犻櫎鏂囦欢璧勬簮鎴愬姛: count={}, result={}, method={}", ids.length, result, methodName);
                
                // 娓呴櫎鐩稿叧缂撳瓨
                if (fileAssets != null) {
                    for (ImFileAsset fileAsset : fileAssets) {
                        clearRelatedCache(fileAsset);
                    }
                }
            } else {
                log.warn("鎵归噺鍒犻櫎鏂囦欢璧勬簮澶辫触: ids={}, count={}, result={}, method={}", ids, ids.length, result, methodName);
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("鎵归噺鍒犻櫎鏂囦欢璧勬簮寮傚父: ids={}, error={}, method={}", ids, e.getMessage(), methodName, e);
            throw new BusinessException("鎵归噺鍒犻櫎鏂囦欢璧勬簮澶辫触", e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("鎵归噺鍒犻櫎鏂囦欢璧勬簮鑰楁椂: {}ms, ids={}, count={}, method={}", duration, ids, ids != null ? ids.length : 0, methodName);
        }
    }
    
    /**
     * 鍒犻櫎鏂囦欢璧勬簮淇℃伅
     * 
     * @param id 鏂囦欢璧勬簮ID
     * @return 缁撴灉
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteById(Long id) {
        long startTime = System.currentTimeMillis();
        String methodName = "deleteById";
        
        try {
            log.debug("鍒犻櫎鏂囦欢璧勬簮: fileId={}, method={}", id, methodName);
            
            // 鍏堟煡璇㈡枃浠朵俊鎭紝鐢ㄤ簬缂撳瓨娓呯悊
            ImFileAsset fileAsset = selectById(id);
            
            // 鍩虹鍒犻櫎閫昏緫鐢辩埗绫诲疄鐜?            int result = super.deleteById(id);
            
            if (result > 0) {
                log.info("鍒犻櫎鏂囦欢璧勬簮鎴愬姛: fileId={}, result={}, method={}", id, result, methodName);
                
                // 娓呴櫎鐩稿叧缂撳瓨
                if (fileAsset != null) {
                    clearRelatedCache(fileAsset);
                }
            } else {
                log.warn("鍒犻櫎鏂囦欢璧勬簮澶辫触: fileId={}, result={}, method={}", id, result, methodName);
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("鍒犻櫎鏂囦欢璧勬簮寮傚父: fileId={}, error={}, method={}", id, e.getMessage(), methodName, e);
            throw new BusinessException("鍒犻櫎鏂囦欢璧勬簮澶辫触", e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("鍒犻櫎鏂囦欢璧勬簮鑰楁椂: {}ms, fileId={}, method={}", duration, id, methodName);
        }
    }
    
    /**
     * 鏍规嵁MD5鍊兼煡璇㈡枃浠惰祫婧?     * 
     * @param md5 鏂囦欢MD5鍊?     * @return 鏂囦欢璧勬簮
     */
    public ImFileAsset selectImFileAssetByMd5(String md5) {
        long startTime = System.currentTimeMillis();
        String methodName = "selectImFileAssetByMd5";
        
        try {
            // 鍙傛暟楠岃瘉
            if (md5 == null || md5.trim().isEmpty()) {
                throw new BusinessException(methodName + "鍙傛暟鏃犳晥: MD5涓嶈兘涓虹┖");
            }
            
            log.debug("鏍规嵁MD5鏌ヨ鏂囦欢璧勬簮: md5={}, method={}", md5, methodName);
            
            // 鐢熸垚缂撳瓨閿?            String cacheKey = FILE_MD5_CACHE_PREFIX + md5;
            
            // 妫€鏌ョ紦瀛?            @SuppressWarnings("unchecked")
            ImFileAsset cachedFile = (ImFileAsset) redisTemplate.opsForValue().get(cacheKey);
            if (cachedFile != null) {
                log.debug("浠庣紦瀛樿幏鍙栨枃浠惰祫婧? md5={}, fileId={}, method={}", md5, cachedFile.getId(), methodName);
                return cachedFile;
            }
            
            // 鏋勫缓鏌ヨ鏉′欢
            ImFileAsset query = new ImFileAsset();
            query.setMd5(md5);
            
            // 鏌ヨ鏁版嵁搴?            List<ImFileAsset> fileAssets = selectList(query);
            ImFileAsset fileAsset = fileAssets != null && !fileAssets.isEmpty() ? fileAssets.get(0) : null;
            
            // 缂撳瓨缁撴灉
            if (fileAsset != null) {
                redisTemplate.opsForValue().set(cacheKey, fileAsset, CACHE_TIMEOUT_MINUTES, java.util.concurrent.TimeUnit.MINUTES);
                log.debug("鏂囦欢璧勬簮宸茬紦瀛? md5={}, fileId={}, method={}", md5, fileAsset.getId(), methodName);
            }
            
            return fileAsset;
            
        } catch (Exception e) {
            log.error("鏍规嵁MD5鏌ヨ鏂囦欢璧勬簮寮傚父: md5={}, error={}, method={}", md5, e.getMessage(), methodName, e);
            throw new BusinessException("鏍规嵁MD5鏌ヨ鏂囦欢璧勬簮澶辫触", e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("鏍规嵁MD5鏌ヨ鏂囦欢璧勬簮鑰楁椂: {}ms, md5={}, method={}", duration, md5, methodName);
        }
    }
    
    /**
     * 鏍规嵁鐢ㄦ埛ID鏌ヨ鐢ㄦ埛涓婁紶鐨勬枃浠惰祫婧愬垪琛?     * 
     * @param userId 鐢ㄦ埛ID
     * @return 鏂囦欢璧勬簮闆嗗悎
     */
    public List<ImFileAsset> selectImFileAssetListByUploaderId(Long userId) {
        long startTime = System.currentTimeMillis();
        String methodName = "selectImFileAssetListByUploaderId";
        
        try {
            // 鍙傛暟楠岃瘉
            validateId(userId, methodName);
            
            log.debug("鏍规嵁鐢ㄦ埛ID鏌ヨ鏂囦欢璧勬簮鍒楄〃: userId={}, method={}", userId, methodName);
            
            // 鐢熸垚缂撳瓨閿?            String cacheKey = USER_FILES_CACHE_PREFIX + userId;
            
            // 妫€鏌ョ紦瀛?            @SuppressWarnings("unchecked")
            List<ImFileAsset> cachedFiles = (List<ImFileAsset>) redisTemplate.opsForValue().get(cacheKey);
            if (cachedFiles != null && !cachedFiles.isEmpty()) {
                log.debug("浠庣紦瀛樿幏鍙栫敤鎴锋枃浠惰祫婧愬垪琛? userId={}, count={}, method={}", userId, cachedFiles.size(), methodName);
                return cachedFiles;
            }
            
            // 鏋勫缓鏌ヨ鏉′欢
            ImFileAsset query = new ImFileAsset();
            query.setUploaderId(userId);
            
            // 鏌ヨ鏁版嵁搴?            List<ImFileAsset> fileAssets = selectList(query);
            
            // 缂撳瓨缁撴灉
            if (fileAssets != null && !fileAssets.isEmpty()) {
                redisTemplate.opsForValue().set(cacheKey, fileAssets, CACHE_TIMEOUT_MINUTES, java.util.concurrent.TimeUnit.MINUTES);
                log.debug("鐢ㄦ埛鏂囦欢璧勬簮鍒楄〃宸茬紦瀛? userId={}, count={}, method={}", userId, fileAssets.size(), methodName);
            }
            
            return fileAssets;
            
        } catch (Exception e) {
            log.error("鏍规嵁鐢ㄦ埛ID鏌ヨ鏂囦欢璧勬簮鍒楄〃寮傚父: userId={}, error={}, method={}", userId, e.getMessage(), methodName, e);
            throw new BusinessException("鏍规嵁鐢ㄦ埛ID鏌ヨ鏂囦欢璧勬簮鍒楄〃澶辫触", e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("鏍规嵁鐢ㄦ埛ID鏌ヨ鏂囦欢璧勬簮鍒楄〃鑰楁椂: {}ms, userId={}, method={}", duration, userId, methodName);
        }
    }
    
    /**
     * 鏍规嵁MD5缂撳瓨鏂囦欢璧勬簮
     * 
     * @param fileAsset 鏂囦欢璧勬簮
     */
    private void cacheFileByMd5(ImFileAsset fileAsset) {
        if (fileAsset != null && fileAsset.getMd5() != null) {
            String cacheKey = FILE_MD5_CACHE_PREFIX + fileAsset.getMd5();
            redisTemplate.opsForValue().set(cacheKey, fileAsset, CACHE_TIMEOUT_MINUTES, java.util.concurrent.TimeUnit.MINUTES);
            log.debug("鏂囦欢璧勬簮MD5缂撳瓨宸叉洿鏂? fileId={}, md5={}", fileAsset.getId(), fileAsset.getMd5());
        }
    }
    
    /**
     * 娓呴櫎鐢ㄦ埛鏂囦欢鍒楄〃缂撳瓨
     * 
     * @param userId 鐢ㄦ埛ID
     */
    private void clearUserFilesCache(Long userId) {
        if (userId != null) {
            String cacheKey = USER_FILES_CACHE_PREFIX + userId;
            redisTemplate.delete(cacheKey);
            log.debug("鐢ㄦ埛鏂囦欢鍒楄〃缂撳瓨宸叉竻闄? userId={}", userId);
        }
    }
    
    /**
     * 瀹炵幇EnhancedBaseServiceImpl鐨勬娊璞℃柟娉?     * 
     * @return 瀹炰綋绫诲瀷鍚嶇О
     */
    @Override
    protected String getEntityType() {
        return "fileAsset";
    }
    
    /**
     * 瀹炵幇EnhancedBaseServiceImpl鐨勬娊璞℃柟娉?     * 
     * @param entity 鏂囦欢璧勬簮瀹炰綋
     * @return 鏂囦欢璧勬簮ID
     */
    @Override
    protected Long getEntityId(ImFileAsset entity) {
        return entity != null ? entity.getId() : null;
    }
    
    /**
     * 瀹炵幇EnhancedBaseServiceImpl鐨勬娊璞℃柟娉?     * 
     * @param entity 鏂囦欢璧勬簮瀹炰綋
     */
    @Override
    protected void setCreateTime(ImFileAsset entity) {
        if (entity != null && entity.getCreateTime() == null) {
            entity.setCreateTime(LocalDateTime.now());
        }
    }
    
    /**
     * 瀹炵幇EnhancedBaseServiceImpl鐨勬娊璞℃柟娉?     * 
     * @param entity 鏂囦欢璧勬簮瀹炰綋
     */
    @Override
    protected void setUpdateTime(ImFileAsset entity) {
        if (entity != null) {
            // 鏂囦欢璧勬簮閫氬父涓嶉渶瑕佹洿鏂版椂闂达紝浣嗚繖閲屾彁渚涚粺涓€鐨勬帴鍙?            // 濡傛灉闇€瑕佹洿鏂帮紝鍙互鍙栨秷涓嬮潰鐨勬敞閲?            // entity.setUpdateTime(LocalDateTime.now());
        }
    }
    
    /**
     * 瀹炵幇EnhancedBaseServiceImpl涓殑clearRelatedCache鏂规硶锛屾彁渚涙枃浠惰祫婧愮壒瀹氱紦瀛樻竻鐞嗛€昏緫
     * 
     * @param entity 鏂囦欢璧勬簮瀹炰綋
     */
    @Override
    protected void clearRelatedCache(ImFileAsset entity) {
        if (entity != null) {
            // 娓呴櫎瀹炰綋缂撳瓨
            clearEntityCache(entity.getId());
            
            // 娓呴櫎MD5缂撳瓨
            if (entity.getMd5() != null) {
                redisTemplate.delete(FILE_MD5_CACHE_PREFIX + entity.getMd5());
            }
            
            // 娓呴櫎鐢ㄦ埛鏂囦欢鍒楄〃缂撳瓨
            if (entity.getUploaderId() != null) {
                clearUserFilesCache(entity.getUploaderId());
            }
        }
    }
}
