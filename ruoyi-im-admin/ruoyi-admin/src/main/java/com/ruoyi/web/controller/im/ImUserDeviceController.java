package com.ruoyi.web.controller.im;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.web.domain.ImUserDevice;
import com.ruoyi.web.service.ImUserDeviceService;
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
    @Log(title = "用户设备管理", businessType = BusinessType.INSERT)
    @PostMapping
    @ResponseBody
    public AjaxResult add(@RequestBody ImUserDevice imUserDevice) {
        return toAjax(imUserDeviceService.insertImUserDevice(imUserDevice));
    }

    /**
     * 修改用户设备
     */
    @RequiresPermissions("im:userDevice:edit")
    @Log(title = "用户设备管理", businessType = BusinessType.UPDATE)
    @PutMapping
    @ResponseBody
    public AjaxResult edit(@RequestBody ImUserDevice imUserDevice) {
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
