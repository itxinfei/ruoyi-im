package com.ruoyi.im.controller;

import com.ruoyi.im.annotation.RateLimit;
import com.ruoyi.im.common.Result;
import com.ruoyi.im.dto.message.*;
import com.ruoyi.im.service.*;
import com.ruoyi.im.util.SecurityUtils;
import com.ruoyi.im.vo.message.ImMessageVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import java.util.List;

/**
 * 消息控制器
 * 提供消息发送、消息列表查询、消息撤回、消息转发、消息回复等核心功能
 *
 * @author ruoyi
 */
@Tag(name = "消息管理", description = "消息发送、查询、撤回、转发、回复等核心接口")
@RestController
@RequestMapping("/api/im/messages")
@Validated
public class ImMessageController {

    private static final Logger log = LoggerFactory.getLogger(ImMessageController.class);

    private final ImMessageService imMessageService;
    private final ImWebSocketBroadcastService broadcastService;
    private final ImMessageReadService messageReadService;
    private final ImConversationService conversationService;
    private final ImMessageRetryService retryService;
    private final ISensitiveWordService sensitiveWordService;

    public ImMessageController(
            ImMessageService imMessageService,
            ImWebSocketBroadcastService broadcastService,
            ImMessageReadService messageReadService,
            ImConversationService conversationService,
            ImMessageRetryService retryService,
            ISensitiveWordService sensitiveWordService) {
        this.imMessageService = imMessageService;
        this.broadcastService = broadcastService;
        this.messageReadService = messageReadService;
        this.conversationService = conversationService;
        this.retryService = retryService;
        this.sensitiveWordService = sensitiveWordService;
    }

    // ==================== 消息发送接口 ====================

    /**
     * 发送消息
     */
    @Operation(summary = "发送消息", description = "发送文本、图片、文件、语音等各类消息")
    @PostMapping
    @RateLimit(key = "send_message", time = 60, count = 300, limitType = RateLimit.LimitType.USER)
    public Result<ImMessageVO> send(@Valid @RequestBody ImMessageSendRequest request) {
        Long userId = SecurityUtils.getLoginUserId();
        ImMessageVO messageVO = imMessageService.sendMessage(request, userId);
        return Result.success(messageVO);
    }

    /**
     * 重试发送失败的消息
     */
    @Operation(summary = "重试发送消息", description = "重试发送失败的消息")
    @PostMapping("/retry/{clientMsgId}")
    public Result<ImMessageVO> retryMessage(@PathVariable String clientMsgId) {
        Long userId = SecurityUtils.getLoginUserId();

        if (!retryService.canRetry(clientMsgId)) {
            int retryCount = retryService.getRetryCount(clientMsgId);
            return Result.error(400, String.format("重试次数已达上限（%d次）", retryCount));
        }

        retryService.retrySendWithDelay(clientMsgId);
        return Result.success("正在重试发送...");
    }

    // ==================== 消息查询接口 ====================

    /**
     * 获取会话消息列表
     */
    @Operation(summary = "获取会话消息列表", description = "分页获取指定会话的消息记录")
    @GetMapping("/conversations/{conversationId}")
    public Result<List<ImMessageVO>> getMessages(
            @PathVariable @Positive(message = "会话ID必须为正数") Long conversationId,
            @RequestParam(required = false) Long lastId,
            @RequestParam(required = false, defaultValue = "20") @Min(value = 1, message = "每页最少1条") @Max(value = 100, message = "每页最多100条") Integer limit) {
        Long userId = SecurityUtils.getLoginUserId();
        List<ImMessageVO> list = imMessageService.getMessages(conversationId, userId, lastId, limit);
        return Result.success(list);
    }

    /**
     * 按类型获取会话消息
     */
    @Operation(summary = "按类型获取会话消息", description = "获取指定会话中特定类型的消息")
    @GetMapping("/conversations/{conversationId}/category/{category}")
    public Result<List<ImMessageVO>> getMessagesByCategory(
            @PathVariable @Positive(message = "会话ID必须为正数") Long conversationId,
            @PathVariable String category,
            @RequestParam(required = false) Long lastId,
            @RequestParam(required = false, defaultValue = "20") @Min(value = 1, message = "每页最少1条") @Max(value = 100, message = "每页最多100条") Integer limit) {
        Long userId = SecurityUtils.getLoginUserId();
        List<ImMessageVO> list = imMessageService.getMessagesByCategory(
                conversationId, category, userId, lastId, limit);
        return Result.success(list);
    }

    // ==================== 消息操作接口 ====================

    /**
     * 撤回消息
     */
    @Operation(summary = "撤回消息", description = "撤回已发送的消息（发送后2分钟内可撤回）")
    @PostMapping("/{messageId}/recall")
    public Result<Void> recall(@PathVariable @Positive(message = "消息ID必须为正数") Long messageId) {
        Long userId = SecurityUtils.getLoginUserId();
        imMessageService.recallMessage(messageId, userId);
        return Result.success("消息已撤回");
    }

    /**
     * 删除消息
     */
    @Operation(summary = "删除消息", description = "删除指定消息（仅删除本地记录）")
    @DeleteMapping("/{messageId}")
    public Result<Void> deleteMessage(@PathVariable @Positive(message = "消息ID必须为正数") Long messageId) {
        Long userId = SecurityUtils.getLoginUserId();
        imMessageService.deleteMessage(messageId, userId);
        return Result.success("消息已删除");
    }

