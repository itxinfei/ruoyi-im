package com.ruoyi.web.controller.im;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.im.domain.ImUser;
import com.ruoyi.web.service.ImUserService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
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
@RestController
@RequestMapping("api/admin/im/user")
public class ImUserController extends BaseController {

    @Autowired
    private ImUserService imUserService;

    /**
     * 查询IM用户列表
     */
    @RequiresPermissions("im:user:list")
    @GetMapping("/list")
    public AjaxResult list(ImUser imUser) {
        startPage();
        List<ImUser> list = imUserService.selectImUserList(imUser);
        return AjaxResult.success(list);
    }

    /**
     * 获取IM用户详细信息
     */
    @RequiresPermissions("im:user:query")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return AjaxResult.success(imUserService.selectImUserById(id));
    }

    /**
     * 新增IM用户
     */
    @RequiresPermissions("im:user:add")
    @Log(title = "IM用户", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Valid @RequestBody ImUser imUser) {
        return toAjax(imUserService.insertImUser(imUser));
    }

    /**
     * 修改IM用户
     */
    @RequiresPermissions("im:user:edit")
    @Log(title = "IM用户", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Valid @RequestBody ImUser imUser) {
        return toAjax(imUserService.updateImUser(imUser));
    }

    /**
     * 删除IM用户
     */
    @RequiresPermissions("im:user:remove")
    @Log(title = "IM用户", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
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
    public AjaxResult resetPassword(@PathVariable("id") Long id) {
        return toAjax(imUserService.resetPassword(id, "123456"));
    }

    /**
     * 启用/禁用用户
     */
    @RequiresPermissions("im:user:edit")
    @Log(title = "修改用户状态", businessType = BusinessType.UPDATE)
    @PutMapping("/{id}/status")
    public AjaxResult changeStatus(@PathVariable("id") Long id, @RequestParam String status) {
        return toAjax(imUserService.changeStatus(id, status));
    }

    /**
     * 获取用户在线状态
     */
    @RequiresPermissions("im:user:query")
    @GetMapping("/online-status")
    public AjaxResult getOnlineStatus() {
        return AjaxResult.success(imUserService.countOnlineUsers());
    }
}
