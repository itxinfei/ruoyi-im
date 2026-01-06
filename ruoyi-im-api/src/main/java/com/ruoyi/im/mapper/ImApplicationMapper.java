package com.ruoyi.im.mapper;

import com.ruoyi.im.domain.ImApplication;

import java.util.List;

/**
 * 应用Mapper接口
 *
 * @author ruoyi
 */
public interface ImApplicationMapper {

    /**
     * 查询应用
     *
     * @param id 应用ID
     * @return 应用
     */
    ImApplication selectImApplicationById(Long id);

    /**
     * 根据编码查询应用
     *
     * @param code 应用编码
     * @return 应用
     */
    ImApplication selectImApplicationByCode(String code);

    /**
     * 查询应用列表
     *
     * @param imApplication 应用
     * @return 应用集合
     */
    List<ImApplication> selectImApplicationList(ImApplication imApplication);

    /**
     * 查询可见应用列表
     *
     * @return 应用集合
     */
    List<ImApplication> selectVisibleApplications();

    /**
     * 根据分类查询应用列表
     *
     * @param category 分类
     * @return 应用集合
     */
    List<ImApplication> selectApplicationsByCategory(String category);

    /**
     * 新增应用
     *
     * @param imApplication 应用
     * @return 结果
     */
    int insertImApplication(ImApplication imApplication);

    /**
     * 修改应用
     *
     * @param imApplication 应用
     * @return 结果
     */
    int updateImApplication(ImApplication imApplication);

    /**
     * 删除应用
     *
     * @param id 应用ID
     * @return 结果
     */
    int deleteImApplicationById(Long id);

    /**
     * 批量删除应用
     *
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    int deleteImApplicationByIds(Long[] ids);
}
