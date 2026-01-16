package com.ruoyi.web.controller.im;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.core.text.Convert;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.web.domain.ImEmail;
import com.ruoyi.web.service.ImEmailService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 邮件管理控制器
 *
 * @author ruoyi
 * @date 2025-01-15
 */
@Controller
@RequestMapping("/im/email")
public class ImEmailController extends BaseController {

    private String prefix = "im/email";

    @Autowired
    private ImEmailService emailService;

    /**
     * 邮件管理页面
     */
    @RequiresPermissions("im:email:view")
    @GetMapping()
    public String email() {
        return prefix + "/email";
    }

    /**
     * 新增邮件页面
     */
    @RequiresPermissions("im:email:add")
    @GetMapping("/add")
    public String add() {
        return prefix + "/add";
    }

    /**
     * 修改邮件页面
     */
    @RequiresPermissions("im:email:edit")
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") Long id, org.springframework.ui.ModelMap mmap) {
        mmap.put("email", emailService.selectImEmailById(id));
        return prefix + "/edit";
    }

    /**
     * 查询邮件列表
     */
    @RequiresPermissions("im:email:list")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(ImEmail imEmail) {
        startPage();
        List<ImEmail> list = emailService.selectImEmailList(imEmail);
        return getDataTable(list);
    }

    /**
     * 导出邮件列表
     */
    @RequiresPermissions("im:email:export")
    @Log(title = "邮件管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    @ResponseBody
    public void export(HttpServletResponse response, ImEmail imEmail) {
        List<ImEmail> list = emailService.selectImEmailList(imEmail);
        ExcelUtil<ImEmail> util = new ExcelUtil<>(ImEmail.class);
        util.exportExcel(response, list, "邮件数据");
    }

    /**
     * 获取邮件详细信息
     */
    @RequiresPermissions("im:email:query")
    @GetMapping("/info/{id}")
    @ResponseBody
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return AjaxResult.success(emailService.selectImEmailById(id));
    }

    /**
     * 新增邮件
     */
    @RequiresPermissions("im:email:add")
    @Log(title = "邮件管理", businessType = BusinessType.INSERT)
    @PostMapping
    @ResponseBody
    public AjaxResult add(@RequestBody ImEmail imEmail) {
        return toAjax(emailService.insertImEmail(imEmail));
    }

    /**
     * 修改邮件（表单提交方式，用于编辑页面）
     */
    @RequiresPermissions("im:email:edit")
    @Log(title = "邮件管理", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    @ResponseBody
    public AjaxResult edit(@RequestBody ImEmail imEmail) {
        return toAjax(emailService.updateImEmail(imEmail));
    }

    /**
     * 删除邮件
     */
    @RequiresPermissions("im:email:remove")
    @Log(title = "邮件管理", businessType = BusinessType.DELETE)
    @PostMapping("/remove/{ids}")
    @ResponseBody
    public AjaxResult remove(@PathVariable String ids) {
        return toAjax(emailService.deleteImEmailByIds(Convert.toLongArray(ids)));
    }

    /**
     * 获取邮件统计
     */
    @RequiresPermissions("im:email:list")
    @GetMapping("/statistics")
    @ResponseBody
    public AjaxResult getStatistics() {
        return AjaxResult.success(emailService.getEmailStatistics());
    }
}
