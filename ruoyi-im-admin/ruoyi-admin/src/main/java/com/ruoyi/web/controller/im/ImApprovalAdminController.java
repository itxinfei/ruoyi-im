package com.ruoyi.web.controller.im;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.web.domain.ImApproval;
import com.ruoyi.web.service.ImApprovalService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * IM审批管理控制器（管理后台）
 *
 * @author ruoyi
 * @date 2025-01-07
 */
@Controller
@RequestMapping("/im/approval")
public class ImApprovalAdminController extends BaseController {

    private String prefix = "im/approval";

    @Autowired
    private ImApprovalService approvalService;

    /**
     * 审批管理页面
     */
    @RequiresPermissions("im:approval:view")
    @GetMapping()
    public String approval() {
        return prefix + "/approval";
    }

    /**
     * 查询审批列表
     */
    @RequiresPermissions("im:approval:list")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(ImApproval imApproval) {
        startPage();
        List<ImApproval> list = approvalService.selectImApprovalList(imApproval);
        return getDataTable(list);
    }

    /**
     * 导出审批列表
     */
    @RequiresPermissions("im:approval:export")
    @Log(title = "审批管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    @ResponseBody
    public void export(HttpServletResponse response, ImApproval imApproval) {
        List<ImApproval> list = approvalService.selectImApprovalList(imApproval);
        ExcelUtil<ImApproval> util = new ExcelUtil<>(ImApproval.class);
        util.exportExcel(response, list, "审批数据");
    }

    /**
     * 获取审批详细信息
     */
    @RequiresPermissions("im:approval:query")
    @GetMapping("/{id}")
    @ResponseBody
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return AjaxResult.success(approvalService.selectImApprovalById(id));
    }

    /**
     * 获取审批详情（包含流程节点）
     */
    @RequiresPermissions("im:approval:query")
    @GetMapping("/{id}/detail")
    @ResponseBody
    public AjaxResult getDetail(@PathVariable("id") Long id) {
        Map<String, Object> detail = approvalService.getApprovalDetail(id);
        return AjaxResult.success(detail);
    }

    /**
     * 新增审批
     */
    @RequiresPermissions("im:approval:add")
    @Log(title = "审批管理", businessType = BusinessType.INSERT)
    @PostMapping
    @ResponseBody
    public AjaxResult add(@RequestBody ImApproval imApproval) {
        return toAjax(approvalService.insertImApproval(imApproval));
    }

    /**
     * 修改审批
     */
    @RequiresPermissions("im:approval:edit")
    @Log(title = "审批管理", businessType = BusinessType.UPDATE)
    @PutMapping
    @ResponseBody
    public AjaxResult edit(@RequestBody ImApproval imApproval) {
        return toAjax(approvalService.updateImApproval(imApproval));
    }

    /**
     * 删除审批
     */
    @RequiresPermissions("im:approval:remove")
    @Log(title = "审批管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    @ResponseBody
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(approvalService.deleteImApprovalByIds(ids));
    }

    /**
     * 通过审批
     */
    @RequiresPermissions("im:approval:approve")
    @Log(title = "通过审批", businessType = BusinessType.UPDATE)
    @PutMapping("/{id}/approve")
    @ResponseBody
    public AjaxResult approve(@PathVariable("id") Long id,
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
    @PutMapping("/{id}/reject")
    @ResponseBody
    public AjaxResult reject(@PathVariable("id") Long id,
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
    @PutMapping("/{id}/cancel")
    @ResponseBody
    public AjaxResult cancel(@PathVariable("id") Long id,
                           @RequestParam Long userId) {
        approvalService.cancelApproval(id, userId);
        return AjaxResult.success("已撤销");
    }

    /**
     * 获取审批统计
     */
    @RequiresPermissions("im:approval:list")
    @GetMapping("/statistics")
    @ResponseBody
    public AjaxResult getStatistics(@RequestParam(required = false) String startTime,
                                   @RequestParam(required = false) String endTime) {
        Map<String, Object> stats = approvalService.getApprovalStatistics(startTime, endTime);
        return AjaxResult.success(stats);
    }

    /**
     * 获取审批模板列表
     */
    @RequiresPermissions("im:approval:query")
    @GetMapping("/templates")
    @ResponseBody
    public AjaxResult getTemplates() {
        return AjaxResult.success(approvalService.getTemplates());
    }

    /**
     * 启用/禁用审批模板
     */
    @RequiresPermissions("im:approval:edit")
    @Log(title = "修改审批模板状态", businessType = BusinessType.UPDATE)
    @PutMapping("/templates/{id}/status")
    @ResponseBody
    public AjaxResult updateTemplateStatus(@PathVariable("id") Long id,
                                      @RequestParam Boolean isActive) {
        approvalService.updateTemplateStatus(id, isActive);
        return AjaxResult.success("更新成功");
    }
}
