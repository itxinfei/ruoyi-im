package com.ruoyi.project.im.controller.im;

import com.ruoyi.framework.aspectj.lang.annotation.Log;
import com.ruoyi.framework.web.controller.BaseController;
import com.ruoyi.framework.web.domain.AjaxResult;
import com.ruoyi.framework.web.page.TableDataInfo;
import com.ruoyi.framework.aspectj.lang.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.project.im.domain.ImDingReceipt;
import com.ruoyi.project.im.service.ImDingReceiptService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * DING回执管理控制器
 *
 * @author ruoyi
 * @date 2025-01-18
 */
@Controller
@RequestMapping("/im/dingReceipt")
public class ImDingReceiptController extends BaseController {

    private String prefix = "im/dingReceipt";

    @Autowired
    private ImDingReceiptService dingReceiptService;

    /**
     * DING回执管理页面
     */
    @RequiresPermissions("im:dingReceipt:view")
    @GetMapping()
    public String dingReceipt() {
        return prefix + "/dingReceipt";
    }

    /**
     * 查询DING回执列表
     */
    @RequiresPermissions("im:dingReceipt:list")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(ImDingReceipt imDingReceipt) {
        startPage();
        List<ImDingReceipt> list = dingReceiptService.selectImDingReceiptList(imDingReceipt);
        return getDataTable(list);
    }

    /**
     * 获取DING回执统计数据
     */
    @RequiresPermissions("im:dingReceipt:list")
    @GetMapping("/statistics")
    @ResponseBody
    public AjaxResult getStatistics(@RequestParam(required = false) Long dingId) {
        Map<String, Object> statistics = dingReceiptService.getReceiptStatistics(dingId);
        return AjaxResult.success(statistics);
    }

    /**
     * 根据DING ID查询回执列表
     */
    @RequiresPermissions("im:dingReceipt:list")
    @GetMapping("/ding/{dingId}")
    @ResponseBody
    public AjaxResult getReceiptsByDingId(@PathVariable("dingId") Long dingId) {
        List<ImDingReceipt> list = dingReceiptService.selectReceiptsByDingId(dingId);
        return AjaxResult.success(list);
    }

    /**
     * 导出DING回执列表
     */
    @RequiresPermissions("im:dingReceipt:export")
    @Log(title = "DING回执管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    @ResponseBody
    public void export(HttpServletResponse response, ImDingReceipt imDingReceipt) {
        List<ImDingReceipt> list = dingReceiptService.selectImDingReceiptList(imDingReceipt);
        ExcelUtil<ImDingReceipt> util = new ExcelUtil<>(ImDingReceipt.class);
        util.exportExcel(response, list, "DING回执数据");
    }

    /**
     * DING回执详情
     */
    @RequiresPermissions("im:dingReceipt:query")
    @GetMapping("/{id}")
    @ResponseBody
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return AjaxResult.success(dingReceiptService.selectImDingReceiptById(id));
    }
}
