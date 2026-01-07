package com.ruoyi.web.controller.im;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.im.domain.ImApplication;
import com.ruoyi.web.service.ImApplicationService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * IM应用管理控制器（管理后台）
 *
 * @author ruoyi
 * @date 2025-01-07
 */
@RestController
@RequestMapping("api/admin/im/application")
public class ImAppAdminController extends BaseController {

    @Autowired
    private ImApplicationService applicationService;

    /**
     * 获取应用列表
     */
    @RequiresPermissions("im:app:list")
    @GetMapping("/list")
    public AjaxResult getApplications(@RequestParam(required = false) String category) {
        List<ImApplication> list = applicationService.getApplications(category);
        return AjaxResult.success(list);
    }

    /**
     * 按分类获取应用
     */
    @RequiresPermissions("im:app:list")
    @GetMapping("/category")
    public AjaxResult getApplicationsByCategory(@RequestParam String category) {
        Map<String, List<ImApplication>> map = applicationService.getApplicationsByCategory();
        return AjaxResult.success(map);
    }

    /**
     * 获取应用详情
     */
    @RequiresPermissions("im:app:query")
    @GetMapping("/{id}")
    public AjaxResult getApplicationById(@PathVariable Long id) {
        ImApplication app = applicationService.getApplicationById(id);
        return AjaxResult.success(app);
    }

    /**
     * 创建应用
     */
    @RequiresPermissions("im:app:add")
    @Log(title = "创建应用", businessType = BusinessType.INSERT)
    @PostMapping("/create")
    public AjaxResult createApplication(@RequestParam String name,
                                    @RequestParam String code,
                                    @RequestParam String category,
                                    @RequestParam String appType,
                                    @RequestParam String appUrl,
                                    @RequestParam(required = false) String icon) {
        Long appId = applicationService.createApplication(name, code, category, appType, appUrl, icon);
        return AjaxResult.success("创建成功", appId);
    }

    /**
     * 更新应用
     */
    @RequiresPermissions("im:app:edit")
    @Log(title = "更新应用", businessType = BusinessType.UPDATE)
    @PutMapping("/{id}")
    public AjaxResult updateApplication(@PathVariable Long id,
                                    @RequestParam String name,
                                    @RequestParam(required = false) String description,
                                    @RequestParam(required = false) String icon) {
        applicationService.updateApplication(id, name, description, icon);
        return AjaxResult.success("更新成功");
    }

    /**
     * 删除应用
     */
    @RequiresPermissions("im:app:remove")
    @Log(title = "删除应用", businessType = BusinessType.DELETE)
    @DeleteMapping("/{id}")
    public AjaxResult deleteApplication(@PathVariable Long id) {
        applicationService.deleteApplication(id);
        return AjaxResult.success("删除成功");
    }

    /**
     * 设置应用可见性
     */
    @RequiresPermissions("im:app:edit")
    @Log(title = "设置应用可见性", businessType = BusinessType.UPDATE)
    @PutMapping("/{id}/visibility")
    public AjaxResult setVisibility(@PathVariable Long id,
                                @RequestParam Boolean isVisible) {
        applicationService.setVisibility(id, isVisible);
        return AjaxResult.success("设置成功");
    }

    /**
     * 批量设置应用可见性
     */
    @RequiresPermissions("im:app:edit")
    @Log(title = "批量设置应用可见性", businessType = BusinessType.UPDATE)
    @PutMapping("/batch-visibility")
    public AjaxResult batchSetVisibility(@RequestParam Long[] ids,
                                     @RequestParam Boolean isVisible) {
        for (Long id : ids) {
            applicationService.setVisibility(id, isVisible);
        }
        return AjaxResult.success("批量设置成功");
    }

    /**
     * 获取应用统计
     */
    @RequiresPermissions("im:app:list")
    @GetMapping("/statistics")
    public AjaxResult getStatistics() {
        Map<String, Object> stats = applicationService.getApplicationStatistics();
        return AjaxResult.success(stats);
    }
}
