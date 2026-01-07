package com.ruoyi.web.controller.im;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.web.domain.ImApproval;
import com.ruoyi.web.service.ImApprovalService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * IM审批管理控制器（管理后台）
 *
 * @author ruoyi
 * @date 2025-01-07
 */
@RestController
@RequestMapping("api/admin/im/approval")
public class ImApprovalAdminController extends BaseController {

    @Autowired
    private ImApprovalService approvalService;

    /**
     * 获取审批详情
     */
    @RequiresPermissions("im:approval:query")
    @GetMapping("/{id}")
    public AjaxResult getDetail(@PathVariable Long id) {
        Map<String, Object> detail = approvalService.getApprovalDetail(id);
        return AjaxResult.success(detail);
    }

    /**
     * 获取审批列表（管理员视角）
     */
    @RequiresPermissions("im:approval:list")
    @GetMapping("/list")
    public AjaxResult getList(@RequestParam(required = false) String status,
                             @RequestParam(required = false) Long userId,
                             @RequestParam(required = false) String title) {
        startPage();
        List<ImApproval> list = approvalService.selectApprovalListForAdmin(status, userId, title);
        return AjaxResult.success(list);
    }

    /**
     * 获取我发起的审批列表
     */
    @RequiresPermissions("im:approval:list")
    @GetMapping("/my")
    public AjaxResult getMyApprovals(@RequestParam Long userId) {
        List<ImApproval> list = approvalService.getMyApprovals(userId);
        return AjaxResult.success(list);
    }

    /**
     * 获取审批模板列表
     */
    @RequiresPermissions("im:approval:query")
    @GetMapping("/templates")
    public AjaxResult getTemplates() {
        return AjaxResult.success(approvalService.getTemplates());
    }

    /**
     * 获取启用的审批模板列表
     */
    @RequiresPermissions("im:approval:query")
    @GetMapping("/templates/active")
    public AjaxResult getActiveTemplates() {
        return AjaxResult.success(approvalService.getActiveTemplates());
    }

    /**
     * 启用/禁用审批模板
     */
    @RequiresPermissions("im:approval:edit")
    @Log(title = "修改审批模板状态", businessType = BusinessType.UPDATE)
    @PutMapping("/templates/{id}/status")
    public AjaxResult updateTemplateStatus(@PathVariable Long id,
                                      @RequestParam Boolean isActive) {
        approvalService.updateTemplateStatus(id, isActive);
        return AjaxResult.success("更新成功");
    }

    /**
     * 通过审批
     */
    @RequiresPermissions("im:approval:approve")
    @Log(title = "通过审批", businessType = BusinessType.UPDATE)
    @PostMapping("/{id}/approve")
    public AjaxResult approve(@PathVariable Long id,
                           @RequestParam(required = false) String comment,
                           @RequestParam Long userId) {
        approvalService.processApproval(id, "APPROVE", comment, userId);
        return AjaxResult.success("已通过");
    }

    /**
     * 驳回审批
     */
    @RequiresPermissions("im:approval:reject")
    @Log(title = "驳回审批", businessType = BusinessType.UPDATE)
    @PostMapping("/{id}/reject")
    public AjaxResult reject(@PathVariable Long id,
                          @RequestParam String comment,
                          @RequestParam Long userId) {
        approvalService.processApproval(id, "REJECT", comment, userId);
        return AjaxResult.success("已驳回");
    }

    /**
     * 撤销审批
     */
    @RequiresPermissions("im:approval:cancel")
    @Log(title = "撤销审批", businessType = BusinessType.UPDATE)
    @PostMapping("/{id}/cancel")
    public AjaxResult cancel(@PathVariable Long id,
                           @RequestParam Long userId) {
        approvalService.cancelApproval(id, userId);
        return AjaxResult.success("已撤销");
    }

    /**
     * 获取审批统计
     */
    @RequiresPermissions("im:approval:list")
    @GetMapping("/statistics")
    public AjaxResult getStatistics(@RequestParam(required = false) String startTime,
                                   @RequestParam(required = false) String endTime) {
        Map<String, Object> stats = approvalService.getApprovalStatistics(startTime, endTime);
        return AjaxResult.success(stats);
    }
}
