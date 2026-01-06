package com.ruoyi.im.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import com.ruoyi.im.mapper.base.BaseMapper;
import com.ruoyi.im.service.BaseService;

/**
 * 鍩虹Service瀹炵幇绫?
 * 
 * @author ruoyi
 * @param <T> 瀹炰綋绫诲瀷
 * @param <M> Mapper绫诲瀷
 */
public abstract class BaseServiceImpl<T, M extends BaseMapper<T>> implements BaseService<T> {
    
    @Autowired
    protected M baseMapper;
    
    /**
     * 鏌ヨ瀹炰綋
     * 
     * @param id 瀹炰綋ID
     * @return 瀹炰綋
     */
    @Override
    public T selectById(Long id) {
        return baseMapper.selectById(id);
    }
    
    /**
     * 鏌ヨ瀹炰綋鍒楄〃
     * 
     * @param entity 瀹炰綋鏉′欢
     * @return 瀹炰綋鍒楄〃
     */
    @Override
    public List<T> selectList(T entity) {
        return baseMapper.selectList(entity);
    }
    
    /**
     * 鏂板瀹炰綋
     * 
     * @param entity 瀹炰綋
     * @return 缁撴灉
     */
    @Override
    public int insert(T entity) {
        return baseMapper.insert(entity);
    }
    
    /**
     * 淇敼瀹炰綋
     * 
     * @param entity 瀹炰綋
     * @return 缁撴灉
     */
    @Override
    public int update(T entity) {
        return baseMapper.update(entity);
    }
    
    /**
     * 鎵归噺鍒犻櫎瀹炰綋
     * 
     * @param ids 闇€瑕佸垹闄ょ殑瀹炰綋ID
     * @return 缁撴灉
     */
    @Override
    public int deleteByIds(Long[] ids) {
        return baseMapper.deleteByIds(ids);
    }
    
    /**
     * 鍒犻櫎瀹炰綋淇℃伅
     * 
     * @param id 瀹炰綋ID
     * @return 缁撴灉
     */
    @Override
    public int deleteById(Long id) {
        return baseMapper.deleteById(id);
    }
}
