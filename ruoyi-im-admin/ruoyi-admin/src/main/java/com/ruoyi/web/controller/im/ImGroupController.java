package com.ruoyi.web.controller.im;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.im.domain.ImGroup;
import com.ruoyi.web.service.ImGroupService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * IM群组管理控制器
 *
 * @author ruoyi
 * @date 2025-01-07
 */
@RestController
@RequestMapping("api/admin/im/group")
public class ImGroupController extends BaseController {

    @Autowired
    private ImGroupService imGroupService;

    /**
     * 查询IM群组列表
     */
    @RequiresPermissions("im:group:list")
    @GetMapping("/list")
    public AjaxResult list(ImGroup imGroup) {
        startPage();
        List<ImGroup> list = imGroupService.selectImGroupList(imGroup);
        return AjaxResult.success(list);
    }

    /**
     * 导出IM群组列表
     */
    @RequiresPermissions("im:group:export")
    @Log(title = "IM群组", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, ImGroup imGroup) {
        List<ImGroup> list = imGroupService.selectImGroupList(imGroup);
        // 导出逻辑
    }

    /**
     * 获取IM群组详细信息
     */
    @RequiresPermissions("im:group:query")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return AjaxResult.success(imGroupService.selectImGroupById(id));
    }

    /**
     * 新增IM群组
     */
    @RequiresPermissions("im:group:add")
    @Log(title = "IM群组", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody ImGroup imGroup) {
        return toAjax(imGroupService.insertImGroup(imGroup));
    }

    /**
     * 修改IM群组
     */
    @RequiresPermissions("im:group:edit")
    @Log(title = "IM群组", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody ImGroup imGroup) {
        return toAjax(imGroupService.updateImGroup(imGroup));
    }

    /**
     * 删除IM群组
     */
    @RequiresPermissions("im:group:remove")
    @Log(title = "IM群组", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(imGroupService.deleteImGroupByIds(ids));
    }

    /**
     * 获取群组成员列表
     */
    @RequiresPermissions("im:group:query")
    @GetMapping("/{id}/members")
    public AjaxResult getMembers(@PathVariable("id") Long groupId) {
        return AjaxResult.success(imGroupService.selectGroupMembersByGroupId(groupId));
    }

    /**
     * 解散群组
     */
    @RequiresPermissions("im:group:edit")
    @Log(title = "解散群组", businessType = BusinessType.DELETE)
    @DeleteMapping("/{id}/dismiss")
    public AjaxResult dismiss(@PathVariable("id") Long groupId) {
        return toAjax(imGroupService.dismissGroup(groupId));
    }
}
