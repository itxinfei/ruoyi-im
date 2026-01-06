package com.ruoyi.web.controller.im;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.im.domain.ImApproval;
import com.ruoyi.im.domain.ImApprovalTemplate;
import com.ruoyi.im.service.ImApprovalService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * IM审批管理控制器
 * 
 * @author ruoyi
 */
@RestController("adminImApprovalController")
@RequestMapping("/api/admin/im/approval")
public class ImApprovalAdminController extends BaseController {

    @Autowired
    private ImApprovalService approvalService;

    /**
     * 获取审批详情
     */
    @RequiresPermissions("im:approval:detail")
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
        // 这里需要根据实际的业务逻辑进行调整
        List<ImApproval> list = approvalService.getApprovalsForAdmin(status, userId, title);
        return AjaxResult.success(list);
    }

    /**
     * 获取我发起的审批列表
     */
    @RequiresPermissions("im:approval:my")
    @GetMapping("/my")
    public AjaxResult getMyApprovals(@RequestParam Long userId) {
        List<ImApproval> list = approvalService.getMyApprovals(userId);
        return AjaxResult.success(list);
    }

    /**
     * 获取审批模板列表
     */
    @RequiresPermissions("im:approval:template")
    @GetMapping("/templates")
    public AjaxResult getTemplates() {
        List<ImApprovalTemplate> list = approvalService.getTemplates();
        return AjaxResult.success(list);
    }

    /**
     * 获取启用的审批模板列表
     */
    @RequiresPermissions("im:approval:template")
    @GetMapping("/templates/active")
    public AjaxResult getActiveTemplates() {
        List<ImApprovalTemplate> list = approvalService.getActiveTemplates();
        return AjaxResult.success(list);
    }

    /**
     * 启用/禁用审批模板
     */
    @RequiresPermissions("im:approval:template:edit")
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
    @PostMapping("/{id}/cancel")
    public AjaxResult cancel(@PathVariable Long id,
                             @RequestParam Long userId) {
        approvalService.cancelApproval(id, userId);
        return AjaxResult.success("已撤销");
    }
}