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
 * 鐎孤ゎ吀閺冦儱绻擲ervice娑撴艾濮熺仦鍌氼槱閻?- 娴兼ê瀵查悧鍫熸拱
 * 娴兼ê瀵查崘鍛啇閿涙碍鍧婇崝鐘电处鐎涙ɑ婧€閸掕翰鈧椒绨ㄩ崝鈩冨付閸掕翰鈧焦鈧嗗厴閻╂垶甯堕妴渚€鏁婄拠顖氼槱閻? * 
 * @author ruoyi
 */
@Service
public class ImAuditLogServiceImpl extends EnhancedBaseServiceImpl<ImAuditLog, ImAuditLogMapper> implements ImAuditLogService {
    private static final Logger log = LoggerFactory.getLogger(ImAuditLogServiceImpl.class);
    
    @Autowired
    private ImAuditLogMapper imAuditLogMapper;
    
    // 缂傛挸鐡ㄩ柨顔煎缂傗偓
    private static final String USER_AUDIT_LOG_CACHE_PREFIX = "im:audit:log:user:";
    private static final String OPERATION_AUDIT_LOG_CACHE_PREFIX = "im:audit:log:operation:";
    private static final String TARGET_AUDIT_LOG_CACHE_PREFIX = "im:audit:log:target:";
    private static final String IP_AUDIT_LOG_CACHE_PREFIX = "im:audit:log:ip:";
    
    // 缂傛挸鐡ㄧ搾鍛閺冨爼妫块敍鍫濆瀻闁界噦绱?
    private static final int CACHE_TIMEOUT_MINUTES = 60;

    /**
     * 鐎圭偟骞嘐nhancedBaseServiceImpl閻ㄥ嫭濞婄挒鈩冩煙濞?     * 
     * @return 鐎圭偘缍嬬猾璇茬€烽崥宥囆?
     */
    @Override
    protected String getEntityType() {
        return "auditLog";
    }
    
    /**
     * 鐎圭偟骞嘐nhancedBaseServiceImpl閻ㄥ嫭濞婄挒鈩冩煙濞?     * 
     * @param entity 鐎孤ゎ吀閺冦儱绻旂€圭偘缍?
     * @return 鐎孤ゎ吀閺冦儱绻擨D
     */
    @Override
    protected Long getEntityId(ImAuditLog entity) {
        return entity != null ? entity.getId() : null;
    }
    
    /**
     * 鐎圭偟骞嘐nhancedBaseServiceImpl閻ㄥ嫭濞婄挒鈩冩煙濞?     * 
     * @param entity 鐎孤ゎ吀閺冦儱绻旂€圭偘缍?
     */
    @Override
    protected void setCreateTime(ImAuditLog entity) {
        if (entity != null && entity.getCreateTime() == null) {
            entity.setCreateTime(LocalDateTime.now());
        }
    }
    
    /**
     * 鐎圭偟骞嘐nhancedBaseServiceImpl閻ㄥ嫭濞婄挒鈩冩煙濞?     * 
     * @param entity 鐎孤ゎ吀閺冦儱绻旂€圭偘缍?
     */
    @Override
    protected void setUpdateTime(ImAuditLog entity) {
        if (entity != null) {
            entity.setCreateTime(LocalDateTime.now());
        }
    }
    
    /**
     * 鐎圭偟骞嘐nhancedBaseServiceImpl娑擃厾娈慶learRelatedCache閺傝纭堕敍灞惧絹娓氭稑顓哥拋鈩冩）韫囨澹掔€规氨绱︾€涙ɑ绔婚悶鍡涒偓鏄忕帆
     * 
     * @param entity 鐎孤ゎ吀閺冦儱绻旂€圭偘缍?
     */
    @Override
    protected void clearRelatedCache(ImAuditLog entity) {
        if (entity != null) {
            // 濞撳懘娅庣€圭偘缍嬬紓鎾崇摠
            clearEntityCache(entity.getId());
            
            // 濞撳懘娅庨悽銊﹀煕鐎孤ゎ吀閺冦儱绻旂紓鎾崇摠
            if (entity.getUserId() != null) {
                redisTemplate.delete(USER_AUDIT_LOG_CACHE_PREFIX + entity.getUserId());
            }
            
            // 濞撳懘娅庨幙宥勭稊缁鐎风€孤ゎ吀閺冦儱绻旂紓鎾崇摠
            if (entity.getOperationType() != null) {
                redisTemplate.delete(OPERATION_AUDIT_LOG_CACHE_PREFIX + entity.getOperationType());
            }
            
            // 濞撳懘娅庨惄顔界垼鐎孤ゎ吀閺冦儱绻旂紓鎾崇摠
            if (entity.getTargetType() != null && entity.getTargetId() != null) {
                redisTemplate.delete(TARGET_AUDIT_LOG_CACHE_PREFIX + entity.getTargetType() + ":" + entity.getTargetId());
            }
            
            // 濞撳懘娅嶪P閸︽澘娼冪€孤ゎ吀閺冦儱绻旂紓鎾崇摠
            if (entity.getIpAddress() != null) {
                redisTemplate.delete(IP_AUDIT_LOG_CACHE_PREFIX + entity.getIpAddress());
            }
        }
    }
    
