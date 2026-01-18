package com.ruoyi.web.controller.im;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.web.domain.ImAuditLog;
import com.ruoyi.web.service.ImAuditLogService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * 审计日志管理控制器
 *
 * @author ruoyi
 * @date 2025-01-18
 */
@Controller
@RequestMapping("/im/auditLog")
public class ImAuditLogController extends BaseController {

    private String prefix = "im/auditLog";

    @Autowired
    private ImAuditLogService imAuditLogService;

    @RequiresPermissions("im:auditLog:view")
    @GetMapping()
    public String auditLog() {
        return prefix + "/auditLog";
    }

    /**
     * 查询审计日志列表
     */
    @RequiresPermissions("im:auditLog:list")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(ImAuditLog imAuditLog) {
        startPage();
        List<ImAuditLog> list = imAuditLogService.selectImAuditLogList(imAuditLog);
        return getDataTable(list);
    }

    /**
     * 获取审计日志统计数据
     */
    @RequiresPermissions("im:auditLog:query")
    @GetMapping("/statistics")
    @ResponseBody
    public AjaxResult getStatistics() {
        return AjaxResult.success(imAuditLogService.getAuditLogStatistics());
    }

    /**
     * 按操作类型统计
     */
    @RequiresPermissions("im:auditLog:query")
    @GetMapping("/statsByType")
    @ResponseBody
    public AjaxResult getStatsByType() {
        return AjaxResult.success(imAuditLogService.countByOperationType());
    }

    /**
     * 按用户统计
     */
    @RequiresPermissions("im:auditLog:query")
    @GetMapping("/statsByUser")
    @ResponseBody
    public AjaxResult getStatsByUser() {
        return AjaxResult.success(imAuditLogService.countByUser());
    }

    /**
     * 获取审计日志详细信息
     */
    @RequiresPermissions("im:auditLog:query")
    @GetMapping("/info/{id}")
    @ResponseBody
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return AjaxResult.success(imAuditLogService.selectImAuditLogById(id));
    }

    /**
     * 导出审计日志列表
     */
    @RequiresPermissions("im:auditLog:export")
    @Log(title = "审计日志管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    @ResponseBody
    public void export(HttpServletResponse response, ImAuditLog imAuditLog) {
        List<ImAuditLog> list = imAuditLogService.selectImAuditLogList(imAuditLog);
        ExcelUtil<ImAuditLog> util = new ExcelUtil<>(ImAuditLog.class);
        util.exportExcel(response, list, "审计日志数据");
    }

    /**
     * 清理旧日志
     */
    @RequiresPermissions("im:auditLog:remove")
    @Log(title = "审计日志管理", businessType = BusinessType.CLEAN)
    @PostMapping("/clean")
    @ResponseBody
    public AjaxResult clean(@RequestParam(defaultValue = "90") int days) {
        int count = imAuditLogService.deleteOldLogs(days);
        return AjaxResult.success("成功清理 " + count + " 条日志记录");
    }

    /**
     * 获取失败日志数量
     */
    @RequiresPermissions("im:auditLog:query")
    @GetMapping("/failedCount")
    @ResponseBody
    public AjaxResult getFailedCount(@RequestParam(defaultValue = "24") int hours) {
        int count = imAuditLogService.countFailedLogs(hours);
        return AjaxResult.success(count);
    }
}
