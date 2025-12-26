package com.ruoyi.im.controller;

import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;

import com.ruoyi.common.core.domain.entity.SysDept;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.system.service.ISysDeptService;

/**
 * IM部门Controller
 * 
 * @author ruoyi
 * @date 2024-08-09
 */
@RestController
@RequestMapping("/im/department")
public class ImDepartmentController extends BaseController
{
    @Autowired
    private ISysDeptService deptService;

    /**
     * 获取部门列表
     */
    @PreAuthorize("@ss.hasPermi('im:department:list')")
    @GetMapping("/list")
    public AjaxResult list(SysDept dept)
    {
        List<SysDept> depts = deptService.selectDeptList(dept);
        return AjaxResult.success(depts);
    }

    /**
     * 查询部门列表（排除节点）
     */
    @PreAuthorize("@ss.hasPermi('im:department:list')")
    @GetMapping("/list/exclude/{deptId}")
    public AjaxResult excludeChild(@PathVariable(value = "deptId", required = false) Long deptId)
    {
        List<SysDept> depts = deptService.selectDeptList(new SysDept());
        depts.removeIf(d -> d.getDeptId().intValue() == deptId || d.getAncestors().contains("," + deptId + ","));
        return AjaxResult.success(depts);
    }

    /**
     * 根据部门编号获取详细信息
     */
    @PreAuthorize("@ss.hasPermi('im:department:query')")
    @GetMapping(value = "/{deptId}")
    public AjaxResult getInfo(@PathVariable Long deptId)
    {
        deptService.checkDeptDataScope(deptId);
        return AjaxResult.success(deptService.selectDeptById(deptId));
    }

