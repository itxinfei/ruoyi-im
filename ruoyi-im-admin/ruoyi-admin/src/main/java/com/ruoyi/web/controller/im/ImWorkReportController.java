package com.ruoyi.web.controller.im;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.web.domain.ImWorkReport;
import com.ruoyi.web.service.ImWorkReportService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 工作报告管理控制器
 *
 * @author ruoyi
 */
@Controller
@RequestMapping("/im/workReport")
public class ImWorkReportController extends BaseController {

    private String prefix = "im/workReport";

    @Autowired
    private ImWorkReportService imWorkReportService;

    @RequiresPermissions("im:workReport:view")
    @GetMapping()
    public String workReport() {
        return prefix + "/workReport";
    }

    /**
     * 查询工作报告列表
     */
    @RequiresPermissions("im:workReport:list")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(ImWorkReport imWorkReport) {
        startPage();
        List<ImWorkReport> list = imWorkReportService.selectImWorkReportList(imWorkReport);
        return getDataTable(list);
    }

    /**
     * 获取工作报告统计数据
     */
    @RequiresPermissions("im:workReport:query")
    @GetMapping("/statistics")
    @ResponseBody
    public AjaxResult getStatistics() {
        return AjaxResult.success(imWorkReportService.getReportStatistics());
    }

    /**
     * 获取工作报告详细信息
     */
    @RequiresPermissions("im:workReport:query")
    @GetMapping("/info/{id}")
    @ResponseBody
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return AjaxResult.success(imWorkReportService.selectImWorkReportById(id));
    }

    /**
     * 新增工作报告
     */
    @RequiresPermissions("im:workReport:add")
    @Log(title = "工作报告管理", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    @ResponseBody
    public AjaxResult add(ImWorkReport imWorkReport) {
        return toAjax(imWorkReportService.insertImWorkReport(imWorkReport));
    }

    /**
     * 修改工作报告（表单提交方式，用于编辑页面）
     */
    @RequiresPermissions("im:workReport:edit")
    @Log(title = "工作报告管理", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    @ResponseBody
    public AjaxResult edit(ImWorkReport imWorkReport) {
        return toAjax(imWorkReportService.updateImWorkReport(imWorkReport));
    }

    /**
     * 审批报告
     */
    @RequiresPermissions("im:workReport:edit")
    @Log(title = "工作报告管理", businessType = BusinessType.UPDATE)
    @PutMapping("/approve")
    @ResponseBody
    public AjaxResult approve(@RequestParam Long id, @RequestParam Long approverId,
                              @RequestParam String status, @RequestParam(required = false) String remark) {
        return toAjax(imWorkReportService.approveReport(id, approverId, status, remark));
    }

    /**
     * 删除工作报告
     */
    @RequiresPermissions("im:workReport:remove")
    @Log(title = "工作报告管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    @ResponseBody
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(imWorkReportService.deleteImWorkReportByIds(ids));
    }

    /**
     * 导出工作报告列表
     */
    @RequiresPermissions("im:workReport:export")
    @Log(title = "工作报告管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    @ResponseBody
    public void export(HttpServletResponse response, ImWorkReport imWorkReport) {
        List<ImWorkReport> list = imWorkReportService.selectImWorkReportList(imWorkReport);
        ExcelUtil<ImWorkReport> util = new ExcelUtil<>(ImWorkReport.class);
        util.exportExcel(response, list, "工作报告数据");
    }
}