    /**
     * 閺屻儴顕楃€孤ゎ吀閺冦儱绻?
     * 
     * @param id 鐎孤ゎ吀閺冦儱绻擨D
     * @return 鐎孤ゎ吀閺冦儱绻?
     */
    @Override
    public ImAuditLog selectImAuditLogById(Long id) {
        // 娴ｈ法鏁ら悥鍓佽selectById閺傝纭堕敍宀冾嚉閺傝纭跺鑼病閸栧懎鎯堢紓鎾崇摠閵嗕線鐛欑拠浣告嫲闁挎瑨顕ゆ径鍕倞
        return selectById(id);
    }

    /**
     * 閺屻儴顕楃€孤ゎ吀閺冦儱绻旈崚妤勩€?
     * 
     * @param imAuditLog 鐎孤ゎ吀閺冦儱绻?
     * @return 鐎孤ゎ吀閺冦儱绻?
     */
    @Override
    public List<ImAuditLog> selectImAuditLogList(ImAuditLog imAuditLog) {
        // 娴ｈ法鏁ら悥鍓佽selectList閺傝纭堕敍宀冾嚉閺傝纭跺鑼病閸栧懎鎯堟宀冪槈閸滃矂鏁婄拠顖氼槱閻?        return selectList(imAuditLog);
    }

    /**
     * 閺傛澘顤冪€孤ゎ吀閺冦儱绻?
     * 
     * @param imAuditLog 鐎孤ゎ吀閺冦儱绻?
     * @return 缂佹挻鐏?
     */
    @Override
    public int insertImAuditLog(ImAuditLog imAuditLog) {
        // 娴ｈ法鏁ら悥鍓佽insert閺傝纭堕敍宀冾嚉閺傝纭跺鑼病閸栧懎鎯堢紓鎾崇摠閵嗕椒绨ㄩ崝鈩冨付閸掕泛鎷伴柨娆掝嚖婢跺嫮鎮?
        return insert(imAuditLog);
    }

    /**
     * 娣囶喗鏁肩€孤ゎ吀閺冦儱绻?
     * 
     * @param imAuditLog 鐎孤ゎ吀閺冦儱绻?
     * @return 缂佹挻鐏?
     */
    @Override
    public int updateImAuditLog(ImAuditLog imAuditLog) {
        // 娴ｈ法鏁ら悥鍓佽update閺傝纭堕敍宀冾嚉閺傝纭跺鑼病閸栧懎鎯堢紓鎾崇摠閵嗕椒绨ㄩ崝鈩冨付閸掕泛鎷伴柨娆掝嚖婢跺嫮鎮?
        return update(imAuditLog);
    }

    /**
     * 閹靛綊鍣洪崚鐘绘珟鐎孤ゎ吀閺冦儱绻?
     * 
     * @param ids 闂団偓鐟曚礁鍨归梽銈囨畱鐎孤ゎ吀閺冦儱绻擨D
     * @return 缂佹挻鐏?
     */
    @Override
    public int deleteImAuditLogByIds(Long[] ids) {
        // 娴ｈ法鏁ら悥鍓佽deleteByIds閺傝纭堕敍宀冾嚉閺傝纭跺鑼病閸栧懎鎯堢紓鎾崇摠閵嗕椒绨ㄩ崝鈩冨付閸掕泛鎷伴柨娆掝嚖婢跺嫮鎮?
        return deleteByIds(ids);
    }

    /**
     * 閸掔娀娅庣€孤ゎ吀閺冦儱绻旀穱鈩冧紖
     * 
     * @param id 鐎孤ゎ吀閺冦儱绻擨D
     * @return 缂佹挻鐏?
     */
    @Override
    public int deleteImAuditLogById(Long id) {
        // 娴ｈ法鏁ら悥鍓佽deleteById閺傝纭堕敍宀冾嚉閺傝纭跺鑼病閸栧懎鎯堢紓鎾崇摠閵嗕椒绨ㄩ崝鈩冨付閸掕泛鎷伴柨娆掝嚖婢跺嫮鎮?
        return deleteById(id);
    }
    
