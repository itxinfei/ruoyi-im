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
    public AjaxResult add(ImDingMessage imDingMessage) {
        return toAjax(dingMessageService.insertImDingMessage(imDingMessage));
    }

    /**
     * 修改DING消息（表单提交方式，用于编辑页面）
     */
    @RequiresPermissions("im:ding:edit")
    @Log(title = "DING消息管理", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    @ResponseBody
    public AjaxResult edit(ImDingMessage imDingMessage) {
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

    /**
     * 发送DING消息
     */
    @RequiresPermissions("im:ding:add")
    @Log(title = "发送DING消息", businessType = BusinessType.INSERT)
    @PostMapping("/send")
    @ResponseBody
    public AjaxResult sendDingMessage(@RequestBody ImDingMessage imDingMessage) {
        try {
            dingMessageService.sendDingMessage(imDingMessage);
            return AjaxResult.success("DING消息发送成功");
        } catch (Exception e) {
            logger.error("发送DING消息失败", e);
            return AjaxResult.error("发送失败: " + e.getMessage());
        }
    }

    /**
     * 定时发送DING消息
     */
    @RequiresPermissions("im:ding:add")
    @Log(title = "定时发送DING消息", businessType = BusinessType.INSERT)
    @PostMapping("/schedule")
    @ResponseBody
    public AjaxResult scheduleDingMessage(@RequestBody ImDingMessage imDingMessage) {
        try {
            if (imDingMessage.getScheduleTime() == null) {
                return AjaxResult.error("请设置定时发送时间");
            }
            dingMessageService.scheduleDingMessage(imDingMessage);
            return AjaxResult.success("DING消息已设置定时发送");
        } catch (Exception e) {
            logger.error("设置定时发送失败", e);
            return AjaxResult.error("设置失败: " + e.getMessage());
        }
    }

    /**
     * 取消定时发送
     */
    @RequiresPermissions("im:ding:edit")
    @Log(title = "取消定时发送", businessType = BusinessType.UPDATE)
    @PutMapping("/{id}/cancel-schedule")
    @ResponseBody
    public AjaxResult cancelSchedule(@PathVariable("id") Long id) {
        try {
            dingMessageService.cancelScheduledMessage(id);
            return AjaxResult.success("已取消定时发送");
        } catch (Exception e) {
            logger.error("取消定时发送失败", e);
            return AjaxResult.error("取消失败: " + e.getMessage());
        }
    }

    /**
     * 撤回DING消息
     */
    @RequiresPermissions("im:ding:edit")
    @Log(title = "撤回DING消息", businessType = BusinessType.UPDATE)
    @PutMapping("/{id}/revoke")
    @ResponseBody
    public AjaxResult revokeDingMessage(@PathVariable("id") Long id) {
        try {
            dingMessageService.revokeDingMessage(id);
            return AjaxResult.success("DING消息已撤回");
        } catch (Exception e) {
            logger.error("撤回DING消息失败", e);
            return AjaxResult.error("撤回失败: " + e.getMessage());
        }
    }

    /**
     * 获取DING消息回执统计
     */
    @RequiresPermissions("im:ding:query")
    @GetMapping("/{id}/receipt-statistics")
    @ResponseBody
    public AjaxResult getReceiptStatistics(@PathVariable("id") Long dingId) {
        return AjaxResult.success(dingMessageService.getReceiptStatistics(dingId));
    }

    /**
     * 提醒未读用户
     */
    @RequiresPermissions("im:ding:edit")
    @Log(title = "提醒未读用户", businessType = BusinessType.UPDATE)
    @PostMapping("/{id}/remind")
    @ResponseBody
    public AjaxResult remindUnreadUsers(@PathVariable("id") Long id) {
        try {
            dingMessageService.remindUnreadUsers(id);
            return AjaxResult.success("已提醒未读用户");
        } catch (Exception e) {
            logger.error("提醒未读用户失败", e);
            return AjaxResult.error("提醒失败: " + e.getMessage());
        }
    }

    /**
     * 获取待发送的定时消息
     */
    @RequiresPermissions("im:ding:list")
    @GetMapping("/scheduled")
    @ResponseBody
    public TableDataInfo getScheduledMessages() {
        startPage();
        List<ImDingMessage> list = dingMessageService.getScheduledMessages();
        return getDataTable(list);
    }

    /**
     * 获取DING消息模板列表
     */
    @RequiresPermissions("im:ding:list")
    @GetMapping("/templates")
    @ResponseBody
    public AjaxResult getTemplates() {
        return AjaxResult.success(dingMessageService.getDingTemplates());
    }

    /**
     * 保存DING消息模板
     */
    @RequiresPermissions("im:ding:add")
    @Log(title = "保存DING模板", businessType = BusinessType.INSERT)
    @PostMapping("/templates")
    @ResponseBody
    public AjaxResult saveTemplate(@RequestBody Map<String, Object> template) {
        try {
            dingMessageService.saveDingTemplate(template);
            return AjaxResult.success("模板保存成功");
        } catch (Exception e) {
            logger.error("保存模板失败", e);
            return AjaxResult.error("保存失败: " + e.getMessage());
        }
    }

    /**
     * 删除DING消息模板
     */
    @RequiresPermissions("im:ding:remove")
    @Log(title = "删除DING模板", businessType = BusinessType.DELETE)
    @DeleteMapping("/templates/{id}")
    @ResponseBody
    public AjaxResult deleteTemplate(@PathVariable("id") Long id) {
        try {
            dingMessageService.deleteDingTemplate(id);
            return AjaxResult.success("模板删除成功");
        } catch (Exception e) {
            logger.error("删除模板失败", e);
            return AjaxResult.error("删除失败: " + e.getMessage());
        }
    }

    /**
     * 使用模板创建DING消息
     */
    @RequiresPermissions("im:ding:add")
    @GetMapping("/templates/{id}/create")
    @ResponseBody
    public AjaxResult createFromTemplate(@PathVariable("id") Long templateId) {
        try {
            ImDingMessage message = dingMessageService.createDingFromTemplate(templateId);
            return AjaxResult.success("从模板创建成功", message);
        } catch (Exception e) {
            logger.error("从模板创建失败", e);
            return AjaxResult.error("创建失败: " + e.getMessage());
        }
    }
}
