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
 * 閺傚洣娆㈢挧鍕爱Service娑撴艾濮熺仦鍌氼槱閻?- 娴兼ê瀵查悧鍫熸拱
 * 娴兼ê瀵查崘鍛啇閿涙碍鍧婇崝鐘电处鐎涙ɑ婧€閸掕翰鈧椒绨ㄩ崝鈩冨付閸掕翰鈧焦鈧嗗厴閻╂垶甯堕妴渚€鏁婄拠顖氼槱閻? * 
 * @author ruoyi
 */
@Service("imFileAssetServiceImpl")
public class ImFileAssetServiceImpl extends EnhancedBaseServiceImpl<ImFileAsset, ImFileAssetMapper> implements ImFileAssetService {
    private static final Logger log = LoggerFactory.getLogger(ImFileAssetServiceImpl.class);
    
    @Autowired
    private ImFileAssetMapper imFileAssetMapper;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    // 缂傛挸鐡ㄩ柨顔煎缂傗偓
    private static final String FILE_ASSET_CACHE_PREFIX = "im:file:asset:";
    private static final String FILE_MD5_CACHE_PREFIX = "im:file:md5:";
    private static final String USER_FILES_CACHE_PREFIX = "im:user:files:";
    
    // 缂傛挸鐡ㄧ搾鍛閺冨爼妫块敍鍫濆瀻闁界噦绱?
    private static final int CACHE_TIMEOUT_MINUTES = 60; // 閺傚洣娆㈢挧鍕爱缂傛挸鐡ㄩ弮鍫曟？閻╃顕潏鍐毐
    
