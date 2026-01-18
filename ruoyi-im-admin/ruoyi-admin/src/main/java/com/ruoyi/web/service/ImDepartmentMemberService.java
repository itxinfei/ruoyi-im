package com.ruoyi.web.service;

import com.ruoyi.web.domain.ImDepartmentMember;

import java.util.List;
import java.util.Map;

/**
 * 部门成员Service接口
 *
 * @author ruoyi
 */
public interface ImDepartmentMemberService {

    /**
     * 查询部门成员关系
     *
     * @param id 部门成员关系主键
     * @return 部门成员关系
     */
    ImDepartmentMember selectImDepartmentMemberById(Long id);

    /**
     * 查询部门成员关系列表
     *
     * @param imDepartmentMember 部门成员关系
     * @return 部门成员关系集合
     */
    List<ImDepartmentMember> selectImDepartmentMemberList(ImDepartmentMember imDepartmentMember);

    /**
     * 根据部门ID查询成员列表
     *
     * @param departmentId 部门ID
     * @return 成员列表
     */
    List<ImDepartmentMember> selectMembersByDepartmentId(Long departmentId);

    /**
     * 根据用户ID查询用户所属部门列表
     *
     * @param userId 用户ID
     * @return 部门成员关系列表
     */
    List<ImDepartmentMember> selectDepartmentsByUserId(Long userId);

    /**
     * 查询用户的主部门
     *
     * @param userId 用户ID
     * @return 主部门成员关系
     */
    ImDepartmentMember selectPrimaryDepartmentByUserId(Long userId);

    /**
     * 根据部门ID和用户ID查询成员关系
     *
     * @param departmentId 部门ID
     * @param userId 用户ID
     * @return 成员关系
     */
    ImDepartmentMember selectByDepartmentIdAndUserId(Long departmentId, Long userId);

    /**
     * 新增部门成员关系
     *
     * @param imDepartmentMember 部门成员关系
     * @return 结果
     */
    int insertImDepartmentMember(ImDepartmentMember imDepartmentMember);

    /**
     * 修改部门成员关系
     *
     * @param imDepartmentMember 部门成员关系
     * @return 结果
     */
    int updateImDepartmentMember(ImDepartmentMember imDepartmentMember);

    /**
     * 批量删除部门成员关系
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    int deleteImDepartmentMemberByIds(Long[] ids);

    /**
     * 删除部门成员关系信息
     *
     * @param id 部门成员关系主键
     * @return 结果
     */
    int deleteImDepartmentMemberById(Long id);

    /**
     * 根据部门ID删除所有成员
     *
     * @param departmentId 部门ID
     * @return 删除行数
     */
    int deleteMembersByDepartmentId(Long departmentId);

    /**
     * 根据用户ID删除所有部门关系
     *
     * @param userId 用户ID
     * @return 删除行数
     */
    int deleteDepartmentsByUserId(Long userId);

    /**
     * 设置用户的主部门
     *
     * @param userId 用户ID
     * @param departmentId 部门ID
     * @return 结果
     */
    int setPrimaryDepartment(Long userId, Long departmentId);

    /**
     * 统计部门成员数量
     *
     * @param departmentId 部门ID
     * @return 成员数量
     */
    int countMembersByDepartmentId(Long departmentId);

    /**
     * 获取部门成员统计数据
     *
     * @return 统计数据
     */
    Map<String, Object> getDepartmentMemberStatistics();

    /**
     * 检查用户是否已在部门中
     *
     * @param departmentId 部门ID
     * @param userId 用户ID
     * @return 数量
     */
    int checkUserExistsInDepartment(Long departmentId, Long userId);
}
