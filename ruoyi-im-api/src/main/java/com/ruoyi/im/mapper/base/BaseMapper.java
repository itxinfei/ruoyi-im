package com.ruoyi.im.mapper.base;

import java.util.List;

/**
 * 基础Mapper接口
 * 
 * @author ruoyi
 * @param <T> 实体类型
 */
public interface BaseMapper<T> {
    
    /**
     * 查询实体
     * 
     * @param id 实体ID
     * @return 实体
     */
    T selectById(Long id);
    
    /**
     * 查询实体列表
     * 
     * @param entity 实体条件
     * @return 实体列表
     */
    List<T> selectList(T entity);
    
    /**
     * 新增实体
     * 
     * @param entity 实体
     * @return 结果
     */
    int insert(T entity);
    
    /**
     * 修改实体
     * 
     * @param entity 实体
     * @return 结果
     */
    int update(T entity);
    
    /**
     * 批量删除实体
     * 
     * @param ids 需要删除的实体ID
     * @return 结果
     */
    int deleteByIds(Long[] ids);
    
    /**
     * 删除实体信息
     * 
     * @param id 实体ID
     * @return 结果
     */
    int deleteById(Long id);
}