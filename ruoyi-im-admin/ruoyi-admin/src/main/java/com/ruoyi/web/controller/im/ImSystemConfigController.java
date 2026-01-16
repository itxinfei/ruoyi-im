package com.ruoyi.web.controller.im;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.web.domain.ImSystemConfig;
import com.ruoyi.web.service.ImSystemConfigService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 系统配置管理控制器
 *
 * @author ruoyi
 */
@Controller
@RequestMapping("/im/systemConfig")
public class ImSystemConfigController extends BaseController {

    private String prefix = "im/systemConfig";

    @Autowired
    private ImSystemConfigService imSystemConfigService;

    @RequiresPermissions("im:systemConfig:view")
    @GetMapping()
    public String systemConfig() {
        return prefix + "/systemConfig";
    }

    /**
     * 查询系统配置列表
     */
    @RequiresPermissions("im:systemConfig:list")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(ImSystemConfig imSystemConfig) {
        startPage();
        List<ImSystemConfig> list = imSystemConfigService.selectImSystemConfigList(imSystemConfig);
        return getDataTable(list);
    }

    /**
     * 获取系统配置统计数据
     */
    @RequiresPermissions("im:systemConfig:query")
    @GetMapping("/statistics")
    @ResponseBody
    public AjaxResult getStatistics() {
        return AjaxResult.success(imSystemConfigService.getSystemConfigStatistics());
    }

    /**
     * 获取系统配置详细信息
     */
    @RequiresPermissions("im:systemConfig:query")
    @GetMapping("/info/{id}")
    @ResponseBody
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return AjaxResult.success(imSystemConfigService.selectImSystemConfigById(id));
    }

    /**
     * 根据配置键获取配置值
     */
    @RequiresPermissions("im:systemConfig:query")
    @GetMapping("/key/{configKey}")
    @ResponseBody
    public AjaxResult getConfigByKey(@PathVariable("configKey") String configKey) {
        return AjaxResult.success(imSystemConfigService.selectImSystemConfigByKey(configKey));
    }

    /**
     * 新增系统配置
     */
    @RequiresPermissions("im:systemConfig:add")
    @Log(title = "系统配置管理", businessType = BusinessType.INSERT)
    @PostMapping
    @ResponseBody
    public AjaxResult add(@RequestBody ImSystemConfig imSystemConfig) {
        return toAjax(imSystemConfigService.insertImSystemConfig(imSystemConfig));
    }

    /**
     * 修改系统配置（表单提交方式，用于编辑页面）
     */
    @RequiresPermissions("im:systemConfig:edit")
    @Log(title = "系统配置管理", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    @ResponseBody
    public AjaxResult edit(@RequestBody ImSystemConfig imSystemConfig) {
        return toAjax(imSystemConfigService.updateImSystemConfig(imSystemConfig));
    }

    /**
     * 切换配置状态
     */
    @RequiresPermissions("im:systemConfig:edit")
    @Log(title = "系统配置管理", businessType = BusinessType.UPDATE)
    @PutMapping("/toggle/{id}")
    @ResponseBody
    public AjaxResult toggleStatus(@PathVariable("id") Long id) {
        return toAjax(imSystemConfigService.toggleStatus(id));
    }

    /**
     * 删除系统配置
     */
    @RequiresPermissions("im:systemConfig:remove")
    @Log(title = "系统配置管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    @ResponseBody
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(imSystemConfigService.deleteImSystemConfigByIds(ids));
    }

    /**
     * 导出系统配置列表
     */
    @RequiresPermissions("im:systemConfig:export")
    @Log(title = "系统配置管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    @ResponseBody
    public void export(HttpServletResponse response, ImSystemConfig imSystemConfig) {
        List<ImSystemConfig> list = imSystemConfigService.selectImSystemConfigList(imSystemConfig);
        ExcelUtil<ImSystemConfig> util = new ExcelUtil<>(ImSystemConfig.class);
        util.exportExcel(response, list, "系统配置数据");
    }
}
