package com.ruoyi.im.mapper;

import com.ruoyi.im.domain.ImGroup;

import java.util.List;

/**
 * 群组Mapper接口
 *
 * @author ruoyi
 */
public interface ImGroupMapper {

    /**
     * 查询群组
     *
     * @param id 群组ID
     * @return 群组
     */
    ImGroup selectImGroupById(Long id);

    /**
     * 查询群组列表
     *
     * @param imGroup 群组
     * @return 群组集合
     */
    List<ImGroup> selectImGroupList(ImGroup imGroup);

    /**
     * 新增群组
     *
     * @param imGroup 群组
     * @return 结果
     */
    int insertImGroup(ImGroup imGroup);

    /**
     * 修改群组
     *
     * @param imGroup 群组
     * @return 结果
     */
    int updateImGroup(ImGroup imGroup);

    /**
     * 删除群组
     *
     * @param id 群组ID
     * @return 结果
     */
    int deleteImGroupById(Long id);

    /**
     * 批量删除群组
     *
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    int deleteImGroupByIds(Long[] ids);

    /**
     * 根据群组ID列表删除群组成员
     *
     * @param groupIds 群组ID列表
     * @return 结果
     */
    int deleteImGroupMemberByGroupIds(Long[] groupIds);
}
