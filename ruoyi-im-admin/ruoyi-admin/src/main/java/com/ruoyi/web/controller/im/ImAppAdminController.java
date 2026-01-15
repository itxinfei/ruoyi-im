package com.ruoyi.web.controller.im;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.web.domain.ImApplication;
import com.ruoyi.web.service.ImApplicationService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * IM应用管理控制器
 *
 * @author ruoyi
 * @date 2025-01-07
 */
@Controller
@RequestMapping("/im/application")
public class ImAppAdminController extends BaseController {

    private String prefix = "im/application";

    @Autowired
    private ImApplicationService applicationService;

    /**
     * 应用管理页面
     */
    @RequiresPermissions("im:app:view")
    @GetMapping()
    public String application() {
        return prefix + "/application";
    }

    /**
     * 查询应用列表
     */
    @RequiresPermissions("im:app:list")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(ImApplication imApplication) {
        startPage();
        List<ImApplication> list = applicationService.selectImApplicationList(imApplication);
        return getDataTable(list);
    }

    /**
     * 导出应用列表
     */
    @RequiresPermissions("im:app:export")
    @Log(title = "应用管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    @ResponseBody
    public void export(HttpServletResponse response, ImApplication imApplication) {
        List<ImApplication> list = applicationService.selectImApplicationList(imApplication);
        ExcelUtil<ImApplication> util = new ExcelUtil<>(ImApplication.class);
        util.exportExcel(response, list, "应用数据");
    }

    /**
     * 获取应用详细信息
     */
    @RequiresPermissions("im:app:query")
    @GetMapping("/{id}")
    @ResponseBody
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return AjaxResult.success(applicationService.selectImApplicationById(id));
    }

    /**
     * 新增应用
     */
    @RequiresPermissions("im:app:add")
    @Log(title = "应用管理", businessType = BusinessType.INSERT)
    @PostMapping
    @ResponseBody
    public AjaxResult add(@RequestBody ImApplication imApplication) {
        return toAjax(applicationService.insertImApplication(imApplication));
    }

    /**
     * 修改应用
     */
    @RequiresPermissions("im:app:edit")
    @Log(title = "应用管理", businessType = BusinessType.UPDATE)
    @PutMapping
    @ResponseBody
    public AjaxResult edit(@RequestBody ImApplication imApplication) {
        return toAjax(applicationService.updateImApplication(imApplication));
    }

    /**
     * 删除应用
     */
    @RequiresPermissions("im:app:remove")
    @Log(title = "应用管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    @ResponseBody
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(applicationService.deleteImApplicationByIds(ids));
    }

    /**
     * 设置应用可见性
     */
    @RequiresPermissions("im:app:edit")
    @Log(title = "设置应用可见性", businessType = BusinessType.UPDATE)
    @PutMapping("/visibility")
    @ResponseBody
    public AjaxResult setVisibility(@RequestParam Long id, @RequestParam Boolean isVisible) {
        applicationService.setVisibility(id, isVisible);
        return AjaxResult.success("设置成功");
    }

    /**
     * 获取应用统计
     */
    @RequiresPermissions("im:app:list")
    @GetMapping("/statistics")
    @ResponseBody
    public AjaxResult getStatistics() {
        return AjaxResult.success(applicationService.getApplicationStatistics());
    }
}
