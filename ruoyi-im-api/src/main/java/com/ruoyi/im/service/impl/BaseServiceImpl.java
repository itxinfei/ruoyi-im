package com.ruoyi.im.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import com.ruoyi.im.mapper.base.BaseMapper;
import com.ruoyi.im.service.BaseService;

/**
 * 閸╄櫣顢匰ervice鐎圭偟骞囩猾?
 * 
 * @author ruoyi
 * @param <T> 鐎圭偘缍嬬猾璇茬€?
 * @param <M> Mapper缁鐎?
 */
public abstract class BaseServiceImpl<T, M extends BaseMapper<T>> implements BaseService<T> {
    
    @Autowired
    protected M baseMapper;
    
    /**
     * 閺屻儴顕楃€圭偘缍?
     * 
     * @param id 鐎圭偘缍婭D
     * @return 鐎圭偘缍?
     */
    @Override
    public T selectById(Long id) {
        return baseMapper.selectById(id);
    }
    
    /**
     * 閺屻儴顕楃€圭偘缍嬮崚妤勩€?
     * 
     * @param entity 鐎圭偘缍嬮弶鈥叉
     * @return 鐎圭偘缍嬮崚妤勩€?
     */
    @Override
    public List<T> selectList(T entity) {
        return baseMapper.selectList(entity);
    }
    
    /**
     * 閺傛澘顤冪€圭偘缍?
     * 
     * @param entity 鐎圭偘缍?
     * @return 缂佹挻鐏?
     */
    @Override
    public int insert(T entity) {
        return baseMapper.insert(entity);
    }
    
    /**
     * 娣囶喗鏁肩€圭偘缍?
     * 
     * @param entity 鐎圭偘缍?
     * @return 缂佹挻鐏?
     */
    @Override
    public int update(T entity) {
        return baseMapper.update(entity);
    }
    
    /**
     * 閹靛綊鍣洪崚鐘绘珟鐎圭偘缍?
     * 
     * @param ids 闂団偓鐟曚礁鍨归梽銈囨畱鐎圭偘缍婭D
     * @return 缂佹挻鐏?
     */
    @Override
    public int deleteByIds(Long[] ids) {
        return baseMapper.deleteByIds(ids);
    }
    
    /**
     * 閸掔娀娅庣€圭偘缍嬫穱鈩冧紖
     * 
     * @param id 鐎圭偘缍婭D
     * @return 缂佹挻鐏?
     */
    @Override
    public int deleteById(Long id) {
        return baseMapper.deleteById(id);
    }
}
