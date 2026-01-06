package com.ruoyi.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.im.domain.ImDepartment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 部门Mapper接口
 *
 * 用于操作部门数据
 *
 * @author ruoyi
 */
@Mapper
public interface ImDepartmentMapper extends BaseMapper<ImDepartment> {

    /**
     * 查询所有部门列表
     *
     * @return 部门列表
     */
    List<ImDepartment> selectAllDepartments();

    /**
     * 根据父部门ID查询子部门列表
     *
     * @param parentId 父部门ID
     * @return 子部门列表
     */
    List<ImDepartment> selectChildrenByParentId(@Param("parentId") Long parentId);

    /**
     * 查询部门及其所有子部门ID列表
     *
     * @param departmentId 部门ID
     * @return 部门ID列表
     */
    List<Long> selectDepartmentAndChildrenIds(@Param("departmentId") Long departmentId);

    /**
     * 检查部门是否存在子部门
     *
     * @param departmentId 部门ID
     * @return 子部门数量
     */
    int countChildrenByDepartmentId(@Param("departmentId") Long departmentId);
}
