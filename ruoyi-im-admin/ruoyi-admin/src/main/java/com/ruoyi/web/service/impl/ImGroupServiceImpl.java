package com.ruoyi.web.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.web.service.IImGroupService;
import com.ruoyi.system.domain.ImGroup;

/**
 * 群组Service业务层处理
 * 
 * @author ruoyi
 */
@Service("adminImGroupServiceImpl")
public class ImGroupServiceImpl implements IImGroupService
{
    @Autowired
    private com.ruoyi.system.service.IImGroupService systemGroupService;

    /**
     * 查询群组
     * 
     * @param id 群组ID
     * @return 群组
     */
    @Override
    public ImGroup selectImGroupById(Long id)
    {
        return systemGroupService.selectImGroupById(id);
    }

    /**
     * 查询群组列表
     * 
     * @param imGroup 群组
     * @return 群组
     */
    @Override
    public List<ImGroup> selectImGroupList(ImGroup imGroup)
    {
        return systemGroupService.selectImGroupList(imGroup);
    }

    /**
     * 新增群组
     * 
     * @param imGroup 群组
     * @return 结果
     */
    @Override
    public int insertImGroup(ImGroup imGroup)
    {
        return systemGroupService.insertImGroup(imGroup);
    }

    /**
     * 修改群组
     * 
     * @param imGroup 群组
     * @return 结果
     */
    @Override
    public int updateImGroup(ImGroup imGroup)
    {
        return systemGroupService.updateImGroup(imGroup);
    }

    /**
     * 批量删除群组
     * 
     * @param ids 需要删除的群组ID
     * @return 结果
     */
    @Override
    public int deleteImGroupByIds(Long[] ids)
    {
        return systemGroupService.deleteImGroupByIds(ids);
    }

    /**
     * 删除群组信息
     * 
     * @param id 群组ID
     * @return 结果
     */
    @Override
    public int deleteImGroupById(Long id)
    {
        return systemGroupService.deleteImGroupById(id);
    }
}