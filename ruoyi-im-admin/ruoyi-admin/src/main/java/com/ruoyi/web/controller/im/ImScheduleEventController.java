package com.ruoyi.web.controller.im;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.core.text.Convert;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.web.domain.ImScheduleEvent;
import com.ruoyi.web.service.ImScheduleEventService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 日程事件管理控制器
 *
 * @author ruoyi
 */
@Controller
@RequestMapping("/im/scheduleEvent")
public class ImScheduleEventController extends BaseController {

    private String prefix = "im/scheduleEvent";

    @Autowired
    private ImScheduleEventService imScheduleEventService;

    @RequiresPermissions("im:scheduleEvent:view")
    @GetMapping()
    public String scheduleEvent() {
        return prefix + "/scheduleEvent";
    }

    /**
     * 查询日程事件列表
     */
    @RequiresPermissions("im:scheduleEvent:list")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(ImScheduleEvent imScheduleEvent) {
        startPage();
        List<ImScheduleEvent> list = imScheduleEventService.selectImScheduleEventList(imScheduleEvent);
        return getDataTable(list);
    }

    /**
     * 获取日程统计数据
     */
    @RequiresPermissions("im:scheduleEvent:query")
    @GetMapping("/statistics")
    @ResponseBody
    public AjaxResult getStatistics() {
        return AjaxResult.success(imScheduleEventService.getEventStatistics());
    }

    /**
     * 获取日程事件详细信息
     */
    @RequiresPermissions("im:scheduleEvent:query")
    @GetMapping("/info/{id}")
    @ResponseBody
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return AjaxResult.success(imScheduleEventService.selectImScheduleEventById(id));
    }

    /**
     * 获取用户的日程事件
     */
    @RequiresPermissions("im:scheduleEvent:query")
    @GetMapping("/user/{userId}")
    @ResponseBody
    public AjaxResult getUserEvents(@PathVariable("userId") Long userId) {
        return AjaxResult.success(imScheduleEventService.selectEventsByUserId(userId));
    }

/**
     * 新增日程事件
     */
    @RequiresPermissions("im:scheduleEvent:add")
    @Log(title = "日程事件", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    @ResponseBody
    public AjaxResult add(@RequestBody ImScheduleEvent imScheduleEvent) {
        return toAjax(imScheduleEventService.insertImScheduleEvent(imScheduleEvent));
    }

    /**
     * 修改日程事件（表单提交方式，用于编辑页面）
     */
    @RequiresPermissions("im:scheduleEvent:edit")
    @Log(title = "日程事件管理", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    @ResponseBody
    public AjaxResult edit(ImScheduleEvent imScheduleEvent) {
        return toAjax(imScheduleEventService.updateImScheduleEvent(imScheduleEvent));
    }

    /**
     * 取消日程
     */
    @RequiresPermissions("im:scheduleEvent:edit")
    @Log(title = "日程事件管理", businessType = BusinessType.UPDATE)
    @PutMapping("/cancel/{id}")
    @ResponseBody
    public AjaxResult cancel(@PathVariable("id") Long id) {
        return toAjax(imScheduleEventService.cancelEvent(id));
    }

    /**
     * 完成日程
     */
    @RequiresPermissions("im:scheduleEvent:edit")
    @Log(title = "日程事件管理", businessType = BusinessType.UPDATE)
    @PutMapping("/complete/{id}")
    @ResponseBody
    public AjaxResult complete(@PathVariable("id") Long id) {
        return toAjax(imScheduleEventService.completeEvent(id));
    }

    /**
     * 删除日程事件
     */
    @RequiresPermissions("im:scheduleEvent:remove")
    @Log(title = "日程事件管理", businessType = BusinessType.DELETE)
    @PostMapping("/remove/{ids}")
    @ResponseBody
    public AjaxResult remove(@PathVariable String ids) {
        return toAjax(imScheduleEventService.deleteImScheduleEventByIds(Convert.toLongArray(ids)));
    }

    /**
     * 导出日程事件列表
     */
    @RequiresPermissions("im:scheduleEvent:export")
    @Log(title = "日程事件管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    @ResponseBody
    public void export(HttpServletResponse response, ImScheduleEvent imScheduleEvent) {
        List<ImScheduleEvent> list = imScheduleEventService.selectImScheduleEventList(imScheduleEvent);
        ExcelUtil<ImScheduleEvent> util = new ExcelUtil<>(ImScheduleEvent.class);
        util.exportExcel(response, list, "日程事件数据");
    }
}
