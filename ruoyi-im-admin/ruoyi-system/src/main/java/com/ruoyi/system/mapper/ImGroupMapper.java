package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.ImGroup;

/**
 * IM群组Mapper接口
 * 
 * @author ruoyi
 * @date 2024-01-01
 */
public interface ImGroupMapper 
{
    /**
     * 查询IM群组
     * 
     * @param id IM群组ID
     * @return IM群组
     */
    public ImGroup selectImGroupById(Long id);

    /**
     * 查询IM群组列表
     * 
     * @param imGroup IM群组
     * @return IM群组集合
     */
    public List<ImGroup> selectImGroupList(ImGroup imGroup);

    /**
     * 新增IM群组
     * 
     * @param imGroup IM群组
     * @return 结果
     */
    public int insertImGroup(ImGroup imGroup);

    /**
     * 修改IM群组
     * 
     * @param imGroup IM群组
     * @return 结果
     */
    public int updateImGroup(ImGroup imGroup);

    /**
     * 删除IM群组
     * 
     * @param id IM群组ID
     * @return 结果
     */
    public int deleteImGroupById(Long id);

    /**
     * 批量删除IM群组
     * 
     * @param ids 需要删除的IM群组ID
     * @return 结果
     */
    public int deleteImGroupByIds(Long[] ids);
}