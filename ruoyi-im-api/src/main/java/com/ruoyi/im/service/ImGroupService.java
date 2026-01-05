package com.ruoyi.im.service;

import com.ruoyi.im.domain.ImGroup;
import java.util.List;

/**
 * 群组Service接口
 * 
 * @author ruoyi
 */
public interface ImGroupService extends BaseService<ImGroup> {
    
    @Override
    ImGroup selectById(Long id);
    
    @Override
    List<ImGroup> selectList(ImGroup imGroup);
    
    @Override
    int insert(ImGroup imGroup);
    
    @Override
    int update(ImGroup imGroup);
    
    @Override
    int deleteByIds(Long[] ids);
    
    @Override
    int deleteById(Long id);
    
    /**
     * 根据用户ID查询群组列表
     * 
     * @param userId 用户ID
     * @return 群组集合
     */
    public List<ImGroup> selectImGroupListByUserId(Long userId);
    
    /**
     * 根据群组ID查询群组信息
     * 
     * @param id 群组ID
     * @return 群组信息
     */
    public ImGroup selectImGroupById(Long id);
    
    /**
     * 更新群组信息
     * 
     * @param imGroup 群组信息
     * @return 结果
     */
    public int updateImGroup(ImGroup imGroup);
    
    /**
     * 根据ID删除群组信息
     * 
     * @param id 群组ID
     * @return 结果
     */
    public int deleteImGroupById(Long id);
}