package com.ruoyi.web.controller.im;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.web.domain.ImDepartment;
import com.ruoyi.web.service.ImDepartmentService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 部门管理控制器
 *
 * @author ruoyi
 */
@Controller
@RequestMapping("/im/department")
public class ImDepartmentController extends BaseController {

    private String prefix = "im/department";

    @Autowired
    private ImDepartmentService imDepartmentService;

    @RequiresPermissions("im:department:view")
    @GetMapping()
    public String department() {
        return prefix + "/department";
    }

    /**
     * 查询部门列表
     */
    @RequiresPermissions("im:department:list")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(ImDepartment imDepartment) {
        startPage();
        List<ImDepartment> list = imDepartmentService.selectImDepartmentList(imDepartment);
        return getDataTable(list);
    }

    /**
     * 查询部门树列表
     */
    @RequiresPermissions("im:department:list")
    @PostMapping("/tree")
    @ResponseBody
    public AjaxResult tree(ImDepartment imDepartment) {
        List<ImDepartment> tree = imDepartmentService.selectImDepartmentTree(imDepartment);
        return AjaxResult.success(tree);
    }

    /**
     * 获取部门统计数据
     */
    @RequiresPermissions("im:department:query")
    @GetMapping("/statistics")
    @ResponseBody
    public AjaxResult getStatistics() {
        return AjaxResult.success(imDepartmentService.getDepartmentStatistics());
    }

    /**
     * 获取部门详细信息
     */
    @RequiresPermissions("im:department:query")
    @GetMapping("/info/{id}")
    @ResponseBody
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return AjaxResult.success(imDepartmentService.selectImDepartmentById(id));
    }

    /**
     * 新增部门
     */
    @RequiresPermissions("im:department:add")
    @Log(title = "部门管理", businessType = BusinessType.INSERT)
    @PostMapping
    @ResponseBody
    public AjaxResult add(@RequestBody ImDepartment imDepartment) {
        return toAjax(imDepartmentService.insertImDepartment(imDepartment));
    }

    /**
     * 修改部门（表单提交方式，用于编辑页面）
     */
    @RequiresPermissions("im:department:edit")
    @Log(title = "部门管理", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    @ResponseBody
    public AjaxResult edit(@RequestBody ImDepartment imDepartment) {
        return toAjax(imDepartmentService.updateImDepartment(imDepartment));
    }

    /**
     * 删除部门
     */
    @RequiresPermissions("im:department:remove")
    @Log(title = "部门管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    @ResponseBody
    public AjaxResult remove(@PathVariable Long[] ids) {
        // 检查是否有子部门
        for (Long id : ids) {
            if (imDepartmentService.checkHasChildren(id) > 0) {
                return AjaxResult.error("存在子部门，不允许删除");
            }
        }
        return toAjax(imDepartmentService.deleteImDepartmentByIds(ids));
    }

    /**
     * 导出部门列表
     */
    @RequiresPermissions("im:department:export")
    @Log(title = "部门管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    @ResponseBody
    public void export(HttpServletResponse response, ImDepartment imDepartment) {
        List<ImDepartment> list = imDepartmentService.selectImDepartmentList(imDepartment);
        ExcelUtil<ImDepartment> util = new ExcelUtil<>(ImDepartment.class);
        util.exportExcel(response, list, "部门数据");
    }
}
