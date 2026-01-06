package com.ruoyi.im.mapper;

import com.ruoyi.im.domain.ImAuditExportRequest;
import com.ruoyi.im.mapper.base.BaseMapper;
import java.util.List;

/**
 * 瀹¤瀵煎嚭璇锋眰Mapper鎺ュ彛
 * 
 * @author ruoyi
 */
public interface ImAuditExportRequestMapper extends BaseMapper<ImAuditExportRequest> {
    /**
     * 鏌ヨ瀹¤瀵煎嚭璇锋眰
     * 
     * @param id 瀹¤瀵煎嚭璇锋眰ID
     * @return 瀹¤瀵煎嚭璇锋眰
     */
    public ImAuditExportRequest selectImAuditExportRequestById(Long id);

    /**
     * 鏌ヨ瀹¤瀵煎嚭璇锋眰鍒楄〃
     * 
     * @param imAuditExportRequest 瀹¤瀵煎嚭璇锋眰
     * @return 瀹¤瀵煎嚭璇锋眰闆嗗悎
     */
    public List<ImAuditExportRequest> selectImAuditExportRequestList(ImAuditExportRequest imAuditExportRequest);

    /**
     * 鏂板瀹¤瀵煎嚭璇锋眰
     * 
     * @param imAuditExportRequest 瀹¤瀵煎嚭璇锋眰
     * @return 缁撴灉
     */
    public int insertImAuditExportRequest(ImAuditExportRequest imAuditExportRequest);

    /**
     * 淇敼瀹¤瀵煎嚭璇锋眰
     * 
     * @param imAuditExportRequest 瀹¤瀵煎嚭璇锋眰
     * @return 缁撴灉
     */
    public int updateImAuditExportRequest(ImAuditExportRequest imAuditExportRequest);

    /**
     * 鍒犻櫎瀹¤瀵煎嚭璇锋眰
     * 
     * @param id 瀹¤瀵煎嚭璇锋眰ID
     * @return 缁撴灉
     */
    public int deleteImAuditExportRequestById(Long id);

    /**
     * 鎵归噺鍒犻櫎瀹¤瀵煎嚭璇锋眰
     * 
     * @param ids 闇€瑕佸垹闄ょ殑鏁版嵁ID
     * @return 缁撴灉
     */
    public int deleteImAuditExportRequestByIds(Long[] ids);
    
    /**
     * 鏍规嵁鐢ㄦ埛ID鏌ヨ瀹¤瀵煎嚭璇锋眰鍒楄〃
     * 
     * @param userId 鐢ㄦ埛ID
     * @return 瀹¤瀵煎嚭璇锋眰闆嗗悎
     */
    public List<ImAuditExportRequest> selectImAuditExportRequestByUserId(Long userId);
    
    /**
     * 鏍规嵁鐘舵€佹煡璇㈠璁″鍑鸿姹傚垪琛?     * 
     * @param status 鐘舵€?     * @return 瀹¤瀵煎嚭璇锋眰闆嗗悎
     */
    public List<ImAuditExportRequest> selectImAuditExportRequestByStatus(String status);
    
    /**
     * 鏍规嵁鐢ㄦ埛ID鍜岀姸鎬佹煡璇㈠璁″鍑鸿姹傚垪琛?     * 
     * @param userId 鐢ㄦ埛ID
     * @param status 鐘舵€?     * @return 瀹¤瀵煎嚭璇锋眰闆嗗悎
     */
    public List<ImAuditExportRequest> selectImAuditExportRequestByUserIdAndStatus(Long userId, String status);
    
    /**
     * 鏇存柊瀵煎嚭璇锋眰鐘舵€?     * 
     * @param id 瀹¤瀵煎嚭璇锋眰ID
     * @param status 鐘舵€?     * @param filePath 鏂囦欢璺緞
     * @param errorMessage 閿欒淇℃伅
     * @return 缁撴灉
     */
    public int updateImAuditExportRequestStatus(Long id, String status, String filePath, String errorMessage);
    
    /**
     * 鍒犻櫎鎸囧畾鏃堕棿涔嬪墠鐨勫璁″鍑鸿姹?     * 
     * @param beforeTime 鏃堕棿
     * @return 缁撴灉
     */
    public int deleteImAuditExportRequestByBeforeTime(java.time.LocalDateTime beforeTime);
}
