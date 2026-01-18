package com.ruoyi.web.controller.im;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.core.text.Convert;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.web.domain.ImDepartmentMember;
import com.ruoyi.web.service.ImDepartmentMemberService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 部门成员管理控制器
 *
 * @author ruoyi
 * @date 2025-01-18
 */
@Controller
@RequestMapping("/im/departmentMember")
public class ImDepartmentMemberController extends BaseController {

    private String prefix = "im/departmentMember";

    @Autowired
    private ImDepartmentMemberService imDepartmentMemberService;

    @RequiresPermissions("im:departmentMember:view")
    @GetMapping()
    public String departmentMember() {
        return prefix + "/member";
    }

    /**
     * 查询部门成员关系列表
     */
    @RequiresPermissions("im:departmentMember:list")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(ImDepartmentMember imDepartmentMember) {
        startPage();
        List<ImDepartmentMember> list = imDepartmentMemberService.selectImDepartmentMemberList(imDepartmentMember);
        return getDataTable(list);
    }

    /**
     * 获取部门成员统计数据
     */
    @RequiresPermissions("im:departmentMember:query")
    @GetMapping("/statistics")
    @ResponseBody
    public AjaxResult getStatistics() {
        return AjaxResult.success(imDepartmentMemberService.getDepartmentMemberStatistics());
    }

    /**
     * 导出部门成员关系列表
     */
    @RequiresPermissions("im:departmentMember:export")
    @Log(title = "部门成员管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    @ResponseBody
    public void export(HttpServletResponse response, ImDepartmentMember imDepartmentMember) {
        List<ImDepartmentMember> list = imDepartmentMemberService.selectImDepartmentMemberList(imDepartmentMember);
        ExcelUtil<ImDepartmentMember> util = new ExcelUtil<>(ImDepartmentMember.class);
        util.exportExcel(response, list, "部门成员数据");
    }

    /**
     * 获取部门成员关系详细信息
     */
    @RequiresPermissions("im:departmentMember:query")
    @GetMapping("/{id}")
    @ResponseBody
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return AjaxResult.success(imDepartmentMemberService.selectImDepartmentMemberById(id));
    }

    /**
     * 根据部门ID查询成员列表
     */
    @RequiresPermissions("im:departmentMember:query")
    @GetMapping("/department/{departmentId}")
    @ResponseBody
    public AjaxResult listByDepartment(@PathVariable("departmentId") Long departmentId) {
        List<ImDepartmentMember> list = imDepartmentMemberService.selectMembersByDepartmentId(departmentId);
        return AjaxResult.success(list);
    }

    /**
     * 根据用户ID查询部门列表
     */
    @RequiresPermissions("im:departmentMember:query")
    @GetMapping("/user/{userId}")
    @ResponseBody
    public AjaxResult listByUser(@PathVariable("userId") Long userId) {
        List<ImDepartmentMember> list = imDepartmentMemberService.selectDepartmentsByUserId(userId);
        return AjaxResult.success(list);
    }

    /**
     * 新增部门成员关系
     */
    @RequiresPermissions("im:departmentMember:add")
    @Log(title = "部门成员管理", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    @ResponseBody
    public AjaxResult add(ImDepartmentMember imDepartmentMember) {
        return toAjax(imDepartmentMemberService.insertImDepartmentMember(imDepartmentMember));
    }

    /**
     * 修改部门成员关系
     */
    @RequiresPermissions("im:departmentMember:edit")
    @Log(title = "部门成员管理", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    @ResponseBody
    public AjaxResult edit(ImDepartmentMember imDepartmentMember) {
        return toAjax(imDepartmentMemberService.updateImDepartmentMember(imDepartmentMember));
    }

    /**
     * 设置主部门
     */
    @RequiresPermissions("im:departmentMember:edit")
    @Log(title = "部门成员管理", businessType = BusinessType.UPDATE)
    @PostMapping("/setPrimary")
    @ResponseBody
    public AjaxResult setPrimary(@RequestParam Long userId, @RequestParam Long departmentId) {
        return toAjax(imDepartmentMemberService.setPrimaryDepartment(userId, departmentId));
    }

    /**
     * 删除部门成员关系
     */
    @RequiresPermissions("im:departmentMember:remove")
    @Log(title = "部门成员管理", businessType = BusinessType.DELETE)
    @PostMapping("/remove/{ids}")
    @ResponseBody
    public AjaxResult remove(@PathVariable String ids) {
        return toAjax(imDepartmentMemberService.deleteImDepartmentMemberByIds(Convert.toLongArray(ids)));
    }
}
