package com.ruoyi.web.controller.im;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.web.domain.ImUser;
import com.ruoyi.web.service.ImUserService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.List;

/**
 * IM用户管理控制器（管理后台）
 *
 * @author ruoyi
 * @date 2025-01-07
 */
@Controller
@RequestMapping("/im/user")
public class ImUserController extends BaseController {

    private String prefix = "im/user";

    /**
     * 用户管理页面
     */
    @RequiresPermissions("im:user:list")
    @GetMapping()
    public String user() {
        return prefix + "/user";
    }

    @Autowired
    private ImUserService imUserService;

    /**
     * 新增用户页面
     */
    @RequiresPermissions("im:user:add")
    @GetMapping("/add")
    public String add() {
        return prefix + "/add";
    }

    /**
     * 修改用户页面
     */
    @RequiresPermissions("im:user:edit")
    @GetMapping("/edit/{userId}")
    public String edit(@PathVariable("userId") Long userId, org.springframework.ui.ModelMap mmap) {
        mmap.put("user", imUserService.selectImUserById(userId));
        return prefix + "/edit";
    }

    /**
     * 查询IM用户列表
     */
    @RequiresPermissions("im:user:list")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(ImUser imUser) {
        startPage();
        List<ImUser> list = imUserService.selectImUserList(imUser);
        return getDataTable(list);
    }

    /**
     * 获取用户统计数据
     */
    @RequiresPermissions("im:user:query")
    @GetMapping("/statistics")
    @ResponseBody
    public AjaxResult getStatistics() {
        return AjaxResult.success(imUserService.getUserStatistics());
    }

    /**
     * 获取IM用户详细信息
     */
    @RequiresPermissions("im:user:query")
    @GetMapping("/info/{id}")
    @ResponseBody
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return AjaxResult.success(imUserService.selectImUserById(id));
    }

    /**
     * 新增IM用户
     */
    @RequiresPermissions("im:user:add")
    @Log(title = "IM用户", businessType = BusinessType.INSERT)
    @PostMapping
    @ResponseBody
    public AjaxResult add(@Valid @RequestBody ImUser imUser) {
        return toAjax(imUserService.insertImUser(imUser));
    }

    /**
     * 修改IM用户
     */
    @RequiresPermissions("im:user:edit")
    @Log(title = "IM用户", businessType = BusinessType.UPDATE)
    @PutMapping
    @ResponseBody
    public AjaxResult edit(@Valid @RequestBody ImUser imUser) {
        return toAjax(imUserService.updateImUser(imUser));
    }

    /**
     * 删除IM用户
     */
    @RequiresPermissions("im:user:remove")
    @Log(title = "IM用户", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    @ResponseBody
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(imUserService.deleteImUserByIds(ids));
    }

    /**
     * 导出IM用户列表
     */
    @RequiresPermissions("im:user:export")
    @Log(title = "IM用户", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, ImUser imUser) {
        List<ImUser> list = imUserService.selectImUserList(imUser);
        // 导出逻辑
    }

    /**
     * 重置用户密码
     */
    @RequiresPermissions("im:user:edit")
    @Log(title = "重置用户密码", businessType = BusinessType.UPDATE)
    @PutMapping("/{id}/reset-password")
    @ResponseBody
    public AjaxResult resetPassword(@PathVariable("id") Long id) {
        return toAjax(imUserService.resetPassword(id, "123456"));
    }

    /**
     * 启用/禁用用户
     */
    @RequiresPermissions("im:user:edit")
    @Log(title = "修改用户状态", businessType = BusinessType.UPDATE)
    @PutMapping("/{id}/status")
    @ResponseBody
    public AjaxResult changeStatus(@PathVariable("id") Long id, @RequestParam String status) {
        return toAjax(imUserService.changeStatus(id, status));
    }

    /**
     * 获取用户在线状态
     */
    @RequiresPermissions("im:user:query")
    @GetMapping("/online-status")
    @ResponseBody
    public AjaxResult getOnlineStatus() {
        return AjaxResult.success(imUserService.countOnlineUsers());
    }

    /**
     * 搜索用户
     */
    @RequiresPermissions("im:user:list")
    @GetMapping("/search")
    @ResponseBody
    public AjaxResult searchUsers(@RequestParam String keyword) {
        List<ImUser> list = imUserService.searchUsers(keyword);
        return AjaxResult.success(list);
    }
}
