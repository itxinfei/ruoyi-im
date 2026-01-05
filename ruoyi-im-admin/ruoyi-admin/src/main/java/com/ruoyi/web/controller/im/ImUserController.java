package com.ruoyi.web.controller.im;

import java.util.List;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
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
import com.ruoyi.system.domain.ImUser;
import com.ruoyi.web.service.IImUserService;

/**
 * IM用户Controller
 * 
 * @author ruoyi
 * @date 2024-01-01
 */
@RestController("adminImUserController")
@RequestMapping("/api/admin/im/user")
public class ImUserController extends BaseController
{
    @Autowired
    private IImUserService imUserService;

    /**
     * 查询IM用户列表
     */
    @RequiresPermissions("im:user:list")
    @GetMapping("/list")
    public TableDataInfo list(ImUser imUser)
    {
        startPage();
        List<ImUser> list = imUserService.selectImUserList(imUser);
        return getDataTable(list);
    }

    /**
     * 导出IM用户列表
     */
    @RequiresPermissions("im:user:export")
    @Log(title = "IM用户", businessType = com.ruoyi.common.enums.BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, ImUser imUser)
    {
        List<ImUser> list = imUserService.selectImUserList(imUser);
        ExcelUtil<ImUser> util = new ExcelUtil<ImUser>(ImUser.class);
        util.exportExcel(response, list, "IM用户数据");
    }

    /**
     * 获取IM用户详细信息
     */
    @RequiresPermissions("im:user:query")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return AjaxResult.success(imUserService.selectImUserById(id));
    }

    /**
     * 新增IM用户
     */
    @RequiresPermissions("im:user:add")
    @Log(title = "IM用户", businessType = com.ruoyi.common.enums.BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Valid @RequestBody ImUser imUser)
    {
        return toAjax(imUserService.insertImUser(imUser));
    }

    /**
     * 修改IM用户
     */
    @RequiresPermissions("im:user:edit")
    @Log(title = "IM用户", businessType = com.ruoyi.common.enums.BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Valid @RequestBody ImUser imUser)
    {
        return toAjax(imUserService.updateImUser(imUser));
    }

    /**
     * 删除IM用户
     */
    @RequiresPermissions("im:user:remove")
    @Log(title = "IM用户", businessType = com.ruoyi.common.enums.BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(imUserService.deleteImUserByIds(ids));
    }
}