package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.domain.ImApproval;
import com.ruoyi.im.domain.ImApprovalTemplate;
import com.ruoyi.im.service.ImApprovalService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 审批中心控制器
 *
 * @author ruoyi
 */
@Api(tags = "审批中心")
@RestController
@RequestMapping("/api/im/approval")
public class ImApprovalController {

    @Autowired
    private ImApprovalService approvalService;

    /**
     * 发起审批
     */
    @ApiOperation("发起审批")
    @PostMapping("/create")
    public Result<Long> createApproval(@RequestParam Long templateId,
                                      @RequestParam String title,
                                      @RequestBody Map<String, Object> formData,
                                      @RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 1L;
        }
        Long approvalId = approvalService.createApproval(templateId, title, formData, userId);
        return Result.success("提交成功", approvalId);
    }

    /**
     * 获取审批详情
     */
    @ApiOperation("获取审批详情")
    @GetMapping("/{id}")
    public Result<Map<String, Object>> getDetail(@PathVariable Long id) {
        Map<String, Object> detail = approvalService.getApprovalDetail(id);
        return Result.success(detail);
    }

    /**
     * 获取待我审批列表
     */
    @ApiOperation("获取待我审批列表")
    @GetMapping("/pending")
    public Result<List<ImApproval>> getPendingApprovals(@RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 1L;
        }
        List<ImApproval> list = approvalService.getPendingApprovals(userId);
        return Result.success(list);
    }

    /**
     * 获取我发起的审批列表
     */
    @ApiOperation("获取我发起的审批列表")
    @GetMapping("/my")
    public Result<List<ImApproval>> getMyApprovals(@RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 1L;
        }
        List<ImApproval> list = approvalService.getMyApprovals(userId);
        return Result.success(list);
    }

    /**
     * 获取我已审批列表
     */
    @ApiOperation("获取我已审批列表")
    @GetMapping("/processed")
    public Result<List<ImApproval>> getProcessedApprovals(@RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 1L;
        }
        List<ImApproval> list = approvalService.getProcessedApprovals(userId);
        return Result.success(list);
    }

    /**
     * 通过审批
     */
    @ApiOperation("通过审批")
    @PostMapping("/{id}/approve")
    public Result<Void> approve(@PathVariable Long id,
                               @RequestParam(required = false) String comment,
                               @RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 1L;
        }
        approvalService.processApproval(id, "APPROVE", comment, userId);
        return Result.success("已通过");
    }

    /**
     * 驳回审批
     */
    @ApiOperation("驳回审批")
    @PostMapping("/{id}/reject")
    public Result<Void> reject(@PathVariable Long id,
                              @RequestParam String comment,
                              @RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 1L;
        }
        approvalService.processApproval(id, "REJECT", comment, userId);
        return Result.success("已驳回");
    }

    /**
     * 撤回审批
     */
    @ApiOperation("撤回审批")
    @PostMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id,
                              @RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 1L;
        }
        approvalService.cancelApproval(id, userId);
        return Result.success("已撤回");
    }

    /**
     * 获取审批模板列表
     */
    @ApiOperation("获取审批模板列表")
    @GetMapping("/templates")
    public Result<List<ImApprovalTemplate>> getTemplates() {
        List<ImApprovalTemplate> list = approvalService.getTemplates();
        return Result.success(list);
    }

    /**
     * 获取启用的审批模板列表
     */
    @ApiOperation("获取启用的审批模板列表")
    @GetMapping("/templates/active")
    public Result<List<ImApprovalTemplate>> getActiveTemplates() {
        List<ImApprovalTemplate> list = approvalService.getActiveTemplates();
        return Result.success(list);
    }
}
