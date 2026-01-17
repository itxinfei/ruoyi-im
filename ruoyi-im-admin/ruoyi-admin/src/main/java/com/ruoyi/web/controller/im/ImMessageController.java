package com.ruoyi.web.controller.im;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.core.text.Convert;
import com.ruoyi.common.enums.BusinessType;
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
 * IM消息管理控制器（管理后台）
 *
 * @author ruoyi
 * @date 2025-01-07
 */
@Controller
@RequestMapping("/im/message")
public class ImMessageController extends BaseController {

    private String prefix = "im/message";

    @Autowired
    private ImMessageService imMessageService;

    /**
     * 消息管理页面
     */
    @RequiresPermissions("im:message:list")
    @GetMapping()
    public String message() {
        return prefix + "/message";
    }

    /**
     * 新增消息页面
     */
    @RequiresPermissions("im:message:add")
    @GetMapping("/add")
    public String add() {
        return prefix + "/add";
    }

    /**
     * 修改消息页面
     */
    @RequiresPermissions("im:message:edit")
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") Long id, org.springframework.ui.ModelMap mmap) {
        mmap.put("message", imMessageService.selectImMessageById(id));
        return prefix + "/edit";
    }

    /**
     * 查询IM消息列表
     */
    @RequiresPermissions("im:message:list")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(ImMessage imMessage) {
        startPage();
        List<ImMessage> list = imMessageService.selectImMessageList(imMessage);
        return getDataTable(list);
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
    @GetMapping("/info/{id}")
    @ResponseBody
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return AjaxResult.success(imMessageService.selectImMessageById(id));
    }

    /**
     * 新增IM消息
     */
    @RequiresPermissions("im:message:add")
    @Log(title = "IM消息", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    @ResponseBody
    public AjaxResult add(ImMessage imMessage) {
        return toAjax(imMessageService.insertImMessage(imMessage));
    }

    /**
     * 获取消息详情（匹配前端调用 /im/message/{id}）
     */
    @RequiresPermissions("im:message:query")
    @GetMapping("/{id}")
    @ResponseBody
    public AjaxResult getMessageDetail(@PathVariable("id") Long id) {
        return AjaxResult.success(imMessageService.selectImMessageById(id));
    }

    /**
     * 修改IM消息（表单提交方式，用于编辑页面）
     */
    @RequiresPermissions("im:message:edit")
    @Log(title = "IM消息", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    @ResponseBody
    public AjaxResult edit(ImMessage imMessage) {
        return toAjax(imMessageService.updateImMessage(imMessage));
    }

    /**
     * 撤回消息
     */
    @RequiresPermissions("im:message:edit")
    @Log(title = "撤回消息", businessType = BusinessType.UPDATE)
    @PutMapping("/{id}/revoke")
    @ResponseBody
    public AjaxResult revoke(@PathVariable("id") Long messageId) {
        return toAjax(imMessageService.revokeMessage(messageId));
    }

    /**
     * 标记消息为敏感
     */
    @RequiresPermissions("im:message:edit")
    @Log(title = "标记敏感消息", businessType = BusinessType.UPDATE)
    @PutMapping("/{id}/mark-sensitive")
    @ResponseBody
    public AjaxResult markSensitive(@PathVariable("id") Long messageId) {
        ImMessage message = new ImMessage();
        message.setId(messageId);
        message.setSensitiveLevel("SENSITIVE");
        return toAjax(imMessageService.updateImMessage(message));
    }

    /**
     * 取消标记敏感消息
     */
    @RequiresPermissions("im:message:edit")
    @Log(title = "取消标记敏感", businessType = BusinessType.UPDATE)
    @PutMapping("/{id}/unmark-sensitive")
    @ResponseBody
    public AjaxResult unmarkSensitive(@PathVariable("id") Long messageId) {
        ImMessage message = new ImMessage();
        message.setId(messageId);
        message.setSensitiveLevel("NORMAL");
        return toAjax(imMessageService.updateImMessage(message));
    }

    /**
     * 删除IM消息
     */
    @RequiresPermissions("im:message:remove")
    @Log(title = "删除IM消息", businessType = BusinessType.DELETE)
    @PostMapping("/remove/{ids}")
    @ResponseBody
    public AjaxResult remove(@PathVariable String ids) {
        return toAjax(imMessageService.deleteImMessageByIds(Convert.toLongArray(ids)));
    }

    /**
     * 获取会话消息列表
     */
    @RequiresPermissions("im:message:list")
    @GetMapping("/conversation/{conversationId}")
    @ResponseBody
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
    @ResponseBody
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
    @ResponseBody
    public AjaxResult getSensitiveStatistics() {
        // 调用 Service 获取统计数据
        Map<String, Object> dbStats = imMessageService.getMessageStatistics();

        // 转换字段名为驼峰格式（前端期望）
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCount", dbStats.get("total_count"));
        stats.put("normalCount", dbStats.get("normal_count"));
        stats.put("sensitiveCount", dbStats.get("sensitive_count"));
        stats.put("highCount", dbStats.get("high_count"));
        return AjaxResult.success(stats);
    }
}
