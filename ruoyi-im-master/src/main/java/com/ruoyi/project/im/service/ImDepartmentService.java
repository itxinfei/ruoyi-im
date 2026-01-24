package com.ruoyi.web.service;

import com.ruoyi.web.domain.ImDepartment;
import java.util.List;
import java.util.Map;

/**
 * 部门Service接口
 *
 * @author ruoyi
 */
public interface ImDepartmentService {

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
     * 批量删除部门
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    int deleteImDepartmentByIds(Long[] ids);

    /**
     * 删除部门信息
     *
     * @param id 部门主键
     * @return 结果
     */
    int deleteImDepartmentById(Long id);

    /**
     * 更新部门状态
     *
     * @param id 部门ID
     * @param status 状态
     * @return 结果
     */
    int updateDepartmentStatus(Long id, Integer status);

    /**
     * 检查部门是否存在子部门
     *
     * @param id 部门ID
     * @return 数量
     */
    int checkHasChildren(Long id);

    /**
     * 获取部门统计数据
     *
     * @return 统计数据
     */
    Map<String, Object> getDepartmentStatistics();
}
