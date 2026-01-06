package com.ruoyi.im.service;

import com.ruoyi.im.domain.ImAuditLog;
import java.util.List;

/**
 * 瀹¤鏃ュ織Service鎺ュ彛
 * 
 * @author ruoyi
 */
public interface ImAuditLogService extends BaseService<ImAuditLog> {
    
    @Override
    ImAuditLog selectById(Long id);
    
    @Override
    List<ImAuditLog> selectList(ImAuditLog imAuditLog);
    
    @Override
    int insert(ImAuditLog imAuditLog);
    
    @Override
    int update(ImAuditLog imAuditLog);
    
    @Override
    int deleteByIds(Long[] ids);
    
    @Override
    int deleteById(Long id);
    
    /**
     * 鏍规嵁鐢ㄦ埛ID鏌ヨ瀹¤鏃ュ織鍒楄〃
     * 
     * @param userId 鐢ㄦ埛ID
     * @return 瀹¤鏃ュ織闆嗗悎
     */
    public List<ImAuditLog> selectImAuditLogByUserId(Long userId);
    
    /**
     * 鏍规嵁鎿嶄綔绫诲瀷鏌ヨ瀹¤鏃ュ織鍒楄〃
     * 
     * @param operationType 鎿嶄綔绫诲瀷
     * @return 瀹¤鏃ュ織闆嗗悎
     */
    public List<ImAuditLog> selectImAuditLogByOperationType(String operationType);
    
    /**
     * 鏍规嵁鐩爣绫诲瀷鍜岀洰鏍嘔D鏌ヨ瀹¤鏃ュ織鍒楄〃
     * 
     * @param targetType 鐩爣绫诲瀷
     * @param targetId 鐩爣ID
     * @return 瀹¤鏃ュ織闆嗗悎
     */
    public List<ImAuditLog> selectImAuditLogByTarget(String targetType, Long targetId);
    
    /**
     * 鏍规嵁IP鍦板潃鏌ヨ瀹¤鏃ュ織鍒楄〃
     * 
     * @param ipAddress IP鍦板潃
     * @return 瀹¤鏃ュ織闆嗗悎
     */
    public List<ImAuditLog> selectImAuditLogByIpAddress(String ipAddress);
    
    /**
     * 璁板綍瀹¤鏃ュ織
     * 
     * @param userId 鐢ㄦ埛ID
     * @param operationType 鎿嶄綔绫诲瀷
     * @param targetType 鐩爣绫诲瀷
     * @param targetId 鐩爣ID
     * @param operationResult 鎿嶄綔缁撴灉
     * @param errorMessage 閿欒淇℃伅
     * @param ipAddress IP鍦板潃
     * @param userAgent 鐢ㄦ埛浠ｇ悊
     * @return 缁撴灉
     */
    public int logAudit(Long userId, String operationType, String targetType, Long targetId, String operationResult, String errorMessage, String ipAddress, String userAgent);
    
    /**
     * 鎵归噺鍒犻櫎鎸囧畾鏃堕棿涔嬪墠鐨勫璁℃棩蹇?
     * 
     * @param beforeTime 鏃堕棿
     * @return 缁撴灉
     */
    public int deleteImAuditLogByBeforeTime(java.time.LocalDateTime beforeTime);
}
