package com.ruoyi.project.im.controller.im;

import com.ruoyi.framework.aspectj.lang.annotation.Log;
import com.ruoyi.framework.web.controller.BaseController;
import com.ruoyi.framework.web.domain.AjaxResult;
import com.ruoyi.framework.web.page.TableDataInfo;
import com.ruoyi.common.utils.text.Convert;
import com.ruoyi.framework.aspectj.lang.enums.BusinessType;
import com.ruoyi.common.utils.text.StringUtils;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.project.im.domain.ImAttendance;
import com.ruoyi.project.im.service.ImAttendanceService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 考勤记录管理控制器
 *
 * @author ruoyi
 * @date 2025-01-18
 */
@Controller
@RequestMapping("/im/attendance")
public class ImAttendanceController extends BaseController {

    private String prefix = "im/attendance";

    @Autowired
    private ImAttendanceService imAttendanceService;

    @RequiresPermissions("im:attendance:view")
    @GetMapping()
    public String attendance() {
        return prefix + "/attendance";
    }

    /**
     * 查询考勤记录列表
     */
    @RequiresPermissions("im:attendance:list")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(ImAttendance imAttendance) {
        startPage();
        List<ImAttendance> list = imAttendanceService.selectImAttendanceList(imAttendance);
        return getDataTable(list);
    }

    /**
     * 获取考勤统计数据
     */
    @RequiresPermissions("im:attendance:query")
    @GetMapping("/statistics")
    @ResponseBody
    public AjaxResult getStatistics() {
        return AjaxResult.success(imAttendanceService.getAttendanceStatistics());
    }

    /**
     * 查询待审批的考勤记录列表
     */
    @RequiresPermissions("im:attendance:list")
    @PostMapping("/pendingApproval")
    @ResponseBody
    public TableDataInfo pendingApprovalList() {
        startPage();
        List<ImAttendance> list = imAttendanceService.selectPendingApprovalList();
        return getDataTable(list);
    }

    /**
     * 获取考勤记录详细信息
     */
    @RequiresPermissions("im:attendance:query")
    @GetMapping("/info/{id}")
    @ResponseBody
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return AjaxResult.success(imAttendanceService.selectImAttendanceById(id));
    }

    /**
     * 获取用户的考勤记录
     */
    @RequiresPermissions("im:attendance:query")
    @GetMapping("/user/{userId}")
    @ResponseBody
    public AjaxResult getUserAttendance(@PathVariable("userId") Long userId) {
        return AjaxResult.success(imAttendanceService.selectAttendanceByUserId(userId));
    }

    /**
     * 获取今日考勤记录
     */
    @RequiresPermissions("im:attendance:query")
    @GetMapping("/today/{userId}")
    @ResponseBody
    public AjaxResult getTodayAttendance(@PathVariable("userId") Long userId) {
        return AjaxResult.success(imAttendanceService.selectTodayAttendance(userId));
    }

    /**
     * 新增考勤记录
     */
    @RequiresPermissions("im:attendance:add")
    @Log(title = "考勤记录", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    @ResponseBody
    public AjaxResult add(ImAttendance imAttendance) {
        return toAjax(imAttendanceService.insertImAttendance(imAttendance));
    }

    /**
     * 修改考勤记录
     */
    @RequiresPermissions("im:attendance:edit")
    @Log(title = "考勤记录管理", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    @ResponseBody
    public AjaxResult edit(ImAttendance imAttendance) {
        return toAjax(imAttendanceService.updateImAttendance(imAttendance));
    }

    /**
     * 审批考勤记录
     */
    @RequiresPermissions("im:attendance:approve")
    @Log(title = "考勤记录审批", businessType = BusinessType.UPDATE)
    @PostMapping("/approve")
    @ResponseBody
    public AjaxResult approve(@RequestParam Long id,
                              @RequestParam String approveStatus,
                              @RequestParam(required = false) Long approverId,
                              @RequestParam(required = false) String approveComment) {
        return toAjax(imAttendanceService.approveAttendance(id, approveStatus, approverId, approveComment));
    }

    /**
     * 批量审批考勤记录
     */
    @RequiresPermissions("im:attendance:approve")
    @Log(title = "考勤记录批量审批", businessType = BusinessType.UPDATE)
    @PostMapping("/batchApprove")
    @ResponseBody
    public AjaxResult batchApprove(@RequestParam String ids,
                                   @RequestParam String approveStatus,
                                   @RequestParam(required = false) Long approverId,
                                   @RequestParam(required = false) String approveComment) {
        Long[] attendanceIds = Convert.toLongArray(ids);
        return toAjax(imAttendanceService.batchApproveAttendance(attendanceIds, approveStatus, approverId, approveComment));
    }

    /**
     * 删除考勤记录
     */
    @RequiresPermissions("im:attendance:remove")
    @Log(title = "考勤记录管理", businessType = BusinessType.DELETE)
    @PostMapping("/remove/{ids}")
    @ResponseBody
    public AjaxResult remove(@PathVariable String ids) {
        return toAjax(imAttendanceService.deleteImAttendanceByIds(Convert.toLongArray(ids)));
    }

    /**
     * 导出考勤记录列表
     */
    @RequiresPermissions("im:attendance:export")
    @Log(title = "考勤记录管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    @ResponseBody
    public void export(HttpServletResponse response, ImAttendance imAttendance) {
        List<ImAttendance> list = imAttendanceService.selectImAttendanceList(imAttendance);
        ExcelUtil<ImAttendance> util = new ExcelUtil<>(ImAttendance.class);
        util.exportExcel(response, list, "考勤记录数据");
    }
}
