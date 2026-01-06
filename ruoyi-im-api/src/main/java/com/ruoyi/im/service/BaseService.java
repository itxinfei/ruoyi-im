package com.ruoyi.im.service;

import java.util.List;

/**
 * 鍩虹Service鎺ュ彛锛屾彁渚涢€氱敤鐨凜RUD鎿嶄綔
 * 
 * @param <T> 瀹炰綋绫诲瀷
 * @author ruoyi
 */
public interface BaseService<T> {
    
    /**
     * 鏍规嵁ID鏌ヨ瀹炰綋
     * 
     * @param id 瀹炰綋ID
     * @return 瀹炰綋瀵硅薄
     */
    T selectById(Long id);
    
    /**
     * 鏌ヨ瀹炰綋鍒楄〃
     * 
     * @param entity 鏌ヨ鏉′欢
     * @return 瀹炰綋闆嗗悎
     */
    List<T> selectList(T entity);
    
    /**
     * 鏂板瀹炰綋
     * 
     * @param entity 瀹炰綋瀵硅薄
     * @return 褰卞搷琛屾暟
     */
    int insert(T entity);
    
    /**
     * 淇敼瀹炰綋
     * 
     * @param entity 瀹炰綋瀵硅薄
     * @return 褰卞搷琛屾暟
     */
    int update(T entity);
    
    /**
     * 鎵归噺鍒犻櫎瀹炰綋
     * 
     * @param ids 闇€瑕佸垹闄ょ殑瀹炰綋ID鏁扮粍
     * @return 褰卞搷琛屾暟
     */
    int deleteByIds(Long[] ids);
    
    /**
     * 鏍规嵁ID鍒犻櫎瀹炰綋
     * 
     * @param id 瀹炰綋ID
     * @return 褰卞搷琛屾暟
     */
    int deleteById(Long id);
}
