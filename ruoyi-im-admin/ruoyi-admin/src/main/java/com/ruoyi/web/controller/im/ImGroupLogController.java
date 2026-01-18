package com.ruoyi.web.controller.im;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.core.text.Convert;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.web.domain.ImGroupLog;
import com.ruoyi.web.service.ImGroupLogService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 群组管理日志控制器
 *
 * @author ruoyi
 */
@Controller
@RequestMapping("/im/group/log")
public class ImGroupLogController extends BaseController {

    private String prefix = "im/groupLog";

    @Autowired
    private ImGroupLogService imGroupLogService;

    @RequiresPermissions("im:group:log:view")
    @GetMapping()
    public String log() {
        return prefix + "/log";
    }

    /**
     * 查询群组管理日志列表
     */
    @RequiresPermissions("im:group:log:list")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(ImGroupLog imGroupLog) {
        startPage();
        List<ImGroupLog> list = imGroupLogService.selectImGroupLogList(imGroupLog);
        return getDataTable(list);
    }

    /**
     * 导出群组管理日志列表
     */
    @RequiresPermissions("im:group:log:export")
    @Log(title = "群组管理日志", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, ImGroupLog imGroupLog) {
        List<ImGroupLog> list = imGroupLogService.selectImGroupLogList(imGroupLog);
        ExcelUtil<ImGroupLog> util = new ExcelUtil<>(ImGroupLog.class);
        util.exportExcel(response, list, "群组管理日志数据");
    }

    /**
     * 获取群组管理日志详细信息
     */
    @RequiresPermissions("im:group:log:query")
    @GetMapping("/{id}")
    @ResponseBody
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return AjaxResult.success(imGroupLogService.selectImGroupLogById(id));
    }

    /**
     * 根据群组ID查询日志列表
     */
    @RequiresPermissions("im:group:log:query")
    @GetMapping("/group/{groupId}")
    @ResponseBody
    public AjaxResult getLogsByGroupId(@PathVariable("groupId") Long groupId) {
        return AjaxResult.success(imGroupLogService.selectLogsByGroupId(groupId));
    }

    /**
     * 显示群组日志页面
     */
    @RequiresPermissions("im:group:log:view")
    @GetMapping("/group/{groupId}/view")
    public String viewLogsByGroupId(@PathVariable("groupId") Long groupId, ModelMap mmap) {
        mmap.put("groupId", groupId);
        // 返回im/group/groupLog页面,而不是im/groupLog/logByGroup
        return "im/group/groupLog";
    }

    /**
     * 删除群组管理日志
     */
    @RequiresPermissions("im:group:log:remove")
    @Log(title = "群组管理日志", businessType = BusinessType.DELETE)
    @PostMapping("/remove/{ids}")
    @ResponseBody
    public AjaxResult remove(@PathVariable String ids) {
        return toAjax(imGroupLogService.deleteImGroupLogByIds(Convert.toLongArray(ids)));
    }

    /**
     * 清空指定群组的日志
     */
    @RequiresPermissions("im:group:log:remove")
    @Log(title = "清空群组日志", businessType = BusinessType.DELETE)
    @DeleteMapping("/group/{groupId}")
    @ResponseBody
    public AjaxResult clearLogsByGroupId(@PathVariable("groupId") Long groupId) {
        return toAjax(imGroupLogService.deleteImGroupLogByIds(new Long[]{groupId}));
    }

    /**
     * 获取群组日志统计数据
     */
    @RequiresPermissions("im:group:log:query")
    @GetMapping("/statistics/{groupId}")
    @ResponseBody
    public AjaxResult getStatistics(@PathVariable("groupId") Long groupId) {
        return AjaxResult.success(imGroupLogService.getLogStatistics(groupId));
    }
}
