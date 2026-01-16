package com.ruoyi.web.controller.im;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.core.text.Convert;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.web.domain.ImGroup;
import com.ruoyi.web.service.ImGroupService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * IM群组管理控制器
 *
 * @author ruoyi
 * @date 2025-01-07
 */
@Controller
@RequestMapping("/im/group")
public class ImGroupController extends BaseController {

    private String prefix = "im/group";

    /**
     * 群组管理页面
     */
    @RequiresPermissions("im:group:view")
    @GetMapping()
    public String group() {
        return prefix + "/group";
    }

    @Autowired
    private ImGroupService imGroupService;

    /**
     * 新增群组页面
     */
    @RequiresPermissions("im:group:add")
    @GetMapping("/add")
    public String add() {
        return prefix + "/add";
    }

    /**
     * 修改群组页面
     */
    @RequiresPermissions("im:group:edit")
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") Long id, org.springframework.ui.ModelMap mmap) {
        mmap.put("group", imGroupService.selectImGroupById(id));
        return prefix + "/edit";
    }

    /**
     * 群组设置页面
     */
    @RequiresPermissions("im:group:edit")
    @GetMapping("/settings/{id}")
    public String settings(@PathVariable("id") Long id, org.springframework.ui.ModelMap mmap) {
        mmap.put("group", imGroupService.selectImGroupById(id));
        return prefix + "/settings";
    }

    /**
     * 查询IM群组列表
     */
    @RequiresPermissions("im:group:list")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(ImGroup imGroup) {
        startPage();
        List<ImGroup> list = imGroupService.selectImGroupList(imGroup);
        return getDataTable(list);
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
     * 获取群组统计数据
     */
    @RequiresPermissions("im:group:query")
    @GetMapping("/statistics")
    @ResponseBody
    public AjaxResult getStatistics() {
        return AjaxResult.success(imGroupService.getGroupStatistics());
    }

    /**
     * 获取IM群组详细信息
     */
    @RequiresPermissions("im:group:query")
    @GetMapping("/info/{id}")
    @ResponseBody
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return AjaxResult.success(imGroupService.selectImGroupById(id));
    }

    /**
     * 新增IM群组
     */
    @RequiresPermissions("im:group:add")
    @Log(title = "IM群组", businessType = BusinessType.INSERT)
    @PostMapping
    @ResponseBody
    public AjaxResult add(@RequestBody ImGroup imGroup) {
        return toAjax(imGroupService.insertImGroup(imGroup));
    }

    /**
     * 修改IM群组（表单提交方式，用于编辑页面）
     */
    @RequiresPermissions("im:group:edit")
    @Log(title = "IM群组", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    @ResponseBody
    public AjaxResult edit(@RequestBody ImGroup imGroup) {
        return toAjax(imGroupService.updateImGroup(imGroup));
    }

    /**
     * 删除IM群组
     */
    @RequiresPermissions("im:group:remove")
    @Log(title = "IM群组", businessType = BusinessType.DELETE)
    @PostMapping("/remove/{ids}")
    @ResponseBody
    public AjaxResult remove(@PathVariable String ids) {
        return toAjax(imGroupService.deleteImGroupByIds(Convert.toLongArray(ids)));
    }

    /**
     * 获取群组成员列表
     */
    @RequiresPermissions("im:group:query")
    @GetMapping("/{id}/members")
    @ResponseBody
    public AjaxResult getMembers(@PathVariable("id") Long groupId) {
        return AjaxResult.success(imGroupService.selectGroupMembersByGroupId(groupId));
    }

    /**
     * 解散群组
     */
    @RequiresPermissions("im:group:edit")
    @Log(title = "解散群组", businessType = BusinessType.DELETE)
    @DeleteMapping("/{id}/dismiss")
    @ResponseBody
    public AjaxResult dismiss(@PathVariable("id") Long groupId) {
        return toAjax(imGroupService.dismissGroup(groupId));
    }
}
