package com.ruoyi.web.controller.im;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.core.text.Convert;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.web.domain.ImDingMessage;
import com.ruoyi.web.service.ImDingMessageService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * DING消息管理控制器
 *
 * @author ruoyi
 * @date 2025-01-15
 */
@Controller
@RequestMapping("/im/ding")
public class ImDingMessageController extends BaseController {

    private String prefix = "im/ding";

    @Autowired
    private ImDingMessageService dingMessageService;

    /**
     * DING消息管理页面
     */
    @RequiresPermissions("im:ding:view")
    @GetMapping()
    public String ding() {
        return prefix + "/ding";
    }

    /**
     * 新增DING消息页面
     */
    @RequiresPermissions("im:ding:add")
    @GetMapping("/add")
    public String add() {
        return prefix + "/add";
    }

    /**
     * 修改DING消息页面
     */
    @RequiresPermissions("im:ding:edit")
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") Long id, org.springframework.ui.ModelMap mmap) {
        mmap.put("ding", dingMessageService.selectImDingMessageById(id));
        return prefix + "/edit";
    }

    /**
     * 查询DING消息列表
     */
    @RequiresPermissions("im:ding:list")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(ImDingMessage imDingMessage) {
        startPage();
        List<ImDingMessage> list = dingMessageService.selectImDingMessageList(imDingMessage);
        return getDataTable(list);
    }

    /**
     * 导出DING消息列表
     */
    @RequiresPermissions("im:ding:export")
    @Log(title = "DING消息管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    @ResponseBody
    public void export(HttpServletResponse response, ImDingMessage imDingMessage) {
        List<ImDingMessage> list = dingMessageService.selectImDingMessageList(imDingMessage);
        ExcelUtil<ImDingMessage> util = new ExcelUtil<>(ImDingMessage.class);
        util.exportExcel(response, list, "DING消息数据");
    }

    /**
     * 获取DING消息详细信息
     */
    @RequiresPermissions("im:ding:query")
    @GetMapping("/info/{id}")
    @ResponseBody
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return AjaxResult.success(dingMessageService.selectImDingMessageById(id));
    }

    /**
     * 新增DING消息
     */
    @RequiresPermissions("im:ding:add")
    @Log(title = "DING消息管理", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    @ResponseBody
    public AjaxResult add(@RequestBody ImDingMessage imDingMessage) {
        return toAjax(dingMessageService.insertImDingMessage(imDingMessage));
    }

    /**
     * 修改DING消息（表单提交方式，用于编辑页面）
     */
    @RequiresPermissions("im:ding:edit")
    @Log(title = "DING消息管理", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    @ResponseBody
    public AjaxResult edit(@RequestBody ImDingMessage imDingMessage) {
        return toAjax(dingMessageService.updateImDingMessage(imDingMessage));
    }

    /**
     * 删除DING消息
     */
    @RequiresPermissions("im:ding:remove")
    @Log(title = "DING消息管理", businessType = BusinessType.DELETE)
    @PostMapping("/remove/{ids}")
    @ResponseBody
    public AjaxResult remove(@PathVariable String ids) {
        return toAjax(dingMessageService.deleteImDingMessageByIds(Convert.toLongArray(ids)));
    }

    /**
     * 获取DING消息统计
     */
    @RequiresPermissions("im:ding:list")
    @GetMapping("/statistics")
    @ResponseBody
    public AjaxResult getStatistics() {
        return AjaxResult.success(dingMessageService.getDingStatistics());
    }

    /**
     * 获取DING消息回执详情
     */
    @RequiresPermissions("im:ding:query")
    @GetMapping("/{id}/receipts")
    @ResponseBody
    public AjaxResult getReceipts(@PathVariable("id") Long dingId) {
        return AjaxResult.success(dingMessageService.getDingReceipts(dingId));
    }
}
