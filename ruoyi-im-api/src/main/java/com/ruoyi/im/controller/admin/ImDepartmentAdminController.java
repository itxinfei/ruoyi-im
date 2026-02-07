package com.ruoyi.im.controller.admin;

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
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.ruoyi.im.util.BeanConvertUtil;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 管理员-部门管理控制器
 * 提供部门管理、部门成员管理等管理员功能
 *
 * @author ruoyi
 */
@Tag(name = "管理员-部门管理", description = "管理员部门管理接口")
@RestController
@RequestMapping("/api/admin/departments")
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
@Validated
public class ImDepartmentAdminController {

    private final ImOrganizationService imOrganizationService;

    /**
     * 构造器注入依赖
     *
     * @param imOrganizationService 组织架构服务
     */
    public ImDepartmentAdminController(ImOrganizationService imOrganizationService) {
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
        BeanConvertUtil.copyProperties(department, vo);
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
            return Collections.emptyList();
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
    @Operation(summary = "获取部门树形结构", description = "管理员获取完整的部门树，包含所有层级的部门和成员数量")
    @GetMapping("/tree")
    public Result<List<ImDepartmentTreeVO>> getDepartmentTree() {
        List<ImDepartmentTreeVO> tree = imOrganizationService.getDepartmentTree();
        return Result.success(tree);
    }

    /**
     * 获取部门详情
     * 根据ID获取指定部门的详细信息
     *
     * @param id 部门ID
     * @return 部门信息
     */
    @Operation(summary = "获取部门详情", description = "管理员根据ID获取指定部门的详细信息")
    @GetMapping("/{id}")
    public Result<ImDepartmentVO> getById(@Parameter(description = "部门ID") @PathVariable Long id) {
        ImDepartment department = imOrganizationService.getDepartmentById(id);
        return Result.success(toVO(department));
    }

    /**
     * 创建部门
     * 创建新的部门
     *
     * @param request 创建请求
     * @return 部门ID
     */
    @Operation(summary = "创建部门", description = "管理员创建新的部门")
    @PostMapping
    public Result<Long> create(
            @Parameter(description = "创建请求")
            @Valid @RequestBody ImDepartmentCreateRequest request) {
        Long departmentId = imOrganizationService.createDepartment(request);
        return Result.success("创建成功", departmentId);
    }

    /**
     * 更新部门信息
     * 更新指定部门的信息
     *
     * @param request 更新请求
     * @return 操作结果
     */
    @Operation(summary = "更新部门信息", description = "管理员更新指定部门的信息")
    @PutMapping
    public Result<Void> update(
            @Parameter(description = "更新请求")
            @Valid @RequestBody ImDepartmentUpdateRequest request) {
        imOrganizationService.updateDepartment(request);
        return Result.success("更新成功");
    }

    /**
     * 删除部门
     * 删除指定的部门，只能删除没有子部门和成员的部门
     *
     * @param id 部门ID
     * @return 操作结果
     */
    @Operation(summary = "删除部门", description = "管理员删除指定的部门，只能删除没有子部门和成员的部门")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@Parameter(description = "部门ID") @PathVariable Long id) {
        imOrganizationService.deleteDepartment(id);
        return Result.success("删除成功");
    }

    /**
     * 获取部门成员列表
     * 获取指定部门的所有成员信息
     *
     * @param id 部门ID
     * @return 成员列表
     */
    @Operation(summary = "获取部门成员列表", description = "管理员获取指定部门的所有成员信息")
    @GetMapping("/{id}/members")
    public Result<List<ImDepartmentMemberVO>> getMembers(
            @Parameter(description = "部门ID") @PathVariable Long id) {
        List<ImDepartmentMemberVO> members = imOrganizationService.getDepartmentMembers(id);
        return Result.success(members);
    }

    /**
     * 添加部门成员
     * 向指定部门添加成员
     *
     * @param request 添加请求
     * @return 操作结果
     */
    @Operation(summary = "添加部门成员", description = "管理员向指定部门添加成员")
    @PostMapping("/member")
    public Result<Void> addMember(
            @Parameter(description = "添加请求")
            @Valid @RequestBody ImDepartmentMemberAddRequest request) {
        imOrganizationService.addDepartmentMember(request);
        return Result.success("添加成功");
    }

