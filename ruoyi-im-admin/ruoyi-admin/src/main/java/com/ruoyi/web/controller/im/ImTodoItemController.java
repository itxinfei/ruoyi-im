package com.ruoyi.web.controller.im;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.web.domain.ImTodoItem;
import com.ruoyi.web.service.ImTodoItemService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 待办事项管理控制器
 *
 * @author ruoyi
 */
@Controller
@RequestMapping("/im/todoItem")
public class ImTodoItemController extends BaseController {

    private String prefix = "im/todoItem";

    @Autowired
    private ImTodoItemService imTodoItemService;

    @RequiresPermissions("im:todoItem:view")
    @GetMapping()
    public String todoItem() {
        return prefix + "/todoItem";
    }

    /**
     * 新增待办事项页面
     */
    @RequiresPermissions("im:todoItem:add")
    @GetMapping("/add")
    public String add() {
        return prefix + "/add";
    }

    /**
     * 修改待办事项页面
     */
    @RequiresPermissions("im:todoItem:edit")
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") Long id, ModelMap mmap) {
        mmap.put("todoItem", imTodoItemService.selectImTodoItemById(id));
        return prefix + "/edit";
    }

    /**
     * 查询待办事项列表
     */
    @RequiresPermissions("im:todoItem:list")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(ImTodoItem imTodoItem) {
        startPage();
        List<ImTodoItem> list = imTodoItemService.selectImTodoItemList(imTodoItem);
        return getDataTable(list);
    }

    /**
     * 获取待办统计数据
     */
    @RequiresPermissions("im:todoItem:query")
    @GetMapping("/statistics")
    @ResponseBody
    public AjaxResult getStatistics() {
        return AjaxResult.success(imTodoItemService.getTodoStatistics());
    }

    /**
     * 获取逾期待办
     */
    @RequiresPermissions("im:todoItem:query")
    @GetMapping("/overdue")
    @ResponseBody
    public AjaxResult getOverdueItems() {
        return AjaxResult.success(imTodoItemService.selectOverdueItems());
    }

    /**
     * 获取今日到期待办
     */
    @RequiresPermissions("im:todoItem:query")
    @GetMapping("/todayDue")
    @ResponseBody
    public AjaxResult getTodayDueItems() {
        return AjaxResult.success(imTodoItemService.selectTodayDueItems());
    }

    /**
     * 获取待办事项详细信息
     */
    @RequiresPermissions("im:todoItem:query")
    @GetMapping("/info/{id}")
    @ResponseBody
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return AjaxResult.success(imTodoItemService.selectImTodoItemById(id));
    }

    /**
     * 新增待办事项
     */
    @RequiresPermissions("im:todoItem:add")
    @Log(title = "待办事项管理", businessType = BusinessType.INSERT)
    @PostMapping
    @ResponseBody
    public AjaxResult add(@RequestBody ImTodoItem imTodoItem) {
        return toAjax(imTodoItemService.insertImTodoItem(imTodoItem));
    }

    /**
     * 修改待办事项
     */
    @RequiresPermissions("im:todoItem:edit")
    @Log(title = "待办事项管理", businessType = BusinessType.UPDATE)
    @PutMapping
    @ResponseBody
    public AjaxResult edit(@RequestBody ImTodoItem imTodoItem) {
        return toAjax(imTodoItemService.updateImTodoItem(imTodoItem));
    }

    /**
     * 开始待办
     */
    @RequiresPermissions("im:todoItem:edit")
    @Log(title = "待办事项管理", businessType = BusinessType.UPDATE)
    @PutMapping("/start/{id}")
    @ResponseBody
    public AjaxResult start(@PathVariable("id") Long id) {
        return toAjax(imTodoItemService.startItem(id));
    }

    /**
     * 完成待办
     */
    @RequiresPermissions("im:todoItem:edit")
    @Log(title = "待办事项管理", businessType = BusinessType.UPDATE)
    @PutMapping("/complete/{id}")
    @ResponseBody
    public AjaxResult complete(@PathVariable("id") Long id) {
        return toAjax(imTodoItemService.completeItem(id));
    }

    /**
     * 取消待办
     */
    @RequiresPermissions("im:todoItem:edit")
    @Log(title = "待办事项管理", businessType = BusinessType.UPDATE)
    @PutMapping("/cancel/{id}")
    @ResponseBody
    public AjaxResult cancel(@PathVariable("id") Long id) {
        return toAjax(imTodoItemService.cancelItem(id));
    }

    /**
     * 删除待办事项
     */
    @RequiresPermissions("im:todoItem:remove")
    @Log(title = "待办事项管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    @ResponseBody
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(imTodoItemService.deleteImTodoItemByIds(ids));
    }

    /**
     * 清理已完成待办
     */
    @RequiresPermissions("im:todoItem:remove")
    @Log(title = "待办事项管理", businessType = BusinessType.CLEAN)
    @PostMapping("/clean")
    @ResponseBody
    public AjaxResult clean(@RequestParam(defaultValue = "30") int days) {
        int count = imTodoItemService.cleanCompletedItems(days);
        return AjaxResult.success("成功清理 " + count + " 条已完成待办");
    }

    /**
     * 导出待办事项列表
     */
    @RequiresPermissions("im:todoItem:export")
    @Log(title = "待办事项管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    @ResponseBody
    public void export(HttpServletResponse response, ImTodoItem imTodoItem) {
        List<ImTodoItem> list = imTodoItemService.selectImTodoItemList(imTodoItem);
        ExcelUtil<ImTodoItem> util = new ExcelUtil<>(ImTodoItem.class);
        util.exportExcel(response, list, "待办事项数据");
    }
}
