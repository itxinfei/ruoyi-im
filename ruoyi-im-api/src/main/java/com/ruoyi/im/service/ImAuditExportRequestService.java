package com.ruoyi.im.service;

import com.ruoyi.im.domain.ImAuditExportRequest;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 瀹¤瀵煎嚭璇锋眰Service鎺ュ彛
 * 
 * @author ruoyi
 */
public interface ImAuditExportRequestService {
    
    /**
     * 鏍规嵁ID鏌ヨ瀹¤瀵煎嚭璇锋眰
     * 
     * @param id 瀹¤瀵煎嚭璇锋眰ID
     * @return 瀹¤瀵煎嚭璇锋眰
     */
    ImAuditExportRequest selectById(Long id);
    
    /**
     * 鏍规嵁ID鏌ヨ瀹¤瀵煎嚭璇锋眰锛堝吋瀹规棫鏂规硶鍚嶏級
     * 
     * @param id 瀹¤瀵煎嚭璇锋眰ID
     * @return 瀹¤瀵煎嚭璇锋眰
     */
    default ImAuditExportRequest selectImAuditExportRequestById(Long id) {
        return selectById(id);
    }
    
    /**
     * 鏍规嵁鐢ㄦ埛ID鏌ヨ瀹¤瀵煎嚭璇锋眰鍒楄〃
     * 
     * @param userId 鐢ㄦ埛ID
     * @return 瀹¤瀵煎嚭璇锋眰鍒楄〃
     */
    default List<ImAuditExportRequest> selectImAuditExportRequestByUserId(Long userId) {
        // 鍒涘缓鏌ヨ鏉′欢
        ImAuditExportRequest query = new ImAuditExportRequest();
        query.setUserId(userId);
        return selectImAuditExportRequestList(query);
    }
    
    /**
     * 鏍规嵁鐘舵€佹煡璇㈠璁″鍑鸿姹傚垪琛?     * 
     * @param status 鐘舵€?     * @return 瀹¤瀵煎嚭璇锋眰鍒楄〃
     */
    default List<ImAuditExportRequest> selectImAuditExportRequestByStatus(String status) {
        // 鍒涘缓鏌ヨ鏉′欢
        ImAuditExportRequest query = new ImAuditExportRequest();
        query.setExportStatus(status);
        return selectImAuditExportRequestList(query);
    }
    
    /**
     * 鍒涘缓瀵煎嚭璇锋眰
     * 
     * @param userId 鐢ㄦ埛ID
     * @param startTime 寮€濮嬫椂闂?     * @param endTime 缁撴潫鏃堕棿
     * @param operationType 鎿嶄綔绫诲瀷
     * @param targetType 鐩爣绫诲瀷
     * @param targetId 鐩爣ID
     * @param format 鏍煎紡
     * @return 缁撴灉
     */
    default int createExportRequest(Long userId, LocalDateTime startTime, LocalDateTime endTime, 
                                   String operationType, String targetType, Long targetId, String format) {
        // 鍒涘缓瀹¤瀵煎嚭璇锋眰瀵硅薄
        ImAuditExportRequest request = new ImAuditExportRequest();
        request.setUserId(userId);
        request.setExportType(operationType); // 瀵煎嚭绫诲瀷璁剧疆涓烘搷浣滅被鍨?        request.setExportStatus("pending"); // 鍒濆鐘舵€佷负寰呭鐞?        request.setCreateTime(LocalDateTime.now());
        request.setUpdateTime(LocalDateTime.now());
        
        // 璁剧疆瀵煎嚭鍙傛暟
        StringBuilder params = new StringBuilder();
        params.append("startTime:").append(startTime)
              .append(",endTime:").append(endTime)
              .append(",targetType:").append(targetType)
              .append(",targetId:").append(targetId)
              .append(",format:").append(format);
        request.setExportParams(params.toString());
        
        return insertImAuditExportRequest(request);
    }
    
