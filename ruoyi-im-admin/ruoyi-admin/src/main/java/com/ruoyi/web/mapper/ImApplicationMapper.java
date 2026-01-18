package com.ruoyi.web.mapper;

import com.ruoyi.web.domain.ImApplication;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

/**
 * IM应用数据访问层接口（Admin模块专用）
 *
 * <p>负责处理IM应用管理相关的数据库操作</p>
 * <p>主要功能包括：应用的增删改查、分类查询、可见性设置、统计信息等</p>
 *
 * @author ruoyi
 * @date 2025-01-07
 */
@Mapper
public interface ImApplicationMapper {

    /**
     * 查询应用列表
     *
     * <p>根据条件查询IM应用列表，支持按应用名称、分类、可见性等条件筛选</p>
     *
     * @param imApplication 应用查询条件，包含name、category、isVisible等字段
     * @return 应用列表，如果没有符合条件的应用则返回空列表
     */
    List<ImApplication> selectImApplicationList(ImApplication imApplication);

    /**
     * 根据ID获取应用信息
     *
     * <p>通过应用ID查询应用的详细信息，包括应用的基本属性、配置、权限等</p>
     *
     * @param id 应用ID，不能为空
     * @return 应用对象，如果应用不存在则返回null
     */
    ImApplication selectImApplicationById(@Param("id") Long id);

    /**
     * 新增应用
     *
     * <p>向数据库中插入新的应用记录，应用名称、编码等信息需要唯一</p>
     *
     * @param imApplication 应用对象，包含应用的基本信息
     * @return 影响行数，1表示成功，0表示失败
     */
    int insertImApplication(ImApplication imApplication);

    /**
     * 修改应用
     *
     * <p>更新应用的基本信息、配置、权限等，应用ID不可修改</p>
     *
     * @param imApplication 应用对象，必须包含应用ID
     * @return 影响行数，1表示成功，0表示失败
     */
    int updateImApplication(ImApplication imApplication);

    /**
     * 删除应用
     *
     * <p>批量删除指定的应用记录，删除操作为物理删除</p>
     *
     * @param ids 应用ID数组，不能为空
     * @return 影响行数，表示成功删除的应用数量
     */
    int deleteImApplicationByIds(@Param("ids") Long[] ids);

    /**
     * 根据分类获取应用列表
     *
     * <p>查询指定分类下的所有应用，通常用于应用中心展示</p>
     *
     * @param category 应用分类（OFFICE办公、DATA数据、TOOLS工具、CUSTOM自定义）
     * @return 应用列表，如果该分类下没有应用则返回空列表
     */
    List<ImApplication> getApplications(@Param("category") String category);

    /**
     * 获取所有应用并按分类分组
     *
     * <p>查询所有应用，并按照category字段进行分组返回</p>
     * <p>用于应用中心按分类展示应用列表</p>
     *
     * @return 应用列表（未分组，调用方需要手动按category分组）
     */
    List<ImApplication> getApplicationsByCategory();

    /**
     * 设置应用可见性
     *
     * <p>更新单个应用的可见性状态，控制应用是否对用户展示</p>
     *
     * @param id 应用ID，不能为空
     * @param isVisible 可见性标志：true-可见，false-隐藏
     */
    void setVisibility(@Param("id") Long id, @Param("isVisible") Boolean isVisible);

    /**
     * 批量设置应用可见性
     *
     * <p>批量更新多个应用的可见性状态，通常用于批量上下架应用</p>
     *
     * @param ids 应用ID数组，不能为空
     * @param isVisible 可见性标志：true-可见，false-隐藏
     */
    void batchSetVisibility(@Param("ids") Long[] ids, @Param("isVisible") Boolean isVisible);

    /**
     * 获取应用统计信息
     *
     * <p>统计应用的各项数据，包括总数、各分类数量、可见性分布等</p>
     * <p>返回的Map包含以下统计项：</p>
     * <ul>
     *   <li>total_count: 应用总数</li>
     *   <li>visible_count: 可见应用数量</li>
     *   <li>hidden_count: 隐藏应用数量</li>
     *   <li>category_counts: 各分类应用数量</li>
     * </ul>
     *
     * @return 统计信息Map，key为统计项名称，value为统计值
     */
    Map<String, Object> getApplicationStatistics();
}
