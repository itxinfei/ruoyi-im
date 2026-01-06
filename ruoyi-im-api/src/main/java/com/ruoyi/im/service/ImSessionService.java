package com.ruoyi.im.service;

import com.ruoyi.im.domain.ImSession;
import com.ruoyi.im.dto.session.ImSessionQueryRequest;
import java.util.List;

/**
 * 浼氳瘽Service鎺ュ彛
 * 
 * @author ruoyi
 */
public interface ImSessionService {
    
    /**
     * 鏍规嵁ID鏌ヨ浼氳瘽
     * 
     * @param id 浼氳瘽ID
     * @return 浼氳瘽
     */
    ImSession selectById(Long id);
    
    /**
     * 鏌ヨ浼氳瘽鍒楄〃
     * 
     * @param request 鏌ヨ鏉′欢
     * @return 浼氳瘽闆嗗悎
     */
    List<ImSession> selectImSessionList(ImSessionQueryRequest request);
    
    /**
     * 鏂板浼氳瘽
     * 
     * @param imSession 浼氳瘽
     * @return 缁撴灉
     */
    int insert(ImSession imSession);
    
    /**
     * 淇敼浼氳瘽
     * 
     * @param imSession 浼氳瘽
     * @return 缁撴灉
     */
    int update(ImSession imSession);
    
    /**
     * 鎵归噺鍒犻櫎浼氳瘽
     * 
     * @param ids 闇€瑕佸垹闄ょ殑浼氳瘽ID
     * @return 缁撴灉
     */
    int deleteByIds(Long[] ids);
    
    /**
     * 鍒犻櫎浼氳瘽淇℃伅
     * 
     * @param id 浼氳瘽ID
     * @return 缁撴灉
     */
    int deleteById(Long id);
}