    /**
     * 閺屻儴顕楅弬鍥︽鐠у嫭绨崚妤勩€冮敍鍫熸暜閹镐礁鍨庢い纰夌礆
     * 
     * @param imFileAsset 閺傚洣娆㈢挧鍕爱閺夆€叉
     * @return 閺傚洣娆㈢挧鍕爱闂嗗棗鎮?
     */
    @Override
    public List<ImFileAsset> selectList(ImFileAsset imFileAsset) {
        long startTime = System.currentTimeMillis();
        String methodName = "selectList";
        
        try {
            log.debug("閺屻儴顕楅弬鍥︽鐠у嫭绨崚妤勩€? params={}, method={}", imFileAsset, methodName);
            
            // 閸╄櫣顢呴崚妤勩€冮弻銉嚄闁槒绶悽杈╁煑缁鐤勯悳?            List<ImFileAsset> result = super.selectList(imFileAsset);
            
            log.debug("閺屻儴顕楅弬鍥︽鐠у嫭绨崚妤勩€冪€瑰本鍨? count={}, method={}", result != null ? result.size() : 0, methodName);
            return result;
            
        } catch (Exception e) {
            log.error("閺屻儴顕楅弬鍥︽鐠у嫭绨崚妤勩€冨鍌氱埗: params={}, error={}, method={}", imFileAsset, e.getMessage(), methodName, e);
            throw new BusinessException("閺屻儴顕楅弬鍥︽鐠у嫭绨崚妤勩€冩径杈Е", e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("閺屻儴顕楅弬鍥︽鐠у嫭绨崚妤勩€冮懓妤佹: {}ms, method={}", duration, methodName);
        }
    }
    
    /**
     * 閺傛澘顤冮弬鍥︽鐠у嫭绨?
     * 
     * @param imFileAsset 閺傚洣娆㈢挧鍕爱娣団剝浼?
     * @return 缂佹挻鐏?
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insert(ImFileAsset imFileAsset) {
        long startTime = System.currentTimeMillis();
        String methodName = "insert";
        
        try {
            log.debug("閺傛澘顤冮弬鍥︽鐠у嫭绨? fileName={}, method={}", imFileAsset != null ? imFileAsset.getFileName() : null, methodName);
            
            // 鐠佸墽鐤嗛崚娑樼紦閺冨爼妫?
            setCreateTime(imFileAsset);
            
            // 閸╄櫣顢呴幓鎺戝弳闁槒绶悽杈╁煑缁鐤勯悳?            int result = super.insert(imFileAsset);
            
            if (result > 0) {
                log.info("閺傛澘顤冮弬鍥︽鐠у嫭绨幋鎰: fileId={}, fileName={}, fileSize={}, result={}, method={}", 
                         imFileAsset.getId(), imFileAsset.getFileName(), imFileAsset.getFileSize(), result, methodName);
                
                // 濞撳懘娅庨悽銊﹀煕閺傚洣娆㈤崚妤勩€冪紓鎾崇摠
                if (imFileAsset.getUploaderId() != null) {
                    clearUserFilesCache(imFileAsset.getUploaderId());
                }
                
                // 缂傛挸鐡∕D5娣団剝浼?
                if (imFileAsset.getMd5() != null) {
                    cacheFileByMd5(imFileAsset);
                }
            } else {
                log.warn("閺傛澘顤冮弬鍥︽鐠у嫭绨径杈Е: fileName={}, result={}, method={}", imFileAsset.getFileName(), result, methodName);
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("閺傛澘顤冮弬鍥︽鐠у嫭绨鍌氱埗: fileName={}, error={}, method={}", imFileAsset.getFileName(), e.getMessage(), methodName, e);
            throw new BusinessException("閺傛澘顤冮弬鍥︽鐠у嫭绨径杈Е", e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("閺傛澘顤冮弬鍥︽鐠у嫭绨懓妤佹: {}ms, fileName={}, method={}", duration, imFileAsset.getFileName(), methodName);
        }
    }
    
    /**
     * 娣囶喗鏁奸弬鍥︽鐠у嫭绨?
     * 
     * @param imFileAsset 閺傚洣娆㈢挧鍕爱娣団剝浼?
     * @return 缂佹挻鐏?
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int update(ImFileAsset imFileAsset) {
        long startTime = System.currentTimeMillis();
        String methodName = "update";
        
        try {
            log.debug("娣囶喗鏁奸弬鍥︽鐠у嫭绨? fileId={}, fileName={}, method={}", 
                      imFileAsset.getId(), imFileAsset.getFileName(), methodName);
            
            // 鐠佸墽鐤嗛弴瀛樻煀閺冨爼妫?
            setUpdateTime(imFileAsset);
            
            // 閸╄櫣顢呮穱顔芥暭闁槒绶悽杈╁煑缁鐤勯悳?            int result = super.update(imFileAsset);
            
            if (result > 0) {
                log.info("娣囶喗鏁奸弬鍥︽鐠у嫭绨幋鎰: fileId={}, fileName={}, result={}, method={}", 
                         imFileAsset.getId(), imFileAsset.getFileName(), result, methodName);
                
                // 濞撳懘娅庨惄绋垮彠缂傛挸鐡?
                clearRelatedCache(imFileAsset);
            } else {
                log.warn("娣囶喗鏁奸弬鍥︽鐠у嫭绨径杈Е: fileId={}, fileName={}, result={}, method={}", 
                         imFileAsset.getId(), imFileAsset.getFileName(), result, methodName);
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("娣囶喗鏁奸弬鍥︽鐠у嫭绨鍌氱埗: fileId={}, fileName={}, error={}, method={}", 
                      imFileAsset.getId(), imFileAsset.getFileName(), e.getMessage(), methodName, e);
            throw new BusinessException("娣囶喗鏁奸弬鍥︽鐠у嫭绨径杈Е", e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("娣囶喗鏁奸弬鍥︽鐠у嫭绨懓妤佹: {}ms, fileId={}, method={}", duration, imFileAsset.getId(), methodName);
        }
    }
    
    /**
     * 閹靛綊鍣洪崚鐘绘珟閺傚洣娆㈢挧鍕爱
     * 
     * @param ids 闂団偓鐟曚礁鍨归梽銈囨畱閺傚洣娆㈢挧鍕爱ID
     * @return 缂佹挻鐏?
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteByIds(Long[] ids) {
        long startTime = System.currentTimeMillis();
        String methodName = "deleteByIds";
        
        try {
            log.debug("閹靛綊鍣洪崚鐘绘珟閺傚洣娆㈢挧鍕爱: ids={}, count={}, method={}", ids, ids != null ? ids.length : 0, methodName);
            
            if (ids == null || ids.length == 0) {
                log.warn("閹靛綊鍣洪崚鐘绘珟閺傚洣娆㈢挧鍕爱閸欏倹鏆熼弮鐘虫櫏: ids={}, method={}", ids, methodName);
                throw new BusinessException("閹靛綊鍣洪崚鐘绘珟閺傚洣娆㈢挧鍕爱閸欏倹鏆熼弮鐘虫櫏");
            }
            
            // 閹靛綊鍣洪崚鐘绘珟閸撳秴鍘涢弻銉嚄閻╃鍙ч弬鍥︽娣団剝浼呴敍宀€鏁ゆ禍搴ｇ处鐎涙ɑ绔婚悶?            List<ImFileAsset> fileAssets = null;
            for (Long id : ids) {
                ImFileAsset fileAsset = selectById(id);
                if (fileAsset != null) {
                    if (fileAssets == null) {
                        fileAssets = new java.util.ArrayList<>();
                    }
                    fileAssets.add(fileAsset);
                }
            }
            
            // 閸╄櫣顢呴幍褰掑櫤閸掔娀娅庨柅鏄忕帆閻㈣京鍩楃猾璇茬杽閻?            int result = super.deleteByIds(ids);
            
            if (result > 0) {
                log.info("閹靛綊鍣洪崚鐘绘珟閺傚洣娆㈢挧鍕爱閹存劕濮? count={}, result={}, method={}", ids.length, result, methodName);
                
                // 濞撳懘娅庨惄绋垮彠缂傛挸鐡?
                if (fileAssets != null) {
                    for (ImFileAsset fileAsset : fileAssets) {
                        clearRelatedCache(fileAsset);
                    }
                }
            } else {
                log.warn("閹靛綊鍣洪崚鐘绘珟閺傚洣娆㈢挧鍕爱婢惰精瑙? ids={}, count={}, result={}, method={}", ids, ids.length, result, methodName);
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("閹靛綊鍣洪崚鐘绘珟閺傚洣娆㈢挧鍕爱瀵倸鐖? ids={}, error={}, method={}", ids, e.getMessage(), methodName, e);
            throw new BusinessException("閹靛綊鍣洪崚鐘绘珟閺傚洣娆㈢挧鍕爱婢惰精瑙?", e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("閹靛綊鍣洪崚鐘绘珟閺傚洣娆㈢挧鍕爱閼版妞? {}ms, ids={}, count={}, method={}", duration, ids, ids != null ? ids.length : 0, methodName);
        }
    }
    
    /**
     * 閸掔娀娅庨弬鍥︽鐠у嫭绨穱鈩冧紖
     * 
     * @param id 閺傚洣娆㈢挧鍕爱ID
     * @return 缂佹挻鐏?
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteById(Long id) {
        long startTime = System.currentTimeMillis();
        String methodName = "deleteById";
        
        try {
            log.debug("閸掔娀娅庨弬鍥︽鐠у嫭绨? fileId={}, method={}", id, methodName);
            
            // 閸忓牊鐓＄拠銏℃瀮娴犳湹淇婇幁顖ょ礉閻劋绨紓鎾崇摠濞撳懐鎮?
            ImFileAsset fileAsset = selectById(id);
            
            // 閸╄櫣顢呴崚鐘绘珟闁槒绶悽杈╁煑缁鐤勯悳?            int result = super.deleteById(id);
            
            if (result > 0) {
                log.info("閸掔娀娅庨弬鍥︽鐠у嫭绨幋鎰: fileId={}, result={}, method={}", id, result, methodName);
                
                // 濞撳懘娅庨惄绋垮彠缂傛挸鐡?
                if (fileAsset != null) {
                    clearRelatedCache(fileAsset);
                }
            } else {
                log.warn("閸掔娀娅庨弬鍥︽鐠у嫭绨径杈Е: fileId={}, result={}, method={}", id, result, methodName);
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("閸掔娀娅庨弬鍥︽鐠у嫭绨鍌氱埗: fileId={}, error={}, method={}", id, e.getMessage(), methodName, e);
            throw new BusinessException("閸掔娀娅庨弬鍥︽鐠у嫭绨径杈Е", e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("閸掔娀娅庨弬鍥︽鐠у嫭绨懓妤佹: {}ms, fileId={}, method={}", duration, id, methodName);
        }
    }
    
    /**
     * 閺嶈宓丮D5閸婂吋鐓＄拠銏℃瀮娴犳儼绁┃?     * 
     * @param md5 閺傚洣娆D5閸?     * @return 閺傚洣娆㈢挧鍕爱
     */
    public ImFileAsset selectImFileAssetByMd5(String md5) {
        long startTime = System.currentTimeMillis();
        String methodName = "selectImFileAssetByMd5";
        
        try {
            // 閸欏倹鏆熸宀冪槈
            if (md5 == null || md5.trim().isEmpty()) {
                throw new BusinessException(methodName + "閸欏倹鏆熼弮鐘虫櫏: MD5娑撳秷鍏樻稉铏光敄");
            }
            
            log.debug("閺嶈宓丮D5閺屻儴顕楅弬鍥︽鐠у嫭绨? md5={}, method={}", md5, methodName);
            
            // 閻㈢喐鍨氱紓鎾崇摠闁?            String cacheKey = FILE_MD5_CACHE_PREFIX + md5;
            
            // 濡偓閺屻儳绱︾€?            @SuppressWarnings("unchecked")
            ImFileAsset cachedFile = (ImFileAsset) redisTemplate.opsForValue().get(cacheKey);
            if (cachedFile != null) {
                log.debug("娴犲海绱︾€涙骞忛崣鏍ㄦ瀮娴犳儼绁┃? md5={}, fileId={}, method={}", md5, cachedFile.getId(), methodName);
                return cachedFile;
            }
            
            // 閺嬪嫬缂撻弻銉嚄閺夆€叉
            ImFileAsset query = new ImFileAsset();
            query.setMd5(md5);
            
            // 閺屻儴顕楅弫鐗堝祦鎼?            List<ImFileAsset> fileAssets = selectList(query);
            ImFileAsset fileAsset = fileAssets != null && !fileAssets.isEmpty() ? fileAssets.get(0) : null;
            
            // 缂傛挸鐡ㄧ紒鎾寸亯
            if (fileAsset != null) {
                redisTemplate.opsForValue().set(cacheKey, fileAsset, CACHE_TIMEOUT_MINUTES, java.util.concurrent.TimeUnit.MINUTES);
                log.debug("閺傚洣娆㈢挧鍕爱瀹歌尙绱︾€? md5={}, fileId={}, method={}", md5, fileAsset.getId(), methodName);
            }
            
            return fileAsset;
            
        } catch (Exception e) {
            log.error("閺嶈宓丮D5閺屻儴顕楅弬鍥︽鐠у嫭绨鍌氱埗: md5={}, error={}, method={}", md5, e.getMessage(), methodName, e);
            throw new BusinessException("閺嶈宓丮D5閺屻儴顕楅弬鍥︽鐠у嫭绨径杈Е", e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("閺嶈宓丮D5閺屻儴顕楅弬鍥︽鐠у嫭绨懓妤佹: {}ms, md5={}, method={}", duration, md5, methodName);
        }
    }
    
    /**
     * 閺嶈宓侀悽銊﹀煕ID閺屻儴顕楅悽銊﹀煕娑撳﹣绱堕惃鍕瀮娴犳儼绁┃鎰灙鐞?     * 
     * @param userId 閻劍鍩汭D
     * @return 閺傚洣娆㈢挧鍕爱闂嗗棗鎮?
     */
    public List<ImFileAsset> selectImFileAssetListByUploaderId(Long userId) {
        long startTime = System.currentTimeMillis();
        String methodName = "selectImFileAssetListByUploaderId";
        
        try {
            // 閸欏倹鏆熸宀冪槈
            validateId(userId, methodName);
            
            log.debug("閺嶈宓侀悽銊﹀煕ID閺屻儴顕楅弬鍥︽鐠у嫭绨崚妤勩€? userId={}, method={}", userId, methodName);
            
            // 閻㈢喐鍨氱紓鎾崇摠闁?            String cacheKey = USER_FILES_CACHE_PREFIX + userId;
            
            // 濡偓閺屻儳绱︾€?            @SuppressWarnings("unchecked")
            List<ImFileAsset> cachedFiles = (List<ImFileAsset>) redisTemplate.opsForValue().get(cacheKey);
            if (cachedFiles != null && !cachedFiles.isEmpty()) {
                log.debug("娴犲海绱︾€涙骞忛崣鏍暏閹撮攱鏋冩禒鎯扮カ濠ф劕鍨悰? userId={}, count={}, method={}", userId, cachedFiles.size(), methodName);
                return cachedFiles;
            }
            
            // 閺嬪嫬缂撻弻銉嚄閺夆€叉
            ImFileAsset query = new ImFileAsset();
            query.setUploaderId(userId);
            
            // 閺屻儴顕楅弫鐗堝祦鎼?            List<ImFileAsset> fileAssets = selectList(query);
            
            // 缂傛挸鐡ㄧ紒鎾寸亯
            if (fileAssets != null && !fileAssets.isEmpty()) {
                redisTemplate.opsForValue().set(cacheKey, fileAssets, CACHE_TIMEOUT_MINUTES, java.util.concurrent.TimeUnit.MINUTES);
                log.debug("閻劍鍩涢弬鍥︽鐠у嫭绨崚妤勩€冨鑼处鐎? userId={}, count={}, method={}", userId, fileAssets.size(), methodName);
            }
            
            return fileAssets;
            
        } catch (Exception e) {
            log.error("閺嶈宓侀悽銊﹀煕ID閺屻儴顕楅弬鍥︽鐠у嫭绨崚妤勩€冨鍌氱埗: userId={}, error={}, method={}", userId, e.getMessage(), methodName, e);
            throw new BusinessException("閺嶈宓侀悽銊﹀煕ID閺屻儴顕楅弬鍥︽鐠у嫭绨崚妤勩€冩径杈Е", e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("閺嶈宓侀悽銊﹀煕ID閺屻儴顕楅弬鍥︽鐠у嫭绨崚妤勩€冮懓妤佹: {}ms, userId={}, method={}", duration, userId, methodName);
        }
    }
    
    /**
     * 閺嶈宓丮D5缂傛挸鐡ㄩ弬鍥︽鐠у嫭绨?
     * 
     * @param fileAsset 閺傚洣娆㈢挧鍕爱
     */
    private void cacheFileByMd5(ImFileAsset fileAsset) {
        if (fileAsset != null && fileAsset.getMd5() != null) {
            String cacheKey = FILE_MD5_CACHE_PREFIX + fileAsset.getMd5();
            redisTemplate.opsForValue().set(cacheKey, fileAsset, CACHE_TIMEOUT_MINUTES, java.util.concurrent.TimeUnit.MINUTES);
            log.debug("閺傚洣娆㈢挧鍕爱MD5缂傛挸鐡ㄥ鍙夋纯閺? fileId={}, md5={}", fileAsset.getId(), fileAsset.getMd5());
        }
    }
    
    /**
     * 濞撳懘娅庨悽銊﹀煕閺傚洣娆㈤崚妤勩€冪紓鎾崇摠
     * 
     * @param userId 閻劍鍩汭D
     */
    private void clearUserFilesCache(Long userId) {
        if (userId != null) {
            String cacheKey = USER_FILES_CACHE_PREFIX + userId;
            redisTemplate.delete(cacheKey);
            log.debug("閻劍鍩涢弬鍥︽閸掓銆冪紓鎾崇摠瀹稿弶绔婚梽? userId={}", userId);
        }
    }
    
    /**
     * 鐎圭偟骞嘐nhancedBaseServiceImpl閻ㄥ嫭濞婄挒鈩冩煙濞?     * 
     * @return 鐎圭偘缍嬬猾璇茬€烽崥宥囆?
     */
    @Override
    protected String getEntityType() {
        return "fileAsset";
    }
    
    /**
     * 鐎圭偟骞嘐nhancedBaseServiceImpl閻ㄥ嫭濞婄挒鈩冩煙濞?     * 
     * @param entity 閺傚洣娆㈢挧鍕爱鐎圭偘缍?
     * @return 閺傚洣娆㈢挧鍕爱ID
     */
    @Override
    protected Long getEntityId(ImFileAsset entity) {
        return entity != null ? entity.getId() : null;
    }
    
    /**
     * 鐎圭偟骞嘐nhancedBaseServiceImpl閻ㄥ嫭濞婄挒鈩冩煙濞?     * 
     * @param entity 閺傚洣娆㈢挧鍕爱鐎圭偘缍?
     */
    @Override
    protected void setCreateTime(ImFileAsset entity) {
        if (entity != null && entity.getCreateTime() == null) {
            entity.setCreateTime(LocalDateTime.now());
        }
    }
    
    /**
     * 鐎圭偟骞嘐nhancedBaseServiceImpl閻ㄥ嫭濞婄挒鈩冩煙濞?     * 
     * @param entity 閺傚洣娆㈢挧鍕爱鐎圭偘缍?
     */
    @Override
    protected void setUpdateTime(ImFileAsset entity) {
        if (entity != null) {
            // 閺傚洣娆㈢挧鍕爱闁艾鐖舵稉宥夋付鐟曚焦娲块弬鐗堟闂傝揪绱濇担鍡氱箹闁插本褰佹笟娑氱埠娑撯偓閻ㄥ嫭甯撮崣?            // 婵″倹鐏夐棁鈧憰浣规纯閺傚府绱濋崣顖欎簰閸欐牗绉锋稉瀣桨閻ㄥ嫭鏁為柌?            // entity.setUpdateTime(LocalDateTime.now());
        }
    }
    
    /**
     * 鐎圭偟骞嘐nhancedBaseServiceImpl娑擃厾娈慶learRelatedCache閺傝纭堕敍灞惧絹娓氭稒鏋冩禒鎯扮カ濠ф劗澹掔€规氨绱︾€涙ɑ绔婚悶鍡涒偓鏄忕帆
     * 
     * @param entity 閺傚洣娆㈢挧鍕爱鐎圭偘缍?
     */
    @Override
    protected void clearRelatedCache(ImFileAsset entity) {
        if (entity != null) {
            // 濞撳懘娅庣€圭偘缍嬬紓鎾崇摠
            clearEntityCache(entity.getId());
            
            // 濞撳懘娅嶮D5缂傛挸鐡?
            if (entity.getMd5() != null) {
                redisTemplate.delete(FILE_MD5_CACHE_PREFIX + entity.getMd5());
            }
            
            // 濞撳懘娅庨悽銊﹀煕閺傚洣娆㈤崚妤勩€冪紓鎾崇摠
            if (entity.getUploaderId() != null) {
                clearUserFilesCache(entity.getUploaderId());
            }
        }
    }
}
