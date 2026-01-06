package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.domain.ImApproval;
import com.ruoyi.im.domain.ImApprovalTemplate;
import com.ruoyi.im.service.ImApprovalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 审批中心控制器
 * 提供审批发起、查询、处理等核心功能
 *
 * @author ruoyi
 */
@Tag(name = "审批中心", description = "审批发起、查询、处理等接口")
@RestController
@RequestMapping("/api/im/approval")
public class ImApprovalController {

    @Autowired
    private ImApprovalService approvalService;

    /**
     * 发起审批
     * 创建新的审批申请
     *
     * @param templateId 审批模板ID
     * @param title 审批标题
     * @param formData 表单数据
     * @param userId 当前登录用户ID，从请求头中获取
     * @return 创建结果，包含审批ID
     */
    @Operation(summary = "发起审批", description = "创建新的审批申请")
    @PostMapping("/create")
    public Result<Long> createApproval(@RequestParam Long templateId,
                                      @RequestParam String title,
                                      @RequestBody Map<String, Object> formData,
                                      @RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 1L; // 开发环境默认用户
        }
        Long approvalId = approvalService.createApproval(templateId, title, formData, userId);
        return Result.success("提交成功", approvalId);
    }

    /**
     * 获取审批详情
     * 根据审批ID获取审批详细信息
     *
     * @param id 审批ID
     * @return 审批详情
     */
    @Operation(summary = "获取审批详情", description = "获取审批详细信息")
    @GetMapping("/{id}")
    public Result<Map<String, Object>> getDetail(@PathVariable Long id) {
        Map<String, Object> detail = approvalService.getApprovalDetail(id);
        return Result.success(detail);
    }

    /**
     * 获取待我审批列表
     * 获取需要当前用户审批的审批列表
     *
     * @param userId 当前登录用户ID，从请求头中获取
     * @return 待审批列表
     */
    @Operation(summary = "获取待我审批列表", description = "获取需要当前用户审批的审批列表")
    @GetMapping("/pending")
    public Result<List<ImApproval>> getPendingApprovals(@RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 1L; // 开发环境默认用户
        }
        List<ImApproval> list = approvalService.getPendingApprovals(userId);
        return Result.success(list);
    }

    /**
     * 获取我发起的审批列表
     * 获取当前用户发起的审批列表
     *
     * @param userId 当前登录用户ID，从请求头中获取
     * @return 我发起的审批列表
     */
    @Operation(summary = "获取我发起的审批列表", description = "获取当前用户发起的审批列表")
    @GetMapping("/my")
    public Result<List<ImApproval>> getMyApprovals(@RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 1L; // 开发环境默认用户
        }
        List<ImApproval> list = approvalService.getMyApprovals(userId);
        return Result.success(list);
    }

    /**
     * 获取我已审批列表
     * 获取当前用户已经审批的审批列表
     *
     * @param userId 当前登录用户ID，从请求头中获取
     * @return 我已审批的审批列表
     */
    @Operation(summary = "获取我已审批列表", description = "获取当前用户已经审批的审批列表")
    @GetMapping("/processed")
    public Result<List<ImApproval>> getProcessedApprovals(@RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 1L; // 开发环境默认用户
        }
        List<ImApproval> list = approvalService.getProcessedApprovals(userId);
        return Result.success(list);
    }

    /**
     * 通过审批
     * 批准指定的审批申请
     *
     * @param id 审批ID
     * @param comment 审批意见
     * @param userId 当前登录用户ID，从请求头中获取
     * @return 操作结果
     */
    @Operation(summary = "通过审批", description = "批准指定的审批申请")
    @PostMapping("/{id}/approve")
    public Result<Void> approve(@PathVariable Long id,
                               @RequestParam(required = false) String comment,
                               @RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 1L; // 开发环境默认用户
        }
        approvalService.processApproval(id, "APPROVE", comment, userId);
        return Result.success("已通过");
    }

    /**
     * 驳回审批
     * 驳回指定的审批申请
     *
     * @param id 审批ID
     * @param comment 审批意见
     * @param userId 当前登录用户ID，从请求头中获取
     * @return 操作结果
     */
    @Operation(summary = "驳回审批", description = "驳回指定的审批申请")
    @PostMapping("/{id}/reject")
    public Result<Void> reject(@PathVariable Long id,
                              @RequestParam String comment,
                              @RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 1L; // 开发环境默认用户
        }
        approvalService.processApproval(id, "REJECT", comment, userId);
        return Result.success("已驳回");
    }

    /**
     * 撤回审批
     * 撤回当前用户发起的审批申请
     *
     * @param id 审批ID
     * @param userId 当前登录用户ID，从请求头中获取
     * @return 操作结果
     */
    @Operation(summary = "撤回审批", description = "撤回当前用户发起的审批申请")
    @PostMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id,
                              @RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 1L; // 开发环境默认用户
        }
        approvalService.cancelApproval(id, userId);
        return Result.success("已撤回");
    }

    /**
     * 获取审批模板列表
     * 获取所有审批模板
     *
     * @return 审批模板列表
     */
    @Operation(summary = "获取审批模板列表", description = "获取所有审批模板")
    @GetMapping("/templates")
    public Result<List<ImApprovalTemplate>> getTemplates() {
        List<ImApprovalTemplate> list = approvalService.getTemplates();
        return Result.success(list);
    }

    /**
     * 获取启用的审批模板列表
     * 获取所有启用状态的审批模板
     *
     * @return 启用的审批模板列表
     */
    @Operation(summary = "获取启用的审批模板列表", description = "获取所有启用状态的审批模板")
    @GetMapping("/templates/active")
    public Result<List<ImApprovalTemplate>> getActiveTemplates() {
        List<ImApprovalTemplate> list = approvalService.getActiveTemplates();
        return Result.success(list);
    }
}