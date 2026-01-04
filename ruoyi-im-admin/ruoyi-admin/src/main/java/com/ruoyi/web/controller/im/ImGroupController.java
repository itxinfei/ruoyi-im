package com.ruoyi.web.controller.im;

import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.apache.shiro.authz.annotation.RequiresPermissions;
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
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.system.domain.ImGroup;
import com.ruoyi.system.service.IImGroupService;

/**
 * IM群组Controller
 * 
 * @author ruoyi
 * @date 2024-01-01
 */
@RestController
@RequestMapping("/api/admin/im/group")
public class ImGroupController extends BaseController
{
    @Autowired
    private IImGroupService imGroupService;

    /**
     * 查询IM群组列表
     */
    @RequiresPermissions("im:group:list")
    @GetMapping("/list")
    public TableDataInfo list(ImGroup imGroup)
    {
        startPage();
        List<ImGroup> list = imGroupService.selectImGroupList(imGroup);
        return getDataTable(list);
    }

    /**
     * 导出IM群组列表
     */
    @RequiresPermissions("im:group:export")
    @Log(title = "IM群组", businessType = com.ruoyi.common.enums.BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, ImGroup imGroup)
    {
        List<ImGroup> list = imGroupService.selectImGroupList(imGroup);
        ExcelUtil<ImGroup> util = new ExcelUtil<ImGroup>(ImGroup.class);
        util.exportExcel(response, list, "IM群组数据");
    }

    /**
     * 获取IM群组详细信息
     */
    @RequiresPermissions("im:group:query")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return AjaxResult.success(imGroupService.selectImGroupById(id));
    }

    /**
     * 新增IM群组
     */
    @RequiresPermissions("im:group:add")
    @Log(title = "IM群组", businessType = com.ruoyi.common.enums.BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody ImGroup imGroup)
    {
        return toAjax(imGroupService.insertImGroup(imGroup));
    }

    /**
     * 修改IM群组
     */
    @RequiresPermissions("im:group:edit")
    @Log(title = "IM群组", businessType = com.ruoyi.common.enums.BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody ImGroup imGroup)
    {
        return toAjax(imGroupService.updateImGroup(imGroup));
    }

    /**
     * 删除IM群组
     */
    @RequiresPermissions("im:group:remove")
    @Log(title = "IM群组", businessType = com.ruoyi.common.enums.BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(imGroupService.deleteImGroupByIds(ids));
    }
}