package com.ruoyi.im.mapper.base;

import java.util.List;

/**
 * 鍩虹Mapper鎺ュ彛
 * 
 * @author ruoyi
 * @param <T> 瀹炰綋绫诲瀷
 */
public interface BaseMapper<T> {
    
    /**
     * 鏌ヨ瀹炰綋
     * 
     * @param id 瀹炰綋ID
     * @return 瀹炰綋
     */
    T selectById(Long id);
    
    /**
     * 鏌ヨ瀹炰綋鍒楄〃
     * 
     * @param entity 瀹炰綋鏉′欢
     * @return 瀹炰綋鍒楄〃
     */
    List<T> selectList(T entity);
    
    /**
     * 鏂板瀹炰綋
     * 
     * @param entity 瀹炰綋
     * @return 缁撴灉
     */
    int insert(T entity);
    
    /**
     * 淇敼瀹炰綋
     * 
     * @param entity 瀹炰綋
     * @return 缁撴灉
     */
    int update(T entity);
    
    /**
     * 鎵归噺鍒犻櫎瀹炰綋
     * 
     * @param ids 闇€瑕佸垹闄ょ殑瀹炰綋ID
     * @return 缁撴灉
     */
    int deleteByIds(Long[] ids);
    
    /**
     * 鍒犻櫎瀹炰綋淇℃伅
     * 
     * @param id 瀹炰綋ID
     * @return 缁撴灉
     */
    int deleteById(Long id);
}