    /**
     * 批量删除消息
     */
    @Operation(summary = "批量删除消息", description = "批量删除指定消息")
    @DeleteMapping("/batch")
    public Result<Void> batchDeleteMessages(@RequestBody @Valid MessagesBatchDeleteRequest request) {
        Long userId = SecurityUtils.getLoginUserId();
        // 暂时逐个删除，Service层可以后续实现批量删除优化
        for (Long messageId : request.getMessageIds()) {
            imMessageService.deleteMessage(messageId, userId);
        }
        return Result.success("批量删除成功");
    }

    /**
     * 清空会话聊天记录
     */
    @Operation(summary = "清空会话聊天记录", description = "清空指定会话中的所有消息")
    @DeleteMapping("/conversations/{conversationId}")
    public Result<Void> clearConversationMessages(@PathVariable @Positive(message = "会话ID必须为正数") Long conversationId) {
        Long userId = SecurityUtils.getLoginUserId();
        imMessageService.clearConversationMessages(conversationId, userId);
        return Result.success("聊天记录已清空");
    }

    /**
     * 编辑消息
     */
    @Operation(summary = "编辑消息", description = "编辑已发送的文本消息")
    @PutMapping("/{messageId}/edit")
    public Result<Void> edit(
            @PathVariable @Positive(message = "消息ID必须为正数") Long messageId,
            @Valid @RequestBody MessageEditRequest request) {
        Long userId = SecurityUtils.getLoginUserId();
        imMessageService.editMessage(messageId, request.getNewContent(), userId);
        return Result.success("消息已编辑");
    }

    // ==================== 消息转发回复接口 ====================

    /**
     * 转发消息
     */
    @Operation(summary = "转发消息", description = "将消息转发到其他会话或用户")
    @PostMapping("/{messageId}/forward")
    public Result<Long> forward(
            @PathVariable @Positive(message = "消息ID必须为正数") Long messageId,
            @Valid @RequestBody ImMessageForwardRequest request) {
        Long userId = SecurityUtils.getLoginUserId();
        request.setMessageId(messageId);
        Long newMessageId = imMessageService.forwardMessage(
                request.getMessageId(),
                request.getToConversationId(),
                request.getToUserId(),
                request.getContent(),
                userId);
        return Result.success("转发成功", newMessageId);
    }

    /**
     * 批量转发消息
     */
    @Operation(summary = "批量转发消息", description = "批量转发消息，支持逐条转发或合并转发")
    @PostMapping("/forward/batch")
    public Result<List<Long>> batchForward(@Valid @RequestBody ImMessageBatchForwardRequest request) {
        Long userId = SecurityUtils.getLoginUserId();
        List<Long> newMessageIds = imMessageService.batchForwardMessages(
                request.getMessageIds(),
                request.getToConversationId(),
                request.getForwardType(),
                request.getContent(),
                userId);
        return Result.success("转发成功", newMessageIds);
    }

    /**
     * 回复消息
     */
    @Operation(summary = "回复消息", description = "引用原消息进行回复")
    @PostMapping("/{messageId}/reply")
    public Result<Long> reply(
            @PathVariable @Positive(message = "消息ID必须为正数") Long messageId,
            @Valid @RequestBody ImMessageReplyRequest request) {
        Long userId = SecurityUtils.getLoginUserId();
        request.setMessageId(messageId);
        Long newMessageId = imMessageService.replyMessage(
                request.getMessageId(),
                request.getContent(),
                userId);
        return Result.success("回复成功", newMessageId);
    }

    // ==================== 消息已读标记接口 ====================

    /**
     * 标记消息已读
     */
    @Operation(summary = "标记消息已读", description = "批量标记指定消息为已读状态")
    @PutMapping("/mark-read")
    public Result<Void> markAsRead(@Valid @RequestBody ImMessageMarkReadRequest request) {
        Long userId = SecurityUtils.getLoginUserId();
        imMessageService.markAsRead(request.getConversationId(), userId, request.getMessageIds());
        return Result.success("已标记为已读");
    }

    /**
     * 标记会话消息已读
     */
    @Operation(summary = "标记会话已读", description = "将会话中指定消息之前的所有消息标记为已读")
    @PutMapping("/conversations/{conversationId}/read")
    public Result<Void> markConversationRead(
            @PathVariable @Positive(message = "会话ID必须为正数") Long conversationId,
            @RequestParam(required = false) Long lastReadMessageId) {
        Long userId = SecurityUtils.getLoginUserId();
        messageReadService.markConversationAsRead(conversationId, lastReadMessageId, userId);
        return Result.success("已标记为已读");
    }

    // ==================== 敏感词检测 ====================

    /**
     * 检测文本中的敏感词
     */
    @Operation(summary = "检测敏感词", description = "检测文本中是否包含敏感词")
    @PostMapping("/sensitive-word/check")
    public Result<java.util.Map<String, Object>> checkSensitiveWord(@RequestBody @Valid MessageSensitiveWordCheckRequest request) {
        boolean contains = sensitiveWordService.containsSensitiveWord(request.getText());
        java.util.Set<String> words = sensitiveWordService.detectSensitiveWords(request.getText());
        
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("contains", contains);
        result.put("words", words);
        result.put("count", words.size());
        
        return Result.success(result);
    }

    /**
     * 过滤敏感词
     */
    @Operation(summary = "过滤敏感词", description = "过滤文本中的敏感词")
    @PostMapping("/sensitive-word/filter")
    public Result<java.util.Map<String, String>> filterSensitiveWord(@RequestBody @Valid MessageSensitiveWordFilterRequest request) {
        String filtered = request.getReplacement() != null 
            ? sensitiveWordService.filter(request.getText(), request.getReplacement())
            : sensitiveWordService.filter(request.getText());
        
        java.util.Map<String, String> result = new java.util.HashMap<>();
        result.put("original", request.getText());
        result.put("filtered", filtered);
        
        return Result.success(result);
    }
}