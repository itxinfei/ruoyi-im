package com.ruoyi.im.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import com.ruoyi.im.mapper.base.BaseMapper;
import com.ruoyi.im.service.BaseService;

/**
 * 基础Service实现类
 * 
 * @author ruoyi
 * @param <T> 实体类型
 * @param <M> Mapper类型
 */
public abstract class BaseServiceImpl<T, M extends BaseMapper<T>> implements BaseService<T> {
    
    @Autowired
    protected M baseMapper;
    
    /**
     * 查询实体
     * 
     * @param id 实体ID
     * @return 实体
     */
    @Override
    public T selectById(Long id) {
        return baseMapper.selectById(id);
    }
    
    /**
     * 查询实体列表
     * 
     * @param entity 实体条件
     * @return 实体列表
     */
    @Override
    public List<T> selectList(T entity) {
        return baseMapper.selectList(entity);
    }
    
    /**
     * 新增实体
     * 
     * @param entity 实体
     * @return 结果
     */
    @Override
    public int insert(T entity) {
        return baseMapper.insert(entity);
    }
    
    /**
     * 修改实体
     * 
     * @param entity 实体
     * @return 结果
     */
    @Override
    public int update(T entity) {
        return baseMapper.update(entity);
    }
    
    /**
     * 批量删除实体
     * 
     * @param ids 需要删除的实体ID
     * @return 结果
     */
    @Override
    public int deleteByIds(Long[] ids) {
        return baseMapper.deleteByIds(ids);
    }
    
    /**
     * 删除实体信息
     * 
     * @param id 实体ID
     * @return 结果
     */
    @Override
    public int deleteById(Long id) {
        return baseMapper.deleteById(id);
    }
}