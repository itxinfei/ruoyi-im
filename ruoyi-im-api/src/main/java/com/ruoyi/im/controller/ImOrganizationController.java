package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.domain.ImDepartment;
import com.ruoyi.im.dto.organization.ImDepartmentCreateRequest;
import com.ruoyi.im.dto.organization.ImDepartmentMemberAddRequest;
import com.ruoyi.im.dto.organization.ImDepartmentUpdateRequest;
import com.ruoyi.im.service.ImOrganizationService;
import com.ruoyi.im.vo.organization.ImDepartmentMemberVO;
import com.ruoyi.im.vo.organization.ImDepartmentTreeVO;
import com.ruoyi.im.vo.organization.ImDepartmentVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.BeanUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 组织架构控制器
 * 提供部门管理、部门成员管理等功能
 *
 * @author ruoyi
 */
@Tag(name = "组织架构", description = "组织架构管理接口，提供部门管理、部门成员管理等功能")
@RestController
@RequestMapping("/api/im/organization")
@Validated
public class ImOrganizationController {

    private final ImOrganizationService imOrganizationService;

    public ImOrganizationController(ImOrganizationService imOrganizationService) {
        this.imOrganizationService = imOrganizationService;
    }

    /**
     * 将 Entity 转换为 VO
     *
     * @param department 部门实体
     * @return 部门视图对象
     */
    private ImDepartmentVO toVO(ImDepartment department) {
        if (department == null) {
            return new ImDepartmentVO();
        }
        ImDepartmentVO vo = new ImDepartmentVO();
        BeanUtils.copyProperties(department, vo);
        return vo;
    }

    /**
     * 批量将 Entity 转换为 VO
     *
     * @param list 部门实体列表
     * @return 部门视图对象列表
     */
    private List<ImDepartmentVO> toVOList(List<ImDepartment> list) {
        if (list == null || list.isEmpty()) {
            return List.of();
        }
        return list.stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    /**
     * 获取部门树形结构
     * 获取完整的部门树，包含所有层级的部门和成员数量
     *
     * @return 部门树形结构
     */
    @Operation(summary = "获取部门树形结构", description = "获取完整的部门树，包含所有层级的部门和成员数量")
    @GetMapping("/department/tree")
    public Result<List<ImDepartmentTreeVO>> getDepartmentTree() {
        List<ImDepartmentTreeVO> tree = imOrganizationService.getDepartmentTree();
        return Result.success(tree);
    }

    /**
     * 根据ID获取部门信息
     * 获取指定部门的详细信息
     *
     * @param departmentId 部门ID
     * @return 部门信息
     */
    @Operation(summary = "获取部门详情", description = "根据ID获取指定部门的详细信息")
    @GetMapping("/department/{departmentId}")
    public Result<ImDepartmentVO> getDepartmentById(@PathVariable Long departmentId) {
        ImDepartment department = imOrganizationService.getDepartmentById(departmentId);
        return Result.success(toVO(department));
    }

    /**
     * 创建部门
     * 创建新的部门
     *
     * @param request 创建请求
     * @return 部门ID
     */
    @Operation(summary = "创建部门", description = "创建新的部门")
    @PostMapping("/department")
    public Result<Long> createDepartment(@Valid @RequestBody ImDepartmentCreateRequest request) {
        Long departmentId = imOrganizationService.createDepartment(request);
        return Result.success(departmentId);
    }

    /**
     * 更新部门信息
     * 更新指定部门的信息
     *
     * @param request 更新请求
     * @return 操作结果
     */
    @Operation(summary = "更新部门信息", description = "更新指定部门的信息")
    @PutMapping("/department")
    public Result<Void> updateDepartment(@Valid @RequestBody ImDepartmentUpdateRequest request) {
        imOrganizationService.updateDepartment(request);
        return Result.success();
    }

    /**
     * 删除部门
     * 删除指定的部门，只能删除没有子部门和成员的部门
     *
     * @param departmentId 部门ID
     * @return 操作结果
     */
    @Operation(summary = "删除部门", description = "删除指定的部门，只能删除没有子部门和成员的部门")
    @DeleteMapping("/department/{departmentId}")
    public Result<Void> deleteDepartment(@PathVariable Long departmentId) {
        imOrganizationService.deleteDepartment(departmentId);
        return Result.success();
    }

    /**
     * 获取部门成员列表
     * 获取指定部门的所有成员信息
     *
     * @param departmentId 部门ID
     * @return 成员列表
     */
    @Operation(summary = "获取部门成员列表", description = "获取指定部门的所有成员信息")
    @GetMapping("/department/{departmentId}/members")
    public Result<List<ImDepartmentMemberVO>> getDepartmentMembers(@PathVariable Long departmentId) {
        List<ImDepartmentMemberVO> members = imOrganizationService.getDepartmentMembers(departmentId);
        return Result.success(members);
    }

    /**
     * 添加部门成员
     * 向指定部门添加成员
     *
     * @param request 添加请求
     * @return 操作结果
     */
    @Operation(summary = "添加部门成员", description = "向指定部门添加成员")
    @PostMapping("/department/member")
    public Result<Void> addDepartmentMember(@Valid @RequestBody ImDepartmentMemberAddRequest request) {
        imOrganizationService.addDepartmentMember(request);
        return Result.success();
    }

    /**
     * 移除部门成员
     * 从指定部门移除成员
     *
     * @param departmentId 部门ID
     * @param userId 用户ID
     * @return 操作结果
     */
    @Operation(summary = "移除部门成员", description = "从指定部门移除成员")
    @DeleteMapping("/department/{departmentId}/member/{userId}")
    public Result<Void> removeDepartmentMember(@PathVariable Long departmentId, @PathVariable Long userId) {
        imOrganizationService.removeDepartmentMember(departmentId, userId);
        return Result.success();
    }

    /**
     * 设置用户主部门
     * 设置用户的主部门，每个用户只能有一个主部门
     *
     * @param userId 用户ID
     * @param departmentId 部门ID
     * @return 操作结果
     */
    @Operation(summary = "设置用户主部门", description = "设置用户的主部门，每个用户只能有一个主部门")
    @PutMapping("/user/{userId}/primary-department/{departmentId}")
    public Result<Void> setPrimaryDepartment(@PathVariable Long userId, @PathVariable Long departmentId) {
        imOrganizationService.setPrimaryDepartment(userId, departmentId);
        return Result.success();
    }

    /**
     * 获取用户所属部门列表
     * 获取指定用户所属的所有部门
     *
     * @param userId 用户ID
     * @return 部门列表
     */
    @Operation(summary = "获取用户所属部门列表", description = "获取指定用户所属的所有部门")
    @GetMapping("/user/{userId}/departments")
    public Result<List<ImDepartmentVO>> getUserDepartments(@PathVariable Long userId) {
        List<ImDepartment> departments = imOrganizationService.getUserDepartments(userId);
        return Result.success(toVOList(departments));
    }

    /**
     * 获取用户主部门
     * 获取用户的主部门信息
     *
     * @param userId 用户ID
     * @return 主部门信息
     */
    @Operation(summary = "获取用户主部门", description = "获取用户的主部门信息")
    @GetMapping("/user/{userId}/primary-department")
    public Result<ImDepartmentVO> getUserPrimaryDepartment(@PathVariable Long userId) {
        ImDepartment department = imOrganizationService.getUserPrimaryDepartment(userId);
        return Result.success(toVO(department));
    }
}
