package com.ruoyi.im.controller;

import java.util.List;
import java.util.Map;
import com.alibaba.fastjson.JSON;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.im.domain.ImMessage;
import com.ruoyi.im.service.IImMessageService;

/**
 * IM消息Controller
 *
 * @author ruoyi
 * @date 2024-08-09
 */
@RestController
@RequestMapping("/im/message")
public class ImMessageController extends BaseController {

    @Autowired
    private IImMessageService imMessageService;

    /**
     * 查询IM消息列表
     */
    @PreAuthorize("@ss.hasPermi('im:message:list')")
    @GetMapping("/list")
    public TableDataInfo list(ImMessage imMessage) {
        startPage();
        List<ImMessage> list = imMessageService.list();
        return getDataTable(list);
    }

    /**
     * 获取消息历史
     */
    @PreAuthorize("@ss.hasPermi('im:message:history')")
    @GetMapping("/history")
    public TableDataInfo getMessageHistory(@RequestParam Long sessionId, @RequestParam(required = false) Long lastMessageId, @RequestParam(defaultValue = "20") int size) {
        startPage();
        List<ImMessage> messages = imMessageService.selectConversationMessages(sessionId, getUserId(), lastMessageId, size);
        return getDataTable(messages);
    }

    /**
     * 搜索消息
     */
    @PreAuthorize("@ss.hasPermi('im:message:search')")
    @GetMapping("/search")
    public TableDataInfo searchMessages(@RequestParam String keyword, @RequestParam(required = false) Long sessionId) {
        startPage();
        List<ImMessage> messages = imMessageService.searchMessages(sessionId, getUserId(), keyword, null, null, null);
        return getDataTable(messages);
    }

    /**
     * 发送消息
     */
    @PreAuthorize("@ss.hasPermi('im:message:send')")
    @Log(title = "发送消息", businessType = BusinessType.INSERT)
    @PostMapping("/send")
    public AjaxResult sendMessage(@RequestBody Map<String, Object> params) {
        Long conversationId = Long.valueOf(params.get("conversationId").toString());
        String type = params.get("type").toString();
        Object content = params.get("content");
        Long replyToMessageId = params.get("replyToMessageId") != null ? 
            Long.valueOf(params.get("replyToMessageId").toString()) : null;
        String clientMsgId = params.get("clientMsgId") != null ? 
            params.get("clientMsgId").toString() : null;

        ImMessage message = new ImMessage();
        message.setConversationId(conversationId);
        message.setSenderId(getUserId());
        message.setContent(content instanceof String ? (String) content : JSON.toJSONString(content));
        message.setType(type);
        message.setReplyToMessageId(replyToMessageId);
        message.setClientMsgId(clientMsgId);

        return toAjax(imMessageService.sendMessage(message));
    }

    /**
     * 撤回消息
     */
    @PreAuthorize("@ss.hasPermi('im:message:recall')")
    @Log(title = "撤回消息", businessType = BusinessType.UPDATE)
    @PutMapping("/recall/{messageId}")
    public AjaxResult recallMessage(@PathVariable Long messageId) {
        return toAjax(imMessageService.revokeMessage(messageId, getUserId()));
    }

    /**
     * 删除消息
     */
    @PreAuthorize("@ss.hasPermi('im:message:remove')")
    @Log(title = "删除消息", businessType = BusinessType.DELETE)
    @DeleteMapping("/delete/{messageId}")
    public AjaxResult deleteMessage(@PathVariable Long messageId) {
        return toAjax(imMessageService.removeById(messageId));
    }

    /**
     * 清空会话消息
     */
    @PreAuthorize("@ss.hasPermi('im:message:clear')")
    @Log(title = "清空会话消息", businessType = BusinessType.DELETE)
    @PostMapping("/clear/{sessionId}")
    public AjaxResult clearSessionMessages(@PathVariable Long sessionId) {
        return toAjax(imMessageService.deleteConversationAllMessages(sessionId));
    }

    /**
     * 标记消息已读
     */
    @PreAuthorize("@ss.hasPermi('im:message:read')")
    @Log(title = "标记消息已读", businessType = BusinessType.UPDATE)
    @PutMapping("/read/{conversationId}")
    public AjaxResult markMessageRead(@PathVariable Long conversationId, @RequestBody Map<String, Object> params) {
        Long lastReadMessageId = Long.valueOf(params.get("lastReadMessageId").toString());
        return toAjax(imMessageService.markMessageAsRead(conversationId, getUserId(), lastReadMessageId));
    }

    /**
     * 批量标记消息已读
     */
    @PreAuthorize("@ss.hasPermi('im:message:read:batch')")
    @Log(title = "批量标记消息已读", businessType = BusinessType.UPDATE)
    @PostMapping("/read/batch")
    public AjaxResult batchMarkMessagesRead(@RequestBody List<Long> messageIds) {
        return toAjax(imMessageService.updateMessageStatusBatch(messageIds, "READ"));
    }

    /**
     * 获取未读消息数
     */
    @PreAuthorize("@ss.hasPermi('im:message:unread:count')")
    @GetMapping("/unread/count")
    public AjaxResult getUnreadCount() {
        // 根据实际需求实现获取总未读消息数方法
        int totalCount = 0;
        return AjaxResult.success(totalCount);
    }

