package com.ruoyi.web.controller.im;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.web.domain.ImUserSetting;
import com.ruoyi.web.service.ImUserSettingService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 用户设置管理控制器
 *
 * @author ruoyi
 */
@Controller
@RequestMapping("/im/userSetting")
public class ImUserSettingController extends BaseController {

    private String prefix = "im/userSetting";

    @Autowired
    private ImUserSettingService imUserSettingService;

    @RequiresPermissions("im:userSetting:view")
    @GetMapping()
    public String userSetting() {
        return prefix + "/userSetting";
    }

    /**
     * 查询用户设置列表
     */
    @RequiresPermissions("im:userSetting:list")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(ImUserSetting imUserSetting) {
        startPage();
        List<ImUserSetting> list = imUserSettingService.selectImUserSettingList(imUserSetting);
        return getDataTable(list);
    }

    /**
     * 获取设置统计数据
     */
    @RequiresPermissions("im:userSetting:query")
    @GetMapping("/statistics")
    @ResponseBody
    public AjaxResult getStatistics() {
        return AjaxResult.success(imUserSettingService.getSettingStatistics());
    }

    /**
     * 获取用户设置详细信息
     */
    @RequiresPermissions("im:userSetting:query")
    @GetMapping("/info/{id}")
    @ResponseBody
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return AjaxResult.success(imUserSettingService.selectImUserSettingById(id));
    }

    /**
     * 获取用户的所有设置
     */
    @RequiresPermissions("im:userSetting:query")
    @GetMapping("/user/{userId}")
    @ResponseBody
    public AjaxResult getUserSettings(@PathVariable("userId") Long userId) {
        return AjaxResult.success(imUserSettingService.selectSettingsByUserId(userId));
    }

    /**
     * 获取指定设置值
     */
    @RequiresPermissions("im:userSetting:query")
    @GetMapping("/value")
    @ResponseBody
    public AjaxResult getSettingValue(@RequestParam Long userId, @RequestParam String settingKey) {
        String value = imUserSettingService.getSettingValue(userId, settingKey, null);
        return AjaxResult.success(value);
    }

    /**
     * 新增用户设置
     */
    @RequiresPermissions("im:userSetting:add")
    @Log(title = "用户设置", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    @ResponseBody
    public AjaxResult add(@RequestBody ImUserSetting imUserSetting) {
        return toAjax(imUserSettingService.insertImUserSetting(imUserSetting));
    }

    /**
     * 修改用户设置（表单提交方式，用于编辑页面）
     */
    @RequiresPermissions("im:userSetting:edit")
    @Log(title = "用户设置管理", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    @ResponseBody
    public AjaxResult edit(ImUserSetting imUserSetting) {
        return toAjax(imUserSettingService.updateImUserSetting(imUserSetting));
    }

    /**
     * 更新设置值
     */
    @RequiresPermissions("im:userSetting:edit")
    @Log(title = "用户设置管理", businessType = BusinessType.UPDATE)
    @PutMapping("/value")
    @ResponseBody
    public AjaxResult updateSettingValue(@RequestParam Long userId,
                                          @RequestParam String settingKey,
                                          @RequestParam String settingValue) {
        return toAjax(imUserSettingService.updateSettingValue(userId, settingKey, settingValue));
    }

    /**
     * 删除用户设置
     */
    @RequiresPermissions("im:userSetting:remove")
    @Log(title = "用户设置管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    @ResponseBody
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(imUserSettingService.deleteImUserSettingByIds(ids));
    }

    /**
     * 重置用户设置
     */
    @RequiresPermissions("im:userSetting:edit")
    @Log(title = "用户设置管理", businessType = BusinessType.UPDATE)
    @PutMapping("/reset/{userId}")
    @ResponseBody
    public AjaxResult reset(@PathVariable("userId") Long userId) {
        return toAjax(imUserSettingService.resetUserSettings(userId));
    }

    /**
     * 导出用户设置列表
     */
    @RequiresPermissions("im:userSetting:export")
    @Log(title = "用户设置管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    @ResponseBody
    public void export(HttpServletResponse response, ImUserSetting imUserSetting) {
        List<ImUserSetting> list = imUserSettingService.selectImUserSettingList(imUserSetting);
        ExcelUtil<ImUserSetting> util = new ExcelUtil<>(ImUserSetting.class);
        util.exportExcel(response, list, "用户设置数据");
    }
}
