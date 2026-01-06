package com.ruoyi.im.service;

import com.ruoyi.im.dto.organization.ImDepartmentCreateRequest;
import com.ruoyi.im.dto.organization.ImDepartmentMemberAddRequest;
import com.ruoyi.im.dto.organization.ImDepartmentUpdateRequest;
import com.ruoyi.im.domain.ImDepartment;
import com.ruoyi.im.vo.organization.ImDepartmentMemberVO;
import com.ruoyi.im.vo.organization.ImDepartmentTreeVO;

import java.util.List;

/**
 * 组织架构服务接口
 *
 * 用于管理部门组织架构和成员
 *
 * @author ruoyi
 */
public interface ImOrganizationService {

    /**
     * 获取部门树形结构
     *
     * @return 部门树
     */
    List<ImDepartmentTreeVO> getDepartmentTree();

    /**
     * 根据ID获取部门信息
     *
     * @param departmentId 部门ID
     * @return 部门信息
     */
    ImDepartment getDepartmentById(Long departmentId);

    /**
     * 创建部门
     *
     * @param request 创建请求
     * @return 部门ID
     */
    Long createDepartment(ImDepartmentCreateRequest request);

    /**
     * 更新部门信息
     *
     * @param request 更新请求
     */
    void updateDepartment(ImDepartmentUpdateRequest request);

    /**
     * 删除部门
     *
     * @param departmentId 部门ID
     */
    void deleteDepartment(Long departmentId);

    /**
     * 获取部门成员列表
     *
     * @param departmentId 部门ID
     * @return 成员列表
     */
    List<ImDepartmentMemberVO> getDepartmentMembers(Long departmentId);

    /**
     * 添加部门成员
     *
     * @param request 添加请求
     */
    void addDepartmentMember(ImDepartmentMemberAddRequest request);

    /**
     * 移除部门成员
     *
     * @param departmentId 部门ID
     * @param userId 用户ID
     */
    void removeDepartmentMember(Long departmentId, Long userId);

    /**
     * 设置用户主部门
     *
     * @param userId 用户ID
     * @param departmentId 部门ID
     */
    void setPrimaryDepartment(Long userId, Long departmentId);

    /**
     * 获取用户所属部门列表
     *
     * @param userId 用户ID
     * @return 部门列表
     */
    List<ImDepartment> getUserDepartments(Long userId);

    /**
     * 获取用户主部门
     *
     * @param userId 用户ID
     * @return 主部门
     */
    ImDepartment getUserPrimaryDepartment(Long userId);

    /**
     * 检查部门名称是否重复
     *
     * @param name 部门名称
     * @param parentId 父部门ID
     * @param excludeId 排除的部门ID（用于更新时检查）
     * @return 是否重复
     */
    boolean checkDepartmentNameExists(String name, Long parentId, Long excludeId);

    /**
     * 获取部门及其所有子部门ID
     *
     * @param departmentId 部门ID
     * @return 部门ID列表
     */
    List<Long> getDepartmentAndChildrenIds(Long departmentId);
}
