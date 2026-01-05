package com.ruoyi.im.service;

import java.util.List;

/**
 * 基础Service接口，提供通用的CRUD操作
 * 
 * @param <T> 实体类型
 * @author ruoyi
 */
public interface BaseService<T> {
    
    /**
     * 根据ID查询实体
     * 
     * @param id 实体ID
     * @return 实体对象
     */
    T selectById(Long id);
    
    /**
     * 查询实体列表
     * 
     * @param entity 查询条件
     * @return 实体集合
     */
    List<T> selectList(T entity);
    
    /**
     * 新增实体
     * 
     * @param entity 实体对象
     * @return 影响行数
     */
    int insert(T entity);
    
    /**
     * 修改实体
     * 
     * @param entity 实体对象
     * @return 影响行数
     */
    int update(T entity);
    
    /**
     * 批量删除实体
     * 
     * @param ids 需要删除的实体ID数组
     * @return 影响行数
     */
    int deleteByIds(Long[] ids);
    
    /**
     * 根据ID删除实体
     * 
     * @param id 实体ID
     * @return 影响行数
     */
    int deleteById(Long id);
}