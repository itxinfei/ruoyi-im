package com.ruoyi.web.controller.im;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.web.domain.ImMessage;
import com.ruoyi.web.service.ImMessageService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * IM消息管理控制器（管理后台）
 *
 * @author ruoyi
 * @date 2025-01-07
 */
@RestController
@RequestMapping("api/admin/im/message")
public class ImMessageController extends BaseController {

    @Autowired
    private ImMessageService imMessageService;

    /**
     * 查询IM消息列表
     */
    @RequiresPermissions("im:message:list")
    @GetMapping("/list")
    public AjaxResult list(ImMessage imMessage) {
        startPage();
        List<ImMessage> list = imMessageService.selectImMessageList(imMessage);
        return AjaxResult.success(list);
    }

    /**
     * 导出IM消息列表
     */
    @RequiresPermissions("im:message:export")
    @Log(title = "IM消息", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, ImMessage imMessage) {
        List<ImMessage> list = imMessageService.selectImMessageList(imMessage);
        // 导出逻辑
    }

    /**
     * 获取IM消息详细信息
     */
    @RequiresPermissions("im:message:query")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return AjaxResult.success(imMessageService.selectImMessageById(id));
    }

    /**
     * 撤回消息
     */
    @RequiresPermissions("im:message:edit")
    @Log(title = "撤回消息", businessType = BusinessType.UPDATE)
    @PutMapping("/{id}/revoke")
    public AjaxResult revoke(@PathVariable("id") Long messageId) {
        return toAjax(imMessageService.revokeMessage(messageId));
    }

    /**
     * 删除IM消息
     */
    @RequiresPermissions("im:message:remove")
    @Log(title = "删除IM消息", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(imMessageService.deleteImMessageByIds(ids));
    }

    /**
     * 获取会话消息列表
     */
    @RequiresPermissions("im:message:list")
    @GetMapping("/conversation/{conversationId}")
    public AjaxResult getByConversation(@PathVariable("conversationId") Long conversationId,
                                      @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                                      @RequestParam(value = "pageSize", defaultValue = "20") Integer pageSize) {
        List<ImMessage> list = imMessageService.selectImMessageListByConversationId(conversationId);
        return AjaxResult.success(list);
    }

    /**
     * 按时间范围查询消息
     */
    @RequiresPermissions("im:message:list")
    @GetMapping("/conversation/{conversationId}/range")
    public AjaxResult getByTimeRange(@PathVariable("conversationId") Long conversationId,
                                   @RequestParam String startTime,
                                   @RequestParam String endTime) {
        List<ImMessage> list = imMessageService.selectImMessageListByTimeRange(conversationId, startTime, endTime);
        return AjaxResult.success(list);
    }

    /**
     * 敏感消息统计
     */
    @RequiresPermissions("im:message:list")
    @GetMapping("/statistics/sensitive")
    public AjaxResult getSensitiveStatistics() {
        return AjaxResult.success(imMessageService.countSensitiveMessages());
    }
}
