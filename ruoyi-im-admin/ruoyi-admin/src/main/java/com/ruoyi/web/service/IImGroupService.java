package com.ruoyi.web.service;

import com.ruoyi.system.domain.ImGroup;
import java.util.List;

/**
 * 群组Service接口
 * 
 * @author ruoyi
 */
public interface IImGroupService 
{
    /**
     * 查询群组
     * 
     * @param id 群组ID
     * @return 群组
     */
    public ImGroup selectImGroupById(Long id);

    /**
     * 查询群组列表
     * 
     * @param imGroup 群组
     * @return 群组集合
     */
    public List<ImGroup> selectImGroupList(ImGroup imGroup);

    /**
     * 新增群组
     * 
     * @param imGroup 群组
     * @return 结果
     */
    public int insertImGroup(ImGroup imGroup);

    /**
     * 修改群组
     * 
     * @param imGroup 群组
     * @return 结果
     */
    public int updateImGroup(ImGroup imGroup);

    /**
     * 批量删除群组
     * 
     * @param ids 需要删除的群组ID
     * @return 结果
     */
    public int deleteImGroupByIds(Long[] ids);

    /**
     * 删除群组信息
     * 
     * @param id 群组ID
     * @return 结果
     */
    public int deleteImGroupById(Long id);
}