    /**
     * 閺嶈宓侀悽銊﹀煕ID閺屻儴顕楃€孤ゎ吀閺冦儱绻旈崚妤勩€?
     * 
     * @param userId 閻劍鍩汭D
     * @return 鐎孤ゎ吀閺冦儱绻旈梿鍡楁値
     */
    @Override
    public List<ImAuditLog> selectImAuditLogByUserId(Long userId) {
        long startTime = System.currentTimeMillis();
        String methodName = "selectImAuditLogByUserId";
        
        try {
            // 閸欏倹鏆熸宀冪槈
            validateId(userId, methodName);
            
            log.debug("閺嶈宓侀悽銊﹀煕ID閺屻儴顕楃€孤ゎ吀閺冦儱绻? userId={}, method={}", userId, methodName);
            
            // 閻㈢喐鍨氱紓鎾崇摠闁?            String cacheKey = USER_AUDIT_LOG_CACHE_PREFIX + userId;
            
            // 濡偓閺屻儳绱︾€?            @SuppressWarnings("unchecked")
            List<ImAuditLog> cachedLogs = (List<ImAuditLog>) redisTemplate.opsForValue().get(cacheKey);
            if (cachedLogs != null) {
                log.debug("娴犲海绱︾€涙骞忛崣鏍暏閹村嘲顓哥拋鈩冩）韫? userId={}, method={}", userId, methodName);
                return cachedLogs;
            }
            
            // 閺屻儴顕楅弫鐗堝祦鎼?            List<ImAuditLog> logs = imAuditLogMapper.selectImAuditLogByUserId(userId);
            
            // 缂傛挸鐡ㄧ紒鎾寸亯
            if (logs != null && !logs.isEmpty()) {
                redisTemplate.opsForValue().set(cacheKey, logs, CACHE_TIMEOUT_MINUTES, TimeUnit.MINUTES);
                log.debug("閻劍鍩涚€孤ゎ吀閺冦儱绻斿鑼处鐎? userId={}, count={}, method={}", 
                          userId, logs.size(), methodName);
            }
            
            return logs;
            
        } catch (Exception e) {
            log.error("閺嶈宓侀悽銊﹀煕ID閺屻儴顕楃€孤ゎ吀閺冦儱绻斿鍌氱埗: userId={}, error={}, method={}", 
                      userId, e.getMessage(), methodName, e);
            throw new BusinessException("閺嶈宓侀悽銊﹀煕ID閺屻儴顕楃€孤ゎ吀閺冦儱绻旀径杈Е", e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("閺嶈宓侀悽銊﹀煕ID閺屻儴顕楃€孤ゎ吀閺冦儱绻旈懓妤佹: {}ms, userId={}, method={}", 
                     duration, userId, methodName);
        }
    }
    
    /**
     * 閺嶈宓侀幙宥勭稊缁鐎烽弻銉嚄鐎孤ゎ吀閺冦儱绻旈崚妤勩€?
     * 
     * @param operationType 閹垮秳缍旂猾璇茬€?
     * @return 鐎孤ゎ吀閺冦儱绻旈梿鍡楁値
     */
    @Override
    public List<ImAuditLog> selectImAuditLogByOperationType(String operationType) {
        long startTime = System.currentTimeMillis();
        String methodName = "selectImAuditLogByOperationType";
        
        try {
            // 閸欏倹鏆熸宀冪槈
            if (operationType == null || operationType.trim().isEmpty()) {
                throw new BusinessException(methodName + "閸欏倹鏆熼弮鐘虫櫏: 閹垮秳缍旂猾璇茬€锋稉宥堝厴娑撹櫣鈹");
            }
            
            log.debug("閺嶈宓侀幙宥勭稊缁鐎烽弻銉嚄鐎孤ゎ吀閺冦儱绻? operationType={}, method={}", operationType, methodName);
            
            // 閻㈢喐鍨氱紓鎾崇摠闁?            String cacheKey = OPERATION_AUDIT_LOG_CACHE_PREFIX + operationType;
            
            // 濡偓閺屻儳绱︾€?            @SuppressWarnings("unchecked")
            List<ImAuditLog> cachedLogs = (List<ImAuditLog>) redisTemplate.opsForValue().get(cacheKey);
            if (cachedLogs != null) {
                log.debug("娴犲海绱︾€涙骞忛崣鏍ㄦ惙娴ｆ粎琚崹瀣吀鐠佲剝妫╄箛? operationType={}, method={}", operationType, methodName);
                return cachedLogs;
            }
            
            // 閺屻儴顕楅弫鐗堝祦鎼?            List<ImAuditLog> logs = imAuditLogMapper.selectImAuditLogByOperationType(operationType);
            
            // 缂傛挸鐡ㄧ紒鎾寸亯
            if (logs != null && !logs.isEmpty()) {
                redisTemplate.opsForValue().set(cacheKey, logs, CACHE_TIMEOUT_MINUTES, TimeUnit.MINUTES);
                log.debug("閹垮秳缍旂猾璇茬€风€孤ゎ吀閺冦儱绻斿鑼处鐎? operationType={}, count={}, method={}", 
                          operationType, logs.size(), methodName);
            }
            
            return logs;
            
        } catch (Exception e) {
            log.error("閺嶈宓侀幙宥勭稊缁鐎烽弻銉嚄鐎孤ゎ吀閺冦儱绻斿鍌氱埗: operationType={}, error={}, method={}", 
                      operationType, e.getMessage(), methodName, e);
            throw new BusinessException("閺嶈宓侀幙宥勭稊缁鐎烽弻銉嚄鐎孤ゎ吀閺冦儱绻旀径杈Е", e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("閺嶈宓侀幙宥勭稊缁鐎烽弻銉嚄鐎孤ゎ吀閺冦儱绻旈懓妤佹: {}ms, operationType={}, method={}", 
                     duration, operationType, methodName);
        }
    }
    
    /**
     * 閺嶈宓侀惄顔界垼缁鐎烽崪宀€娲伴弽鍢擠閺屻儴顕楃€孤ゎ吀閺冦儱绻旈崚妤勩€?
     * 
     * @param targetType 閻╊喗鐖ｇ猾璇茬€?
     * @param targetId 閻╊喗鐖D
     * @return 鐎孤ゎ吀閺冦儱绻旈梿鍡楁値
     */
    @Override
    public List<ImAuditLog> selectImAuditLogByTarget(String targetType, Long targetId) {
        long startTime = System.currentTimeMillis();
        String methodName = "selectImAuditLogByTarget";
        
        try {
            // 閸欏倹鏆熸宀冪槈
            if (targetType == null || targetType.trim().isEmpty()) {
                throw new BusinessException(methodName + "閸欏倹鏆熼弮鐘虫櫏: 閻╊喗鐖ｇ猾璇茬€锋稉宥堝厴娑撹櫣鈹");
            }
            validateId(targetId, methodName);
            
            log.debug("閺嶈宓侀惄顔界垼閺屻儴顕楃€孤ゎ吀閺冦儱绻? targetType={}, targetId={}, method={}", 
                      targetType, targetId, methodName);
            
            // 閻㈢喐鍨氱紓鎾崇摠闁?            String cacheKey = TARGET_AUDIT_LOG_CACHE_PREFIX + targetType + ":" + targetId;
            
            // 濡偓閺屻儳绱︾€?            @SuppressWarnings("unchecked")
            List<ImAuditLog> cachedLogs = (List<ImAuditLog>) redisTemplate.opsForValue().get(cacheKey);
            if (cachedLogs != null) {
                log.debug("娴犲海绱︾€涙骞忛崣鏍窗閺嶅洤顓哥拋鈩冩）韫? targetType={}, targetId={}, method={}", 
                          targetType, targetId, methodName);
                return cachedLogs;
            }
            
            // 閺屻儴顕楅弫鐗堝祦鎼?            List<ImAuditLog> logs = imAuditLogMapper.selectImAuditLogByTarget(targetType, targetId);
            
            // 缂傛挸鐡ㄧ紒鎾寸亯
            if (logs != null && !logs.isEmpty()) {
                redisTemplate.opsForValue().set(cacheKey, logs, CACHE_TIMEOUT_MINUTES, TimeUnit.MINUTES);
                log.debug("閻╊喗鐖ｇ€孤ゎ吀閺冦儱绻斿鑼处鐎? targetType={}, targetId={}, count={}, method={}", 
                          targetType, targetId, logs.size(), methodName);
            }
            
            return logs;
            
        } catch (Exception e) {
            log.error("閺嶈宓侀惄顔界垼閺屻儴顕楃€孤ゎ吀閺冦儱绻斿鍌氱埗: targetType={}, targetId={}, error={}, method={}", 
                      targetType, targetId, e.getMessage(), methodName, e);
            throw new BusinessException("閺嶈宓侀惄顔界垼閺屻儴顕楃€孤ゎ吀閺冦儱绻旀径杈Е", e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("閺嶈宓侀惄顔界垼閺屻儴顕楃€孤ゎ吀閺冦儱绻旈懓妤佹: {}ms, targetType={}, targetId={}, method={}", 
                     duration, targetType, targetId, methodName);
        }
    }
    
    /**
     * 閺嶈宓両P閸︽澘娼冮弻銉嚄鐎孤ゎ吀閺冦儱绻旈崚妤勩€?
     * 
     * @param ipAddress IP閸︽澘娼?
     * @return 鐎孤ゎ吀閺冦儱绻旈梿鍡楁値
     */
    @Override
    public List<ImAuditLog> selectImAuditLogByIpAddress(String ipAddress) {
        long startTime = System.currentTimeMillis();
        String methodName = "selectImAuditLogByIpAddress";
        
        try {
            // 閸欏倹鏆熸宀冪槈
            if (ipAddress == null || ipAddress.trim().isEmpty()) {
                throw new BusinessException(methodName + "閸欏倹鏆熼弮鐘虫櫏: IP閸︽澘娼冩稉宥堝厴娑撹櫣鈹");
            }
            
            log.debug("閺嶈宓両P閸︽澘娼冮弻銉嚄鐎孤ゎ吀閺冦儱绻? ipAddress={}, method={}", ipAddress, methodName);
            
            // 閻㈢喐鍨氱紓鎾崇摠闁?            String cacheKey = IP_AUDIT_LOG_CACHE_PREFIX + ipAddress;
            
            // 濡偓閺屻儳绱︾€?            @SuppressWarnings("unchecked")
            List<ImAuditLog> cachedLogs = (List<ImAuditLog>) redisTemplate.opsForValue().get(cacheKey);
            if (cachedLogs != null) {
                log.debug("娴犲海绱︾€涙骞忛崣鏈揚閸︽澘娼冪€孤ゎ吀閺冦儱绻? ipAddress={}, method={}", ipAddress, methodName);
                return cachedLogs;
            }
            
            // 閺屻儴顕楅弫鐗堝祦鎼?            List<ImAuditLog> logs = imAuditLogMapper.selectImAuditLogByIpAddress(ipAddress);
            
            // 缂傛挸鐡ㄧ紒鎾寸亯
            if (logs != null && !logs.isEmpty()) {
                redisTemplate.opsForValue().set(cacheKey, logs, CACHE_TIMEOUT_MINUTES, TimeUnit.MINUTES);
                log.debug("IP閸︽澘娼冪€孤ゎ吀閺冦儱绻斿鑼处鐎? ipAddress={}, count={}, method={}", 
                          ipAddress, logs.size(), methodName);
            }
            
            return logs;
            
        } catch (Exception e) {
            log.error("閺嶈宓両P閸︽澘娼冮弻銉嚄鐎孤ゎ吀閺冦儱绻斿鍌氱埗: ipAddress={}, error={}, method={}", 
                      ipAddress, e.getMessage(), methodName, e);
            throw new BusinessException("閺嶈宓両P閸︽澘娼冮弻銉嚄鐎孤ゎ吀閺冦儱绻旀径杈Е", e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("閺嶈宓両P閸︽澘娼冮弻銉嚄鐎孤ゎ吀閺冦儱绻旈懓妤佹: {}ms, ipAddress={}, method={}", 
                     duration, ipAddress, methodName);
        }
    }
    
    /**
     * 鐠佹澘缍嶇€孤ゎ吀閺冦儱绻?
     * 
     * @param userId 閻劍鍩汭D
     * @param operationType 閹垮秳缍旂猾璇茬€?
     * @param targetType 閻╊喗鐖ｇ猾璇茬€?
     * @param targetId 閻╊喗鐖D
     * @param operationResult 閹垮秳缍旂紒鎾寸亯
     * @param errorMessage 闁挎瑨顕ゆ穱鈩冧紖
     * @param ipAddress IP閸︽澘娼?
     * @param userAgent 閻劍鍩涙禒锝囨倞
     * @return 缂佹挻鐏?
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
     * 閹靛綊鍣洪崚鐘绘珟閹稿洤鐣鹃弮鍫曟？娑斿澧犻惃鍕吀鐠佲剝妫╄箛?     * 
     * @param beforeTime 閺冨爼妫?
     * @return 缂佹挻鐏?
     */
    @Override
    public int deleteImAuditLogByBeforeTime(LocalDateTime beforeTime) {
        return imAuditLogMapper.deleteImAuditLogByBeforeTime(beforeTime);
    }
}
