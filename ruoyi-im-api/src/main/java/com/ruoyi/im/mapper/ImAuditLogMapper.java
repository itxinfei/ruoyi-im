package com.ruoyi.im.mapper;

import com.ruoyi.im.domain.ImAuditLog;
import com.ruoyi.im.mapper.base.BaseMapper;
import java.util.List;

/**
 * 瀹¤鏃ュ織Mapper鎺ュ彛
 * 
 * @author ruoyi
 */
public interface ImAuditLogMapper extends BaseMapper<ImAuditLog> {
    /**
     * 鏌ヨ瀹¤鏃ュ織
     * 
     * @param id 瀹¤鏃ュ織ID
     * @return 瀹¤鏃ュ織
     */
    public ImAuditLog selectImAuditLogById(Long id);

    /**
     * 鏌ヨ瀹¤鏃ュ織鍒楄〃
     * 
     * @param imAuditLog 瀹¤鏃ュ織
     * @return 瀹¤鏃ュ織闆嗗悎
     */
    public List<ImAuditLog> selectImAuditLogList(ImAuditLog imAuditLog);

    /**
     * 鏂板瀹¤鏃ュ織
     * 
     * @param imAuditLog 瀹¤鏃ュ織
     * @return 缁撴灉
     */
    public int insertImAuditLog(ImAuditLog imAuditLog);

    /**
     * 淇敼瀹¤鏃ュ織
     * 
     * @param imAuditLog 瀹¤鏃ュ織
     * @return 缁撴灉
     */
    public int updateImAuditLog(ImAuditLog imAuditLog);

    /**
     * 鍒犻櫎瀹¤鏃ュ織
     * 
     * @param id 瀹¤鏃ュ織ID
     * @return 缁撴灉
     */
    public int deleteImAuditLogById(Long id);

    /**
     * 鎵归噺鍒犻櫎瀹¤鏃ュ織
     * 
     * @param ids 闇€瑕佸垹闄ょ殑鏁版嵁ID
     * @return 缁撴灉
     */
    public int deleteImAuditLogByIds(Long[] ids);
    
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
     * 鎵归噺鍒犻櫎鎸囧畾鏃堕棿涔嬪墠鐨勫璁℃棩蹇?     * 
     * @param beforeTime 鏃堕棿
     * @return 缁撴灉
     */
    public int deleteImAuditLogByBeforeTime(java.time.LocalDateTime beforeTime);
}
