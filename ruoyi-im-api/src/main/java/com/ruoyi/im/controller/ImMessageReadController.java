package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.service.ImMessageReadService;
import com.ruoyi.im.util.SecurityUtils;
import com.ruoyi.im.vo.message.ImMessageReadDetailVO;
import com.ruoyi.im.vo.message.ImMessageReadStatusVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 消息已读回执控制器
 *
 * @author ruoyi
 */
@Tag(name = "消息已读回执", description = "消息已读状态查询、标记已读等接口")
@RestController
@RequestMapping("/api/im/message/read")
public class ImMessageReadController {

    private static final Logger log = LoggerFactory.getLogger(ImMessageReadController.class);

    @Autowired
    private ImMessageReadService messageReadService;

    /**
     * 标记单条消息为已读
     *
     * @param conversationId 会话ID
     * @param messageId      消息ID
     * @return 操作结果
     */
    @Operation(summary = "标记消息已读", description = "标记单条消息为已读状态")
    @PostMapping("/{messageId}")
    public Result<Void> markAsRead(
            @PathVariable Long messageId,
            @RequestParam Long conversationId) {
        Long userId = SecurityUtils.getLoginUserId();
        messageReadService.markMessageAsRead(conversationId, messageId, userId);
        return Result.success("已标记为已读");
    }

    /**
     * 批量标记消息为已读
     *
     * @param data 请求数据
     * @return 操作结果
     */
    @Operation(summary = "批量标记已读", description = "批量标记多条消息为已读状态")
    @PostMapping("/batch")
    public Result<Void> markBatchAsRead(
            @RequestBody java.util.Map<String, Object> data) {
        Long userId = SecurityUtils.getLoginUserId();
        Long conversationId = data.get("conversationId") != null ?
            Long.valueOf(data.get("conversationId").toString()) : null;
        @SuppressWarnings("unchecked")
        List<Long> messageIds = (List<Long>) data.get("messageIds");

        messageReadService.markMessagesAsRead(conversationId, messageIds, userId);
        return Result.success("已批量标记为已读");
    }

    /**
     * 标记会话所有消息为已读
     *
     * @param conversationId 会话ID
     * @param upToMessageId  标记到此消息ID为止
     * @return 操作结果
     */
    @Operation(summary = "标记会话已读", description = "将会话中所有消息标记为已读")
    @PostMapping("/conversation/{conversationId}")
    public Result<Void> markConversationAsRead(
            @PathVariable Long conversationId,
            @RequestParam Long upToMessageId) {
        Long userId = SecurityUtils.getLoginUserId();
        messageReadService.markConversationAsRead(conversationId, upToMessageId, userId);
        return Result.success("会话已标记为已读");
    }

    /**
     * 获取消息已读状态
     *
     * @param messageId 消息ID
     * @return 已读状态
     */
    @Operation(summary = "获取消息已读状态", description = "获取消息的已读人数和百分比")
    @GetMapping("/status/{messageId}")
    public Result<ImMessageReadStatusVO> getReadStatus(
            @PathVariable Long messageId) {
        Long userId = SecurityUtils.getLoginUserId();
        ImMessageReadStatusVO status = messageReadService.getMessageReadStatus(messageId, userId);
        return Result.success(status);
    }

    /**
     * 获取消息已读详情
     *
     * @param messageId 消息ID
     * @return 已读详情
     */
    @Operation(summary = "获取消息已读详情", description = "获取消息的已读和未读用户列表")
    @GetMapping("/detail/{messageId}")
    public Result<ImMessageReadDetailVO> getReadDetail(
            @PathVariable Long messageId) {
        Long userId = SecurityUtils.getLoginUserId();
        ImMessageReadDetailVO detail = messageReadService.getMessageReadDetail(messageId, userId);
        return Result.success(detail);
    }

    /**
     * 获取会话消息已读状态列表
     *
     * @param conversationId 会话ID
     * @return 已读状态列表
     */
    @Operation(summary = "获取会话消息已读状态", description = "获取会话中当前用户发送的消息的已读状态")
    @GetMapping("/conversation/{conversationId}")
    public Result<List<ImMessageReadStatusVO>> getConversationReadStatus(
            @PathVariable Long conversationId) {
        Long userId = SecurityUtils.getLoginUserId();
        List<ImMessageReadStatusVO> statusList = messageReadService.getConversationMessageReadStatus(
            conversationId, userId);
        return Result.success(statusList);
    }

    /**
     * 标记会话已读（PUT接口，兼容前端）
     *
     * @param conversationId 会话ID
     * @return 操作结果
     */
    @Operation(summary = "标记会话已读", description = "将会话中所有消息标记为已读（PUT接口）")
    @PutMapping("/conversation")
    public Result<Void> markConversationRead(
            @RequestParam Long conversationId) {
        Long userId = SecurityUtils.getLoginUserId();
        try {
            messageReadService.markConversationAsRead(conversationId, null, userId);
            return Result.success("会话已标记为已读");
        } catch (Exception e) {
            log.error("标记会话已读失败: conversationId={}, userId={}", conversationId, userId, e);
            return Result.error("标记会话已读失败: " + e.getMessage());
        }
    }

    /**
     * 撤回已读回执
     *
     * @param messageId 消息ID
     * @return 操作结果
     */
    @Operation(summary = "撤回已读回执", description = "删除某条消息的已读记录")
    @DeleteMapping("/{messageId}")
    public Result<Void> revokeReadReceipt(
            @PathVariable Long messageId) {
        Long userId = SecurityUtils.getLoginUserId();
        messageReadService.revokeReadReceipt(messageId, userId);
        return Result.success("已撤回已读回执");
    }
}
