package com.ruoyi.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.im.domain.ImGroup;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 群组Mapper接口
 *
 * @author ruoyi
 */
@Mapper
public interface ImGroupMapper extends BaseMapper<ImGroup> {

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

    /**
     * 分页查询群组列表（管理员用）
     *
     * @param page 分页参数
     * @param keyword 搜索关键词
     * @return 群组分页列表
     */
    IPage<ImGroup> selectGroupPage(Page<ImGroup> page, @Param("keyword") String keyword);
}
