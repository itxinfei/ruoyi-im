package com.ruoyi.system.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.system.domain.ImGroup;
import com.ruoyi.system.mapper.ImGroupMapper;
import com.ruoyi.system.service.IImGroupService;

/**
 * IM群组Service业务层处理
 * 
 * @author ruoyi
 * @date 2024-01-01
 */
@Service
public class ImGroupServiceImpl implements IImGroupService
{
    @Autowired
    private ImGroupMapper imGroupMapper;

    /**
     * 查询IM群组
     * 
     * @param id IM群组ID
     * @return IM群组
     */
    @Override
    public ImGroup selectImGroupById(Long id)
    {
        return imGroupMapper.selectImGroupById(id);
    }

    /**
     * 查询IM群组列表
     * 
     * @param imGroup IM群组
     * @return IM群组
     */
    @Override
    public List<ImGroup> selectImGroupList(ImGroup imGroup)
    {
        return imGroupMapper.selectImGroupList(imGroup);
    }

    /**
     * 新增IM群组
     * 
     * @param imGroup IM群组
     * @return 结果
     */
    @Override
    public int insertImGroup(ImGroup imGroup)
    {
        return imGroupMapper.insertImGroup(imGroup);
    }

    /**
     * 修改IM群组
     * 
     * @param imGroup IM群组
     * @return 结果
     */
    @Override
    public int updateImGroup(ImGroup imGroup)
    {
        return imGroupMapper.updateImGroup(imGroup);
    }

    /**
     * 批量删除IM群组
     * 
     * @param ids 需要删除的IM群组ID
     * @return 结果
     */
    @Override
    public int deleteImGroupByIds(Long[] ids)
    {
        return imGroupMapper.deleteImGroupByIds(ids);
    }

    /**
     * 删除IM群组信息
     * 
     * @param id IM群组ID
     * @return 结果
     */
    @Override
    public int deleteImGroupById(Long id)
    {
        return imGroupMapper.deleteImGroupById(id);
    }
}