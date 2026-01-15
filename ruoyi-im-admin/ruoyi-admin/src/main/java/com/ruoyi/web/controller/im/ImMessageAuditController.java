package com.ruoyi.web.controller.im;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.web.domain.ImMessage;
import com.ruoyi.web.service.ImMessageService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 消息审核管理控制器
 *
 * @author ruoyi
 */
@Controller
@RequestMapping("/im/message/audit")
public class ImMessageAuditController extends BaseController {

    private String prefix = "im/message";

    @Autowired
    private ImMessageService imMessageService;

    @RequiresPermissions("im:message:audit:view")
    @GetMapping()
    public String message() {
        return prefix + "/message";
    }

    /**
     * 查询敏感消息列表
     */
    @RequiresPermissions("im:message:audit:list")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(ImMessage imMessage) {
        startPage();
        // 默认查询敏感消息
        imMessage.setSensitiveLevel("SENSITIVE");
        List<ImMessage> list = imMessageService.selectImMessageList(imMessage);
        return getDataTable(list);
    }

    /**
     * 搜索消息（按关键词）
     */
    @RequiresPermissions("im:message:audit:query")
    @PostMapping("/search")
    @ResponseBody
    public TableDataInfo search(@RequestParam String keyword) {
        startPage();
        ImMessage query = new ImMessage();
        query.setContent(keyword);
        List<ImMessage> list = imMessageService.selectImMessageList(query);
        return getDataTable(list);
    }

    /**
     * 获取消息详细信息
     */
    @RequiresPermissions("im:message:audit:query")
    @GetMapping("/{id}")
    @ResponseBody
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return AjaxResult.success(imMessageService.selectImMessageById(id));
    }

    /**
     * 标记消息为敏感
     */
    @RequiresPermissions("im:message:audit:edit")
    @Log(title = "标记敏感消息", businessType = BusinessType.UPDATE)
    @PutMapping("/mark-sensitive/{id}")
    @ResponseBody
    public AjaxResult markSensitive(@PathVariable("id") Long id) {
        ImMessage message = new ImMessage();
        message.setId(id);
        message.setSensitiveLevel("SENSITIVE");
        return toAjax(imMessageService.updateImMessage(message));
    }

    /**
     * 取消敏感标记
     */
    @RequiresPermissions("im:message:audit:edit")
    @Log(title = "取消敏感标记", businessType = BusinessType.UPDATE)
    @PutMapping("/unmark-sensitive/{id}")
    @ResponseBody
    public AjaxResult unmarkSensitive(@PathVariable("id") Long id) {
        ImMessage message = new ImMessage();
        message.setId(id);
        message.setSensitiveLevel("NORMAL");
        return toAjax(imMessageService.updateImMessage(message));
    }

    /**
     * 删除违规消息
     */
    @RequiresPermissions("im:message:audit:remove")
    @Log(title = "删除违规消息", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    @ResponseBody
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(imMessageService.deleteImMessageByIds(ids));
    }

    /**
     * 消息统计
     */
    @RequiresPermissions("im:message:audit:query")
    @GetMapping("/statistics")
    @ResponseBody
    public AjaxResult getStatistics() {
        return AjaxResult.success(imMessageService.getMessageStatistics());
    }

    /**
     * 导出敏感消息列表
     */
    @RequiresPermissions("im:message:audit:export")
    @Log(title = "消息审核", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    @ResponseBody
    public void export(HttpServletResponse response, ImMessage imMessage) {
        imMessage.setSensitiveLevel("SENSITIVE");
        List<ImMessage> list = imMessageService.selectImMessageList(imMessage);
        ExcelUtil<ImMessage> util = new ExcelUtil<>(ImMessage.class);
        util.exportExcel(response, list, "敏感消息数据");
    }

    /**
     * 根据会话ID查询消息
     */
    @RequiresPermissions("im:message:audit:query")
    @PostMapping("/conversation/{conversationId}")
    @ResponseBody
    public TableDataInfo getMessagesByConversationId(@PathVariable Long conversationId) {
        startPage();
        ImMessage query = new ImMessage();
        query.setConversationId(conversationId);
        List<ImMessage> list = imMessageService.selectImMessageList(query);
        return getDataTable(list);
    }

    /**
     * 根据用户ID查询消息
     */
    @RequiresPermissions("im:message:audit:query")
    @PostMapping("/sender/{senderId}")
    @ResponseBody
    public TableDataInfo getMessagesBySenderId(@PathVariable Long senderId) {
        startPage();
        ImMessage query = new ImMessage();
        query.setSenderId(senderId);
        List<ImMessage> list = imMessageService.selectImMessageList(query);
        return getDataTable(list);
    }
}
