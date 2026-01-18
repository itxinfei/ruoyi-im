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
    @GetMapping("/{id}")
    @ResponseBody
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return AjaxResult.success(emailService.selectImEmailById(id));
    }

    /**
     * 新增邮件
     */
    @RequiresPermissions("im:email:add")
    @Log(title = "邮件管理", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    @ResponseBody
    public AjaxResult add(ImEmail imEmail) {
        return toAjax(emailService.insertImEmail(imEmail));
    }

    /**
     * 修改邮件（表单提交方式，用于编辑页面）
     */
    @RequiresPermissions("im:email:edit")
    @Log(title = "邮件管理", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    @ResponseBody
    public AjaxResult edit(ImEmail imEmail) {
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

    /**
     * 标记邮件为已读
     */
    @RequiresPermissions("im:email:edit")
    @Log(title = "标记邮件已读", businessType = BusinessType.UPDATE)
    @PutMapping("/{id}/read")
    @ResponseBody
    public AjaxResult markAsRead(@PathVariable("id") Long id) {
        return toAjax(emailService.markAsRead(id));
    }

    /**
     * 批量标记邮件为已读
     */
    @RequiresPermissions("im:email:edit")
    @Log(title = "批量标记已读", businessType = BusinessType.UPDATE)
    @PostMapping("/batch-read")
    @ResponseBody
    public AjaxResult batchMarkAsRead(@RequestBody List<Long> ids) {
        int count = emailService.batchMarkAsRead(ids);
        return AjaxResult.success("成功标记 " + count + " 封邮件为已读");
    }

    /**
     * 标记邮件为未读
     */
    @RequiresPermissions("im:email:edit")
    @Log(title = "标记邮件未读", businessType = BusinessType.UPDATE)
    @PutMapping("/{id}/unread")
    @ResponseBody
    public AjaxResult markAsUnread(@PathVariable("id") Long id) {
        return toAjax(emailService.markAsUnread(id));
    }

    /**
     * 标记邮件为重要
     */
    @RequiresPermissions("im:email:edit")
    @Log(title = "标记邮件重要", businessType = BusinessType.UPDATE)
    @PutMapping("/{id}/star")
    @ResponseBody
    public AjaxResult markAsStarred(@PathVariable("id") Long id) {
        return toAjax(emailService.markAsStarred(id));
    }

    /**
     * 取消标记邮件为重要
     */
    @RequiresPermissions("im:email:edit")
    @Log(title = "取消标记重要", businessType = BusinessType.UPDATE)
    @PutMapping("/{id}/unstar")
    @ResponseBody
    public AjaxResult unmarkAsStarred(@PathVariable("id") Long id) {
        return toAjax(emailService.unmarkAsStarred(id));
    }

    /**
     * 移动邮件到垃圾箱
     */
    @RequiresPermissions("im:email:edit")
    @Log(title = "移动到垃圾箱", businessType = BusinessType.UPDATE)
    @PutMapping("/{id}/trash")
    @ResponseBody
    public AjaxResult moveToTrash(@PathVariable("id") Long id) {
        return toAjax(emailService.moveToTrash(id));
    }

    /**
     * 批量移动到垃圾箱
     */
    @RequiresPermissions("im:email:edit")
    @Log(title = "批量移动到垃圾箱", businessType = BusinessType.UPDATE)
    @PostMapping("/batch-trash")
    @ResponseBody
    public AjaxResult batchMoveToTrash(@RequestBody List<Long> ids) {
        int count = emailService.batchMoveToTrash(ids);
        return AjaxResult.success("成功移动 " + count + " 封邮件到垃圾箱");
    }

    /**
     * 从垃圾箱恢复邮件
     */
    @RequiresPermissions("im:email:edit")
    @Log(title = "恢复邮件", businessType = BusinessType.UPDATE)
    @PutMapping("/{id}/restore")
    @ResponseBody
    public AjaxResult restoreFromTrash(@PathVariable("id") Long id) {
        return toAjax(emailService.restoreFromTrash(id));
    }

    /**
     * 永久删除邮件
     */
    @RequiresPermissions("im:email:remove")
    @Log(title = "永久删除邮件", businessType = BusinessType.DELETE)
    @DeleteMapping("/{id}/permanent")
    @ResponseBody
    public AjaxResult permanentDelete(@PathVariable("id") Long id) {
        return toAjax(emailService.permanentDelete(id));
    }

    /**
     * 清空垃圾箱
     */
    @RequiresPermissions("im:email:remove")
    @Log(title = "清空垃圾箱", businessType = BusinessType.DELETE)
    @DeleteMapping("/trash/clear")
    @ResponseBody
    public AjaxResult clearTrash() {
        int count = emailService.clearTrash();
        return AjaxResult.success("成功清空垃圾箱，共删除 " + count + " 封邮件");
    }

    /**
     * 获取收件箱邮件
     */
    @RequiresPermissions("im:email:list")
    @GetMapping("/inbox")
    @ResponseBody
    public TableDataInfo getInbox() {
        startPage();
        List<ImEmail> list = emailService.getEmailsByFolder("INBOX");
        return getDataTable(list);
    }

    /**
     * 获取发件箱邮件
     */
    @RequiresPermissions("im:email:list")
    @GetMapping("/sent")
    @ResponseBody
    public TableDataInfo getSent() {
        startPage();
        List<ImEmail> list = emailService.getEmailsByFolder("SENT");
        return getDataTable(list);
    }

    /**
     * 获取草稿箱邮件
     */
    @RequiresPermissions("im:email:list")
    @GetMapping("/drafts")
    @ResponseBody
    public TableDataInfo getDrafts() {
        startPage();
        List<ImEmail> list = emailService.getEmailsByFolder("DRAFT");
        return getDataTable(list);
    }

    /**
     * 获取垃圾箱邮件
     */
    @RequiresPermissions("im:email:list")
    @GetMapping("/trash")
    @ResponseBody
    public TableDataInfo getTrash() {
        startPage();
        List<ImEmail> list = emailService.getEmailsByFolder("TRASH");
        return getDataTable(list);
    }

    /**
     * 获取未读邮件数量
     */
    @RequiresPermissions("im:email:list")
    @GetMapping("/unread-count")
    @ResponseBody
    public AjaxResult getUnreadCount() {
        int count = emailService.getUnreadCount();
        return AjaxResult.success(count);
    }
}
