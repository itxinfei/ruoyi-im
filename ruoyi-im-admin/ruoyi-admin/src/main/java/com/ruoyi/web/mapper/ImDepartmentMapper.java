package com.ruoyi.web.mapper;

import com.ruoyi.web.domain.ImDepartment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

/**
 * IM部门数据访问层接口（Admin模块专用）
 *
 * <p>负责处理IM部门管理相关的数据库操作</p>
 * <p>主要功能包括：部门的增删改查、部门树查询、子部门查询、部门状态管理等</p>
 *
 * @author ruoyi
 * @date 2025-01-07
 */
@Mapper
public interface ImDepartmentMapper {

    /**
     * 查询部门
     *
     * @param id 部门主键
     * @return 部门
     */
    ImDepartment selectImDepartmentById(Long id);

    /**
     * 查询部门列表
     *
     * @param imDepartment 部门
     * @return 部门集合
     */
    List<ImDepartment> selectImDepartmentList(ImDepartment imDepartment);

    /**
     * 查询部门树列表
     *
     * @param imDepartment 部门
     * @return 部门树集合
     */
    List<ImDepartment> selectImDepartmentTree(ImDepartment imDepartment);

    /**
     * 查询子部门列表
     *
     * @param parentId 父部门ID
     * @return 子部门列表
     */
    List<ImDepartment> selectChildrenByParentId(Long parentId);

    /**
     * 新增部门
     *
     * @param imDepartment 部门
     * @return 结果
     */
    int insertImDepartment(ImDepartment imDepartment);

    /**
     * 修改部门
     *
     * @param imDepartment 部门
     * @return 结果
     */
    int updateImDepartment(ImDepartment imDepartment);

    /**
     * 删除部门
     *
     * @param id 部门主键
     * @return 结果
     */
    int deleteImDepartmentById(Long id);

    /**
     * 批量删除部门
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    int deleteImDepartmentByIds(Long[] ids);

    /**
     * 检查部门是否存在子部门
     *
     * @param id 部门ID
     * @return 数量
     */
    int checkHasChildren(Long id);

    /**
     * 更新部门状态
     *
     * @param id 部门ID
     * @param status 状态
     * @return 结果
     */
    int updateDepartmentStatus(@Param("id") Long id, @Param("status") Integer status);

    /**
     * 获取部门统计数据
     *
     * @return 统计数据
     */
    Map<String, Object> getDepartmentStatistics();
}
