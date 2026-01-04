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
import com.ruoyi.system.domain.ImSession;
import com.ruoyi.system.service.IImSessionService;

/**
 * IM会话Controller
 * 
 * @author ruoyi
 * @date 2024-01-01
 */
@RestController
@RequestMapping("/api/admin/im/session")
public class ImSessionController extends BaseController
{
    @Autowired
    private IImSessionService imSessionService;

    /**
     * 查询IM会话列表
     */
    @RequiresPermissions("im:session:list")
    @GetMapping("/list")
    public TableDataInfo list(ImSession imSession)
    {
        startPage();
        List<ImSession> list = imSessionService.selectImSessionList(imSession);
        return getDataTable(list);
    }

    /**
     * 导出IM会话列表
     */
    @RequiresPermissions("im:session:export")
    @Log(title = "IM会话", businessType = com.ruoyi.common.enums.BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, ImSession imSession)
    {
        List<ImSession> list = imSessionService.selectImSessionList(imSession);
        ExcelUtil<ImSession> util = new ExcelUtil<ImSession>(ImSession.class);
        util.exportExcel(response, list, "IM会话数据");
    }

    /**
     * 获取IM会话详细信息
     */
    @RequiresPermissions("im:session:query")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return AjaxResult.success(imSessionService.selectImSessionById(id));
    }

    /**
     * 新增IM会话
     */
    @RequiresPermissions("im:session:add")
    @Log(title = "IM会话", businessType = com.ruoyi.common.enums.BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody ImSession imSession)
    {
        return toAjax(imSessionService.insertImSession(imSession));
    }

    /**
     * 修改IM会话
     */
    @RequiresPermissions("im:session:edit")
    @Log(title = "IM会话", businessType = com.ruoyi.common.enums.BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody ImSession imSession)
    {
        return toAjax(imSessionService.updateImSession(imSession));
    }

    /**
     * 删除IM会话
     */
    @RequiresPermissions("im:session:remove")
    @Log(title = "IM会话", businessType = com.ruoyi.common.enums.BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(imSessionService.deleteImSessionByIds(ids));
    }
}