    /**
     * 移除部门成员
     * 从指定部门移除成员
     *
     * @param id 部门ID
     * @param userId 用户ID
     * @return 操作结果
     */
    @Operation(summary = "移除部门成员", description = "管理员从指定部门移除成员")
    @DeleteMapping("/{id}/member/{userId}")
    public Result<Void> removeMember(
            @Parameter(description = "部门ID") @PathVariable Long id,
            @Parameter(description = "用户ID") @PathVariable Long userId) {
        imOrganizationService.removeDepartmentMember(id, userId);
        return Result.success("移除成功");
    }

    /**
     * 设置部门负责人
     * 设置指定部门的负责人
     *
     * @param id 部门ID
     * @param leaderId 负责人用户ID
     * @return 操作结果
     */
    @Operation(summary = "设置部门负责人", description = "管理员设置指定部门的负责人")
    @PutMapping("/{id}/leader/{leaderId}")
    public Result<Void> setLeader(
            @Parameter(description = "部门ID") @PathVariable Long id,
            @Parameter(description = "负责人用户ID") @PathVariable Long leaderId) {
        imOrganizationService.setDepartmentLeader(id, leaderId);
        return Result.success("设置成功");
    }

    /**
     * 移动部门
     * 将部门移动到新的父部门下
     *
     * @param id 部门ID
     * @param newParentId 新的父部门ID
     * @return 操作结果
     */
    @Operation(summary = "移动部门", description = "管理员将部门移动到新的父部门下")
    @PutMapping("/{id}/move/{newParentId}")
    public Result<Void> move(
            @Parameter(description = "部门ID") @PathVariable Long id,
            @Parameter(description = "新的父部门ID") @PathVariable Long newParentId) {
        imOrganizationService.moveDepartment(id, newParentId);
        return Result.success("移动成功");
    }

    /**
     * 获取用户所属部门列表
     * 获取指定用户所属的所有部门
     *
     * @param userId 用户ID
     * @return 部门列表
     */
    @Operation(summary = "获取用户所属部门列表", description = "管理员获取指定用户所属的所有部门")
    @GetMapping("/user/{userId}")
    public Result<List<ImDepartmentVO>> getUserDepartments(
            @Parameter(description = "用户ID") @PathVariable Long userId) {
        List<ImDepartment> departments = imOrganizationService.getUserDepartments(userId);
        return Result.success(toVOList(departments));
    }

    /**
     * 获取部门统计信息
     * 获取部门相关的统计数据
     *
     * @return 统计信息
     */
    @Operation(summary = "获取部门统计信息", description = "获取部门总数、成员数等统计信息")
    @GetMapping("/stats")
    public Result<java.util.Map<String, Object>> getStats() {
        // 获取部门树来计算统计信息
        List<ImDepartmentTreeVO> tree = imOrganizationService.getDepartmentTree();

        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("totalDepartments", countDepartments(tree));
        stats.put("totalMembers", countMembers(tree));

        return Result.success(stats);
    }

    /**
     * 递归计算部门总数
     *
     * @param departments 部门列表
     * @return 部门数量
     */
    private int countDepartments(List<ImDepartmentTreeVO> departments) {
        if (departments == null || departments.isEmpty()) {
            return 0;
        }
        int count = departments.size();
        for (ImDepartmentTreeVO dept : departments) {
            if (dept.getChildren() != null && !dept.getChildren().isEmpty()) {
                count += countDepartments(dept.getChildren());
            }
        }
        return count;
    }

    /**
     * 递归计算成员总数
     *
     * @param departments 部门列表
     * @return 成员数量
     */
    private int countMembers(List<ImDepartmentTreeVO> departments) {
        if (departments == null || departments.isEmpty()) {
            return 0;
        }
        int count = 0;
        for (ImDepartmentTreeVO dept : departments) {
            count += dept.getMemberCount() != null ? dept.getMemberCount() : 0;
            if (dept.getChildren() != null && !dept.getChildren().isEmpty()) {
                count += countMembers(dept.getChildren());
            }
        }
        return count;
    }
}
