package com.ruoyi.web.controller.im;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.web.domain.dto.ImUserUpdateDTO;
import com.ruoyi.web.domain.vo.ImUserEnhancedBatchOperationResultVO;
import com.ruoyi.web.service.IImUserService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * IM用户批量操作控制器
 * 
 * @author ruoyi
 * @version 1.0
 * @since 2025-01-22
 */
@Controller
@RequestMapping("/im/user/batch")
public class ImUserBatchController extends BaseController {

    @Autowired
    private IImUserService imUserService;

    @Log(title = "IM用户批量状态更新", businessType = BusinessType.UPDATE)
    @RequiresPermissions("im:user:edit")
    @PostMapping("/updateStatus")
    @ResponseBody
    public AjaxResult batchUpdateStatus(@RequestParam String userIds, 
                                     @RequestParam Integer status) {
        String[] idArray = userIds.split(",");
        ImUserUpdateDTO updateDTO = new ImUserUpdateDTO();
        updateDTO.setStatus(status);
        
        ImUserEnhancedBatchOperationResultVO result = 
                imUserService.batchUpdateUsers(idArray, updateDTO);
        
        if (result.getFailureCount() == 0) {
            return AjaxResult.success("批量更新成功，共处理 " + result.getTotalCount() + " 个用户");
        } else {
            String message = String.format("批量更新完成，成功 %d 个，失败 %d 个。失败详情：%s", 
                    result.getSuccessCount(), result.getFailureCount(), result.getFailureDetails());
            return AjaxResult.error(message);
        }
    }

    @Log(title = "IM用户批量删除", businessType = BusinessType.DELETE)
    @RequiresPermissions("im:user:remove")
    @PostMapping("/remove")
    @ResponseBody
    public AjaxResult batchRemove(@RequestParam String userIds) {
        String[] idArray = userIds.split(",");
        
        ImUserEnhancedBatchOperationResultVO result = 
                imUserService.batchDeleteUsers(idArray);
        
        if (result.getFailureCount() == 0) {
            return AjaxResult.success("批量删除成功，共删除 " + result.getTotalCount() + " 个用户");
        } else {
            String message = String.format("批量删除完成，成功 %d 个，失败 %d 个。失败详情：%s", 
                    result.getSuccessCount(), result.getFailureCount(), result.getFailureDetails());
            return AjaxResult.error(message);
        }
    }

    @Log(title = "IM用户批量重置密码", businessType = BusinessType.UPDATE)
    @RequiresPermissions("im:user:resetPassword")
    @PostMapping("/resetPassword")
    @ResponseBody
    public AjaxResult batchResetPassword(@RequestParam String userIds, 
                                      @RequestParam String newPassword) {
        String[] idArray = userIds.split(",");
        
        ImUserEnhancedBatchOperationResultVO result = 
                imUserService.batchResetPasswords(idArray, newPassword);
        
        if (result.getFailureCount() == 0) {
            return AjaxResult.success("批量重置密码成功，共处理 " + result.getTotalCount() + " 个用户");
        } else {
            String message = String.format("批量重置密码完成，成功 %d 个，失败 %d 个。失败详情：%s", 
                    result.getSuccessCount(), result.getFailureCount(), result.getFailureDetails());
            return AjaxResult.error(message);
        }
    }
}