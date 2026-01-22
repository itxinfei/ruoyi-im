package com.ruoyi.web.controller.im;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.web.domain.dto.PasswordResetDTO;
import com.ruoyi.web.service.IImUserService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * IM用户生命周期管理控制器
 * 
 * @author ruoyi
 * @version 1.0
 * @since 2025-01-22
 */
@Controller
@RequestMapping("/im/user/lifecycle")
public class ImUserLifecycleController extends BaseController {

    @Autowired
    private IImUserService imUserService;

    @Log(title = "重置用户密码", businessType = BusinessType.UPDATE)
    @RequiresPermissions("im:user:resetPassword")
    @PostMapping("/resetPassword")
    @ResponseBody
    public AjaxResult resetPassword(@Validated @RequestBody PasswordResetDTO dto) {
        if (!dto.isPasswordMatch()) {
            return AjaxResult.error("两次输入的密码不一致");
        }
        
        try {
            int result = imUserService.resetPassword(dto.getUserId(), dto.getNewPassword());
            if (result > 0) {
                return AjaxResult.success("密码重置成功");
            } else {
                return AjaxResult.error("用户不存在");
            }
        } catch (Exception e) {
            return AjaxResult.error("密码重置失败：" + e.getMessage());
        }
    }

    @Log(title = "用户状态切换", businessType = BusinessType.UPDATE)
    @RequiresPermissions("im:user:edit")
    @PostMapping("/{id}/status/{status}")
    @ResponseBody
    public AjaxResult toggleUserStatus(@PathVariable Long id, @PathVariable Integer status) {
        try {
            int result = imUserService.updateUserStatus(id, status);
            if (result > 0) {
                String statusText = status == 0 ? "启用" : "禁用";
                return AjaxResult.success("用户" + statusText + "成功");
            } else {
                return AjaxResult.error("用户不存在");
            }
        } catch (Exception e) {
            return AjaxResult.error("状态切换失败：" + e.getMessage());
        }
    }

    @Log(title = "强制用户下线", businessType = BusinessType.UPDATE)
    @RequiresPermissions("im:user:forceLogout")
    @PostMapping("/{id}/forceLogout")
    @ResponseBody
    public AjaxResult forceLogout(@PathVariable Long id) {
        try {
            boolean result = imUserService.forceUserLogout(id);
            if (result) {
                return AjaxResult.success("用户已强制下线");
            } else {
                return AjaxResult.error("用户不存在或已下线");
            }
        } catch (Exception e) {
            return AjaxResult.error("强制下线失败：" + e.getMessage());
        }
    }

    @Log(title = "清理用户缓存", businessType = BusinessType.UPDATE)
    @RequiresPermissions("im:user:clearCache")
    @PostMapping("/{id}/clearCache")
    @ResponseBody
    public AjaxResult clearUserCache(@PathVariable Long id) {
        try {
            boolean result = imUserService.clearUserCache(id);
            if (result) {
                return AjaxResult.success("用户缓存清理成功");
            } else {
                return AjaxResult.error("用户不存在");
            }
        } catch (Exception e) {
            return AjaxResult.error("缓存清理失败：" + e.getMessage());
        }
    }

    @Log(title = "同步用户信息", businessType = BusinessType.UPDATE)
    @RequiresPermissions("im:user:sync")
    @PostMapping("/{id}/sync")
    @ResponseBody
    public AjaxResult syncUserInfo(@PathVariable Long id) {
        try {
            boolean result = imUserService.syncUserInfo(id);
            if (result) {
                return AjaxResult.success("用户信息同步成功");
            } else {
                return AjaxResult.error("用户不存在或同步失败");
            }
        } catch (Exception e) {
            return AjaxResult.error("信息同步失败：" + e.getMessage());
        }
    }
}