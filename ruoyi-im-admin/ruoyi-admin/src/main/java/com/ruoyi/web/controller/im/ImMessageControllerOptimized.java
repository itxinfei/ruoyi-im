package com.ruoyi.web.controller.im;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.web.domain.ImMessage;
import com.ruoyi.web.domain.dto.MessageSendDTO;
import com.ruoyi.web.domain.vo.MessageDetailVO;
import com.ruoyi.web.service.IImMessageService;
import com.ruoyi.web.service.MessageDecryptionService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * IM消息管理控制器（优化版）
 * 
 * @author ruoyi
 * @version 1.0
 * @since 2025-01-22
 */
@Controller
@RequestMapping("/im/message")
public class ImMessageController extends BaseController {

    private String prefix = "im/message";

    @Autowired
    private IImMessageService imMessageService;

    @Autowired
    private MessageDecryptionService messageDecryptionService;

    @RequiresPermissions("im:message:list")
    @GetMapping()
    public String message() {
        return prefix + "/message";
    }

    @RequiresPermissions("im:message:add")
    @GetMapping("/add")
    public String add() {
        return prefix + "/add";
    }

    @RequiresPermissions("im:message:edit")
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") Long id, ModelMap mmap) {
        ImMessage message = imMessageService.selectImMessageById(id);
        mmap.put("message", message);
        return prefix + "/edit";
    }

    @RequiresPermissions("im:message:list")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(ImMessage imMessage) {
        startPage();
        List<ImMessage> list = imMessageService.selectImMessageList(imMessage);
        return getDataTable(list);
    }

    @RequiresPermissions("im:message:export")
    @Log(title = "IM消息", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, ImMessage imMessage) {
        List<ImMessage> list = imMessageService.selectImMessageList(imMessage);
        ExcelUtil<ImMessage> util = new ExcelUtil<>(ImMessage.class);
        util.exportExcel(response, list, "消息数据");
    }

    @RequiresPermissions("im:message:query")
    @GetMapping("/info/{id}")
    @ResponseBody
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return AjaxResult.success(imMessageService.selectImMessageById(id));
    }

    @RequiresPermissions("im:message:add")
    @Log(title = "IM消息", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    @ResponseBody
    public AjaxResult add(@Validated @RequestBody MessageSendDTO dto) {
        try {
            ImMessage message = imMessageService.sendMessage(dto);
            return AjaxResult.success(message);
        } catch (Exception e) {
            return AjaxResult.error("消息发送失败：" + e.getMessage());
        }
    }

    @RequiresPermissions("im:message:query")
    @GetMapping("/{id}")
    @ResponseBody
    public AjaxResult getMessageDetail(@PathVariable("id") Long id) {
        try {
            MessageDetailVO messageDetail = imMessageService.getMessageDetail(id);
            return AjaxResult.success(messageDetail);
        } catch (Exception e) {
            return AjaxResult.error("获取消息详情失败：" + e.getMessage());
        }
    }

    @RequiresPermissions("im:message:edit")
    @Log(title = "IM消息", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    @ResponseBody
    public AjaxResult edit(ImMessage imMessage) {
        return toAjax(imMessageService.updateImMessage(imMessage));
    }

    @RequiresPermissions("im:message:edit")
    @Log(title = "撤回消息", businessType = BusinessType.UPDATE)
    @PostMapping("/revoke/{id}")
    @ResponseBody
    public AjaxResult revoke(@PathVariable("id") Long messageId) {
        try {
            boolean result = imMessageService.revokeMessage(messageId);
            return result ? AjaxResult.success("消息撤回成功") : AjaxResult.error("消息撤回失败");
        } catch (Exception e) {
            return AjaxResult.error("消息撤回失败：" + e.getMessage());
        }
    }

    @RequiresPermissions("im:message:edit")
    @Log(title = "批量撤回消息", businessType = BusinessType.UPDATE)
    @PostMapping("/batchRevoke")
    @ResponseBody
    public AjaxResult batchRevoke(@RequestParam String messageIds) {
        try {
            int count = imMessageService.batchRevokeMessages(messageIds);
            return AjaxResult.success("成功撤回 " + count + " 条消息");
        } catch (Exception e) {
            return AjaxResult.error("批量撤回失败：" + e.getMessage());
        }
    }

    @RequiresPermissions("im:message:edit")
    @Log(title = "标记敏感消息", businessType = BusinessType.UPDATE)
    @PutMapping("/{id}/mark-sensitive")
    @ResponseBody
    public AjaxResult markSensitive(@PathVariable("id") Long messageId) {
        try {
            boolean result = imMessageService.markAsSensitive(messageId);
            return result ? AjaxResult.success("标记成功") : AjaxResult.error("标记失败");
        } catch (Exception e) {
            return AjaxResult.error("标记失败：" + e.getMessage());
        }
    }

    @RequiresPermissions("im:message:edit")
    @Log(title = "批量标记敏感消息", businessType = BusinessType.UPDATE)
    @PostMapping("/batch-mark-sensitive")
    @ResponseBody
    public AjaxResult batchMarkSensitive(@RequestBody List<Long> messageIds) {
        try {
            int count = imMessageService.batchMarkAsSensitive(messageIds);
            return AjaxResult.success("成功标记 " + count + " 条消息");
        } catch (Exception e) {
            return AjaxResult.error("批量标记失败：" + e.getMessage());
        }
    }

    @RequiresPermissions("im:message:edit")
    @Log(title = "取消标记敏感", businessType = BusinessType.UPDATE)
    @PutMapping("/{id}/unmark-sensitive")
    @ResponseBody
    public AjaxResult unmarkSensitive(@PathVariable("id") Long messageId) {
        try {
            boolean result = imMessageService.unmarkSensitive(messageId);
            return result ? AjaxResult.success("取消标记成功") : AjaxResult.error("取消标记失败");
        } catch (Exception e) {
            return AjaxResult.error("取消标记失败：" + e.getMessage());
        }
    }

    @RequiresPermissions("im:message:edit")
    @Log(title = "批量取消标记敏感", businessType = BusinessType.UPDATE)
    @PostMapping("/batch-unmark-sensitive")
    @ResponseBody
    public AjaxResult batchUnmarkSensitive(@RequestBody List<Long> messageIds) {
        try {
            int count = imMessageService.batchUnmarkSensitive(messageIds);
            return AjaxResult.success("成功取消标记 " + count + " 条消息");
        } catch (Exception e) {
            return AjaxResult.error("批量取消标记失败：" + e.getMessage());
        }
    }

    @RequiresPermissions("im:message:remove")
    @Log(title = "删除IM消息", businessType = BusinessType.DELETE)
    @PostMapping("/remove/{ids}")
    @ResponseBody
    public AjaxResult remove(@PathVariable String ids) {
        return toAjax(imMessageService.deleteImMessageByIds(Convert.toLongArray(ids)));
    }

    @RequiresPermissions("im:message:list")
    @GetMapping("/conversation/{conversationId}")
    @ResponseBody
    public AjaxResult getByConversation(@PathVariable("conversationId") Long conversationId,
                                     @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                                     @RequestParam(value = "pageSize", defaultValue = "20") Integer pageSize) {
        try {
            List<MessageDetailVO> messages = imMessageService.getMessagesByConversationId(conversationId, pageNum, pageSize);
            return AjaxResult.success(messages);
        } catch (Exception e) {
            return AjaxResult.error("获取会话消息失败：" + e.getMessage());
        }
    }

    @RequiresPermissions("im:message:list")
    @GetMapping("/conversation/{conversationId}/range")
    @ResponseBody
    public AjaxResult getByTimeRange(@PathVariable("conversationId") Long conversationId,
                                    @RequestParam String startTime,
                                    @RequestParam String endTime) {
        try {
            List<MessageDetailVO> messages = imMessageService.getMessagesByTimeRange(conversationId, startTime, endTime);
            return AjaxResult.success(messages);
        } catch (Exception e) {
            return AjaxResult.error("按时间范围查询消息失败：" + e.getMessage());
        }
    }

    @RequiresPermissions("im:message:list")
    @GetMapping("/statistics")
    @ResponseBody
    public AjaxResult getStatistics() {
        try {
            Map<String, Object> stats = imMessageService.getMessageStatistics();
            return AjaxResult.success(stats);
        } catch (Exception e) {
            return AjaxResult.error("获取统计数据失败：" + e.getMessage());
        }
    }

    @RequiresPermissions("im:message:list")
    @GetMapping("/statistics/sensitive")
    @ResponseBody
    public AjaxResult getSensitiveStatistics() {
        try {
            Map<String, Object> stats = imMessageService.getSensitiveMessageStatistics();
            return AjaxResult.success(stats);
        } catch (Exception e) {
            return AjaxResult.error("获取敏感消息统计失败：" + e.getMessage());
        }
    }
}