    /**
     * 鏇存柊瀵煎嚭璇锋眰鐘舵€?     * 
     * @param id 璇锋眰ID
     * @param status 鐘舵€?     * @param url URL
     * @param errorMsg 閿欒淇℃伅
     * @return 缁撴灉
     */
    default int updateExportStatus(Long id, String status, String url, String errorMsg) {
        ImAuditExportRequest request = selectById(id);
        if (request != null) {
            request.setExportStatus(status);
            request.setExportUrl(url);
            request.setUpdateTime(LocalDateTime.now());
            
            // 娣诲姞閿欒淇℃伅鍒板鍑哄弬鏁颁腑
            if (errorMsg != null) {
                String params = request.getExportParams();
                if (params != null) {
                    params += ",errorMsg:" + errorMsg;
                } else {
                    params = "errorMsg:" + errorMsg;
                }
                request.setExportParams(params);
            }
            
            return updateImAuditExportRequest(request);
        }
        return 0;
    }
    
    /**
     * 鏌ヨ瀹¤瀵煎嚭璇锋眰鍒楄〃
     * 
     * @param imAuditExportRequest 鏌ヨ鏉′欢
     * @return 瀹¤瀵煎嚭璇锋眰闆嗗悎
     */
    List<ImAuditExportRequest> selectImAuditExportRequestList(ImAuditExportRequest imAuditExportRequest);
    
    /**
     * 鏂板瀹¤瀵煎嚭璇锋眰
     * 
     * @param imAuditExportRequest 瀹¤瀵煎嚭璇锋眰
     * @return 缁撴灉
     */
    int insertImAuditExportRequest(ImAuditExportRequest imAuditExportRequest);
    
    /**
     * 鏂板瀹¤瀵煎嚭璇锋眰锛堝吋瀹规柟娉曪級
     * 
     * @param imAuditExportRequest 瀹¤瀵煎嚭璇锋眰
     * @return 缁撴灉
     */
    default int insert(ImAuditExportRequest imAuditExportRequest) {
        return insertImAuditExportRequest(imAuditExportRequest);
    }
    
    /**
     * 淇敼瀹¤瀵煎嚭璇锋眰
     * 
     * @param imAuditExportRequest 瀹¤瀵煎嚭璇锋眰
     * @return 缁撴灉
     */
    int updateImAuditExportRequest(ImAuditExportRequest imAuditExportRequest);
    
    /**
     * 淇敼瀹¤瀵煎嚭璇锋眰锛堝吋瀹规柟娉曪級
     * 
     * @param imAuditExportRequest 瀹¤瀵煎嚭璇锋眰
     * @return 缁撴灉
     */
    default int update(ImAuditExportRequest imAuditExportRequest) {
        return updateImAuditExportRequest(imAuditExportRequest);
    }
    
    /**
     * 鎵归噺鍒犻櫎瀹¤瀵煎嚭璇锋眰
     * 
     * @param ids 闇€瑕佸垹闄ょ殑瀹¤瀵煎嚭璇锋眰ID
     * @return 缁撴灉
     */
    int deleteImAuditExportRequestByIds(Long[] ids);
    
    /**
     * 鍒犻櫎瀹¤瀵煎嚭璇锋眰淇℃伅
     * 
     * @param id 瀹¤瀵煎嚭璇锋眰ID
     * @return 缁撴灉
     */
    int deleteImAuditExportRequestById(Long id);
    
    /**
     * 鍒犻櫎瀹¤瀵煎嚭璇锋眰淇℃伅锛堝吋瀹规柟娉曪級
     * 
     * @param id 瀹¤瀵煎嚭璇锋眰ID
     * @return 缁撴灉
     */
    default int deleteById(Long id) {
        return deleteImAuditExportRequestById(id);
    }
    
    /**
     * 鎵归噺鍒犻櫎瀹¤瀵煎嚭璇锋眰锛堝吋瀹规柟娉曪級
     * 
     * @param ids 闇€瑕佸垹闄ょ殑瀹¤瀵煎嚭璇锋眰ID
     * @return 缁撴灉
     */
    default int deleteByIds(Long[] ids) {
        return deleteImAuditExportRequestByIds(ids);
    }
}
