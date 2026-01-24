package com.ruoyi.project.im.controller.im;

import com.ruoyi.framework.aspectj.lang.annotation.Log;
import com.ruoyi.framework.web.controller.BaseController;
import com.ruoyi.framework.web.domain.AjaxResult;
import com.ruoyi.framework.web.page.TableDataInfo;
import com.ruoyi.framework.aspectj.lang.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.project.im.domain.ImUserDevice;
import com.ruoyi.project.im.service.ImUserDeviceService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 用户设备管理控制器
 *
 * @author ruoyi
 * @date 2025-01-18
 */
@Controller
@RequestMapping("/im/userDevice")
public class ImUserDeviceController extends BaseController {

    private String prefix = "im/userDevice";

    @Autowired
    private ImUserDeviceService imUserDeviceService;

    @RequiresPermissions("im:userDevice:view")
    @GetMapping()
    public String userDevice() {
        return prefix + "/userDevice";
    }

    /**
     * 新增用户设备页面
     */
    @RequiresPermissions("im:userDevice:add")
    @GetMapping("/add")
    public String add() {
        return prefix + "/add";
    }

    /**
     * 修改用户设备页面
     */
    @RequiresPermissions("im:userDevice:edit")
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") Long id, ModelMap mmap) {
        mmap.put("userDevice", imUserDeviceService.selectImUserDeviceById(id));
        return prefix + "/edit";
    }

    /**
     * 查询用户设备列表
     */
    @RequiresPermissions("im:userDevice:list")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(ImUserDevice imUserDevice) {
        startPage();
        List<ImUserDevice> list = imUserDeviceService.selectImUserDeviceList(imUserDevice);
        return getDataTable(list);
    }

    /**
     * 获取设备统计数据
     */
    @RequiresPermissions("im:userDevice:query")
    @GetMapping("/statistics")
    @ResponseBody
    public AjaxResult getStatistics() {
        return AjaxResult.success(imUserDeviceService.getDeviceStatistics());
    }

    /**
     * 获取用户设备详细信息
     */
    @RequiresPermissions("im:userDevice:query")
    @GetMapping("/info/{id}")
    @ResponseBody
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return AjaxResult.success(imUserDeviceService.selectImUserDeviceById(id));
    }

    /**
     * 新增用户设备
     */
    @RequiresPermissions("im:userDevice:add")
    @Log(title = "用户设备", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    @ResponseBody
    public AjaxResult add(ImUserDevice imUserDevice) {
        return toAjax(imUserDeviceService.insertImUserDevice(imUserDevice));
    }

    /**
     * 修改用户设备（表单提交方式，用于编辑页面）
     */
    @RequiresPermissions("im:userDevice:edit")
    @Log(title = "用户设备管理", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    @ResponseBody
    public AjaxResult edit(ImUserDevice imUserDevice) {
        return toAjax(imUserDeviceService.updateImUserDevice(imUserDevice));
    }

    /**
     * 强制下线设备
     */
    @RequiresPermissions("im:userDevice:edit")
    @Log(title = "用户设备管理", businessType = BusinessType.UPDATE)
    @PutMapping("/offline/{id}")
    @ResponseBody
    public AjaxResult forceOffline(@PathVariable("id") Long id) {
        return toAjax(imUserDeviceService.forceOffline(id));
    }

    /**
     * 更新设备状态
     */
    @RequiresPermissions("im:userDevice:edit")
    @Log(title = "用户设备管理", businessType = BusinessType.UPDATE)
    @PutMapping("/status")
    @ResponseBody
    public AjaxResult updateStatus(@RequestParam Long id, @RequestParam String status) {
        return toAjax(imUserDeviceService.updateDeviceStatus(id, status));
    }

    /**
     * 删除用户设备
     */
    @RequiresPermissions("im:userDevice:remove")
    @Log(title = "用户设备管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    @ResponseBody
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(imUserDeviceService.deleteImUserDeviceByIds(ids));
    }

    /**
     * 清理离线设备
     */
    @RequiresPermissions("im:userDevice:remove")
    @Log(title = "用户设备管理", businessType = BusinessType.CLEAN)
    @PostMapping("/clean")
    @ResponseBody
    public AjaxResult clean(@RequestParam(defaultValue = "90") int days) {
        int count = imUserDeviceService.cleanOfflineDevices(days);
        return AjaxResult.success("成功清理 " + count + " 个离线设备");
    }

    /**
     * 导出用户设备列表
     */
    @RequiresPermissions("im:userDevice:export")
    @Log(title = "用户设备管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    @ResponseBody
    public void export(HttpServletResponse response, ImUserDevice imUserDevice) {
        List<ImUserDevice> list = imUserDeviceService.selectImUserDeviceList(imUserDevice);
        ExcelUtil<ImUserDevice> util = new ExcelUtil<>(ImUserDevice.class);
        util.exportExcel(response, list, "用户设备数据");
    }
}