    /**
     * 获取会话未读消息数
     */
    @PreAuthorize("@ss.hasPermi('im:message:unread:conversation')")
    @GetMapping("/unread/conversation/{sessionId}")
    public AjaxResult getConversationUnreadCount(@PathVariable Long sessionId) {
        int count = imMessageService.countUnreadMessages(sessionId, getUserId(), null);
        return AjaxResult.success(count);
    }

    /**
     * 转发消息
     */
    @PreAuthorize("@ss.hasPermi('im:message:forward')")
    @Log(title = "转发消息", businessType = BusinessType.INSERT)
    @PostMapping("/forward")
    public AjaxResult forwardMessage(@RequestBody Map<String, Object> params) {
        Long messageId = Long.valueOf(params.get("messageId").toString());
        List<Long> targetSessionIds = (List<Long>) params.get("targetSessionIds");

        // 实现转发消息逻辑
        return AjaxResult.success("转发消息功能待实现");
    }

    /**
     * 回复消息
     */
    @PreAuthorize("@ss.hasPermi('im:message:reply')")
    @Log(title = "回复消息", businessType = BusinessType.INSERT)
    @PostMapping("/reply")
    public AjaxResult replyMessage(@RequestBody Map<String, Object> params) {
        Long replyToMessageId = Long.valueOf(params.get("replyToMessageId").toString());
        Long sessionId = Long.valueOf(params.get("sessionId").toString());
        String content = params.get("content").toString();
        String messageType = params.get("messageType").toString();

        ImMessage message = new ImMessage();
        message.setConversationId(sessionId);
        message.setSenderId(getUserId());
        message.setContent(content);
        message.setType(messageType);
        message.setReplyToMessageId(replyToMessageId);

        return toAjax(imMessageService.sendMessage(message));
    }

    /**
     * 获取消息详情
     */
    @PreAuthorize("@ss.hasPermi('im:message:query')")
    @GetMapping("/{messageId}")
    public AjaxResult getMessageDetail(@PathVariable Long messageId) {
        ImMessage message = imMessageService.getMessageDetail(messageId, getUserId());
        return AjaxResult.success(message);
    }

    /**
     * 查询引用消息
     */
    @PreAuthorize("@ss.hasPermi('im:message:quoted')")
    @GetMapping("/quoted/{messageId}")
    public AjaxResult getQuotedMessages(@PathVariable Long messageId) {
        // 实现获取引用消息方法
        List<ImMessage> messages = null;
        return AjaxResult.success(messages);
    }

    /**
     * 获取消息已读状态
     */
    @PreAuthorize("@ss.hasPermi('im:message:read:status')")
    @GetMapping("/read/status/{messageId}")
    public AjaxResult getMessageReadStatus(@PathVariable Long messageId) {
        // 实现获取消息已读状态方法
        Map<String, Object> readStatus = null;
        return AjaxResult.success(readStatus);
    }

    /**
     * 统计消息数量
     */
    @PreAuthorize("@ss.hasPermi('im:message:count')")
    @GetMapping("/count")
    public AjaxResult getMessageCount(@RequestParam(required = false) Long sessionId, @RequestParam(required = false) String messageType) {
        int count = imMessageService.countConversationMessages(sessionId);
        return AjaxResult.success(count);
    }

    /**
     * 获取最新消息
     */
    @PreAuthorize("@ss.hasPermi('im:message:latest')")
    @GetMapping("/latest")
    public AjaxResult getLatestMessages(@RequestParam(defaultValue = "10") int limit) {
        // 实现获取最新消息方法
        List<ImMessage> messages = null;
        return AjaxResult.success(messages);
    }

    /**
     * 获取消息类型统计
     */
    @PreAuthorize("@ss.hasPermi('im:message:statistics')")
    @GetMapping("/statistics")
    public AjaxResult getMessageTypeStatistics(@RequestParam(required = false) Long sessionId) {
        // 实现获取消息类型统计方法
        Map<String, Integer> statistics = null;
        return AjaxResult.success(statistics);
    }

    /**
     * 批量删除消息
     */
    @PreAuthorize("@ss.hasPermi('im:message:remove:batch')")
    @Log(title = "批量删除消息", businessType = BusinessType.DELETE)
    @DeleteMapping("/batch")
    public AjaxResult batchDeleteMessages(@RequestBody List<Long> messageIds) {
        return toAjax(imMessageService.removeBatchByIds(messageIds));
    }

    /**
     * 获取消息草稿
     */
    @PreAuthorize("@ss.hasPermi('im:message:draft')")
    @GetMapping("/draft/{sessionId}")
    public AjaxResult getMessageDraft(@PathVariable Long sessionId) {
        // 实现获取消息草稿方法
        String draft = null;
        return AjaxResult.success(draft);
    }

    /**
     * 保存消息草稿
     */
    @PreAuthorize("@ss.hasPermi('im:message:draft:save')")
    @Log(title = "保存消息草稿", businessType = BusinessType.UPDATE)
    @PostMapping("/draft/{sessionId}")
    public AjaxResult saveMessageDraft(@PathVariable Long sessionId, @RequestBody Map<String, Object> params) {
        String content = params.get("content").toString();
        // 实现保存消息草稿方法
        return AjaxResult.success("保存草稿功能待实现");
    }
}