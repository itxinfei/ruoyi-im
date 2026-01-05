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
import com.ruoyi.system.domain.ImMessage;
import com.ruoyi.system.service.IImMessageService;

/**
 * IM消息Controller
 * 
 * @author ruoyi
 * @date 2024-01-01
 */
@RestController("adminImMessageController")
@RequestMapping("/api/admin/im/message")
public class ImMessageController extends BaseController
{
    @Autowired
    private IImMessageService imMessageService;

    /**
     * 查询IM消息列表
     */
    @RequiresPermissions("im:message:list")
    @GetMapping("/list")
    public TableDataInfo list(ImMessage imMessage)
    {
        startPage();
        List<ImMessage> list = imMessageService.selectImMessageList(imMessage);
        return getDataTable(list);
    }

    /**
     * 导出IM消息列表
     */
    @RequiresPermissions("im:message:export")
    @Log(title = "IM消息", businessType = com.ruoyi.common.enums.BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, ImMessage imMessage)
    {
        List<ImMessage> list = imMessageService.selectImMessageList(imMessage);
        ExcelUtil<ImMessage> util = new ExcelUtil<ImMessage>(ImMessage.class);
        util.exportExcel(response, list, "IM消息数据");
    }

    /**
     * 获取IM消息详细信息
     */
    @RequiresPermissions("im:message:query")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return AjaxResult.success(imMessageService.selectImMessageById(id));
    }

    /**
     * 新增IM消息
     */
    @RequiresPermissions("im:message:add")
    @Log(title = "IM消息", businessType = com.ruoyi.common.enums.BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody ImMessage imMessage)
    {
        return toAjax(imMessageService.insertImMessage(imMessage));
    }

    /**
     * 修改IM消息
     */
    @RequiresPermissions("im:message:edit")
    @Log(title = "IM消息", businessType = com.ruoyi.common.enums.BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody ImMessage imMessage)
    {
        return toAjax(imMessageService.updateImMessage(imMessage));
    }

    /**
     * 删除IM消息
     */
    @RequiresPermissions("im:message:remove")
    @Log(title = "IM消息", businessType = com.ruoyi.common.enums.BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(imMessageService.deleteImMessageByIds(ids));
    }
}