    /**
     * 新增部门
     */
    @PreAuthorize("@ss.hasPermi('im:department:add')")
    @Log(title = "部门管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody SysDept dept)
    {
        if (!deptService.checkDeptNameUnique(dept))
        {
            return AjaxResult.error("新增部门'" + dept.getDeptName() + "'失败，部门名称已存在");
        }
        dept.setCreateBy(getLoginName());
        return toAjax(deptService.insertDept(dept));
    }

    /**
     * 修改部门
     */
    @PreAuthorize("@ss.hasPermi('im:department:edit')")
    @Log(title = "部门管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody SysDept dept)
    {
        Long deptId = dept.getDeptId();
        deptService.checkDeptDataScope(deptId);
        if (!deptService.checkDeptNameUnique(dept))
        {
            return AjaxResult.error("修改部门'" + dept.getDeptName() + "'失败，部门名称已存在");
        }
        else if (dept.getParentId().equals(deptId))
        {
            return AjaxResult.error("修改部门'" + dept.getDeptName() + "'失败，上级部门不能是自己");
        }
        else if ("2".equals(dept.getStatus()) && deptService.selectNormalChildrenDeptById(deptId) > 0)
        {
            return AjaxResult.error("该部门包含未停用的子部门！");
        }
        dept.setUpdateBy(getLoginName());
        return toAjax(deptService.updateDept(dept));
    }

    /**
     * 删除部门
     */
    @PreAuthorize("@ss.hasPermi('im:department:remove')")
    @Log(title = "部门管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{deptId}")
    public AjaxResult remove(@PathVariable Long deptId)
    {
        if (deptService.selectDeptCount(deptId) > 0)
        {
            return AjaxResult.error("存在下级部门,不允许删除");
        }
        if (deptService.checkDeptExistUser(deptId))
        {
            return AjaxResult.error("部门存在用户,不允许删除");
        }
        deptService.checkDeptDataScope(deptId);
        return toAjax(deptService.deleteDeptById(deptId));
    }

    /**
     * 获取部门树结构
     */
    @PreAuthorize("@ss.hasPermi('im:department:tree')")
    @GetMapping("/tree")
    public AjaxResult getDeptTree(SysDept dept)
    {
        List<SysDept> depts = deptService.selectDeptList(dept);
        return AjaxResult.success(deptService.selectDeptTree(dept));
    }

    /**
     * 获取部门下拉树列表
     */
    @PreAuthorize("@ss.hasPermi('im:department:tree')")
    @GetMapping("/treeselect")
    public AjaxResult treeselect(SysDept dept)
    {
        List<SysDept> depts = deptService.selectDeptList(dept);
        // TODO: 实现构建部门树选择结构
        return AjaxResult.success(deptService.selectDeptTree(dept));
    }

    /**
     * 加载对应角色部门列表树
     */
    @PreAuthorize("@ss.hasPermi('im:department:tree')")
    @GetMapping(value = "/roleDeptTreeselect/{roleId}")
    public AjaxResult roleDeptTreeselect(@PathVariable("roleId") Long roleId)
    {
        List<SysDept> depts = deptService.selectDeptList(new SysDept());
        AjaxResult ajax = AjaxResult.success();
        // TODO: 实现根据角色ID获取部门列表
        ajax.put("checkedKeys", null);
        // TODO: 实现构建部门树选择结构
        ajax.put("depts", deptService.selectDeptTree(new SysDept()));
        return ajax;
    }

    /**
     * 获取部门用户列表
     */
    @PreAuthorize("@ss.hasPermi('im:department:users')")
    @GetMapping("/{deptId}/users")
    public AjaxResult getDeptUsers(@PathVariable Long deptId)
    {
        // 这里需要调用用户服务获取部门下的用户
        // List<SysUser> users = userService.selectUsersByDeptId(deptId);
        // return AjaxResult.success(users);
        return AjaxResult.success("功能待实现");
    }

    /**
     * 获取部门统计信息
     */
    @PreAuthorize("@ss.hasPermi('im:department:statistics')")
    @GetMapping("/statistics")
    public AjaxResult getDeptStatistics()
    {
        // 统计部门数量、用户数量等信息
        // TODO: 实现部门统计功能
        Map<String, Object> statistics = null;
        return AjaxResult.success(statistics);
    }

    /**
     * 搜索部门
     */
    @PreAuthorize("@ss.hasPermi('im:department:search')")
    @GetMapping("/search")
    public AjaxResult searchDepts(String keyword)
    {
        // TODO: 实现部门搜索功能
        SysDept dept = new SysDept();
        dept.setDeptName(keyword);
        List<SysDept> depts = deptService.selectDeptList(dept);
        return AjaxResult.success(depts);
    }

    /**
     * 获取部门路径
     */
    @PreAuthorize("@ss.hasPermi('im:department:path')")
    @GetMapping("/{deptId}/path")
    public AjaxResult getDeptPath(@PathVariable Long deptId)
    {
        // TODO: 实现获取部门路径功能
        List<SysDept> path = null;
        return AjaxResult.success(path);
    }

    /**
     * 移动部门
     */
    @PreAuthorize("@ss.hasPermi('im:department:move')")
    @Log(title = "移动部门", businessType = BusinessType.UPDATE)
    @PutMapping("/{deptId}/move")
    public AjaxResult moveDept(@PathVariable Long deptId, @RequestBody Map<String, Object> params)
    {
        Long targetParentId = Long.valueOf(params.get("targetParentId").toString());
        // TODO: ISysDeptService中没有moveDept方法，需要实现或使用其他方法
        // return toAjax(deptService.moveDept(deptId, targetParentId));
        return AjaxResult.error("移动部门功能暂未实现");
    }

    /**
     * 复制部门
     */
    @PreAuthorize("@ss.hasPermi('im:department:copy')")
    @Log(title = "复制部门", businessType = BusinessType.INSERT)
    @PostMapping("/{deptId}/copy")
    public AjaxResult copyDept(@PathVariable Long deptId, @RequestBody Map<String, Object> params)
    {
        Long targetParentId = Long.valueOf(params.get("targetParentId").toString());
        String newDeptName = params.get("newDeptName").toString();
        boolean includeUsers = Boolean.parseBoolean(params.get("includeUsers").toString());
        
        // TODO: ISysDeptService中没有copyDept方法，需要实现或使用其他方法
        // return toAjax(deptService.copyDept(deptId, targetParentId, newDeptName, includeUsers));
        return AjaxResult.error("复制部门功能暂未实现");
    }
}