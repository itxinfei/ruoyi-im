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
 * 表情反应相关接口请使用: {@link ImMessageReactionController}
 * 已读回执相关接口请使用: {@link ImMessageReceiptController}
 * 消息搜索相关接口请使用: {@link ImMessageSearchController}
 * 消息同步相关接口请使用: {@link ImMessageSyncController}
 *
 * @author ruoyi
 */
@Tag(name = "消息管理", description = "消息发送、查询、撤回、转发、回复等核心接口")
@RestController
@RequestMapping("/api/im/message")
@Validated
public class ImMessageController {

    private static final Logger log = LoggerFactory.getLogger(ImMessageController.class);

    private final ImMessageService imMessageService;
    private final ImWebSocketBroadcastService broadcastService;
    private final ImMessageReadService messageReadService;
    private final ImConversationService conversationService;
    private final ImMessageRetryService retryService;

    public ImMessageController(
            ImMessageService imMessageService,
            ImWebSocketBroadcastService broadcastService,
            ImMessageReadService messageReadService,
            ImConversationService conversationService,
            ImMessageRetryService retryService) {
        this.imMessageService = imMessageService;
        this.broadcastService = broadcastService;
        this.messageReadService = messageReadService;
        this.conversationService = conversationService;
        this.retryService = retryService;
    }

    // ==================== 消息发送接口 ====================

    /**
     * 发送消息
     * 发送文本、图片、文件、语音等各类消息
     *
     * @param request 消息发送请求参数
     * @return 消息发送结果，包含消息ID、发送时间等信息
     * @apiNote 支持的消息类型：text、image、file、voice、video、location、card等
     */
    @Operation(summary = "发送消息", description = "发送文本、图片、文件、语音等各类消息")
    @PostMapping("/send")
    @RateLimit(key = "send_message", time = 60, count = 300, limitType = RateLimit.LimitType.USER)
    public Result<ImMessageVO> send(@Valid @RequestBody ImMessageSendRequest request) {
        Long userId = SecurityUtils.getLoginUserId();
        log.info("发送消息: userId={}, conversationId={}, msgType={}",
                userId, request.getConversationId(), request.getType());
        ImMessageVO messageVO = imMessageService.sendMessage(request, userId);
        log.info("消息发送成功: userId={}, messageId={}, conversationId={}",
                userId, messageVO.getId(), request.getConversationId());
        return Result.success(messageVO);
    }

    /**
     * 重试发送失败的消息
     * 支持自动重试（最多3次，采用指数退避策略：1s, 2s, 4s）
     *
     * @param clientMsgId 客户端消息ID
     * @return 重试结果
     */
    @Operation(summary = "重试发送消息", description = "重试发送失败的消息，最多重试3次，采用指数退避策略（1s, 2s, 4s）")
    @PostMapping("/retry/{clientMsgId}")
    public Result<ImMessageVO> retryMessage(@PathVariable String clientMsgId) {
        Long userId = SecurityUtils.getLoginUserId();

        // 检查是否可以重试
        if (!retryService.canRetry(clientMsgId)) {
            int retryCount = retryService.getRetryCount(clientMsgId);
            return Result.error(400, String.format("重试次数已达上限（%d次）", retryCount));
        }

        // 异步重试发送（带延迟）
        retryService.retrySendWithDelay(clientMsgId);

        log.info("消息重试任务已提交: clientMsgId={}, userId={}", clientMsgId, userId);

        // 返回提示信息（实际发送结果是异步的）
        return Result.success("正在重试发送...");
    }

    // ==================== 消息查询接口 ====================

    /**
     * 获取会话消息列表
     * 分页获取指定会话的消息记录
     *
     * @param conversationId 会话ID
     * @param lastId 上次查询的最后消息ID，用于分页
     * @param limit 每页数量，默认20条，最多100条
     * @return 消息列表
     * @apiNote 消息按发送时间倒序排列
     */
    @Operation(summary = "获取会话消息列表", description = "分页获取指定会话的消息记录")
    @GetMapping("/list/{conversationId}")
    public Result<List<ImMessageVO>> getMessages(
            @PathVariable @Positive(message = "会话ID必须为正数") Long conversationId,
            @RequestParam(required = false) Long lastId,
            @RequestParam(required = false, defaultValue = "20") @Min(value = 1, message = "每页最少1条") @Max(value = 100, message = "每页最多100条") Integer limit) {
        Long userId = SecurityUtils.getLoginUserId();
        log.info("getMessages API - 当前登录用户 userId={}, conversationId={}", userId, conversationId);
        List<ImMessageVO> list = imMessageService.getMessages(conversationId, userId, lastId, limit);
        return Result.success(list);
    }

    /**
     * 按类型获取会话消息
     * 获取指定会话中特定类型的消息（图片、文件、链接等）
     *
     * @param conversationId 会话ID
     * @param category 消息类型分类（all/image/file/link/voice/video）
     * @param lastId 上次查询的最后消息ID，用于分页
     * @param limit 每页数量
     * @return 消息列表
     * @apiNote 用于聊天记录面板按类型筛选消息
     */
    @Operation(summary = "按类型获取会话消息", description = "获取指定会话中特定类型的消息")
    @GetMapping("/{conversationId}/category/{category}")
    public Result<List<ImMessageVO>> getMessagesByCategory(
            @PathVariable @Positive(message = "会话ID必须为正数") Long conversationId,
            @PathVariable String category,
            @RequestParam(required = false) Long lastId,
            @RequestParam(required = false, defaultValue = "20") @Min(value = 1, message = "每页最少1条") @Max(value = 100, message = "每页最多100条") Integer limit) {
        Long userId = SecurityUtils.getLoginUserId();

        try {
            List<ImMessageVO> list = imMessageService.getMessagesByCategory(
                    conversationId, category, userId, lastId, limit);
            return Result.success(list);
        } catch (Exception e) {
            log.error("按类型获取消息失败: conversationId={}, category={}", conversationId, category, e);
            return Result.fail("获取消息失败");
        }
    }

    // ==================== 消息操作接口 ====================

    /**
     * 撤回消息
     * 撤回已发送的消息（发送后2分钟内可撤回）
     *
     * @param messageId 消息ID
     * @return 撤回结果
     * @apiNote 撤回后消息内容将被替换为"消息已撤回"提示
     */
    @Operation(summary = "撤回消息", description = "撤回已发送的消息（发送后2分钟内可撤回）")
    @DeleteMapping("/{messageId}/recall")
    public Result<Void> recall(@PathVariable @Positive(message = "消息ID必须为正数") Long messageId) {
        Long userId = SecurityUtils.getLoginUserId();
        log.info("撤回消息: userId={}, messageId={}", userId, messageId);
        imMessageService.recallMessage(messageId, userId);
        log.info("消息撤回成功: userId={}, messageId={}", userId, messageId);
        return Result.success("消息已撤回");
    }

    /**
     * 删除消息
     * 删除指定消息（仅删除本地记录，对方不受影响）
     *
     * @param messageId 消息ID
     * @return 删除结果
     */
    @Operation(summary = "删除消息", description = "删除指定消息（仅删除本地记录）")
    @DeleteMapping("/{messageId}")
    public Result<Void> deleteMessage(@PathVariable @Positive(message = "消息ID必须为正数") Long messageId) {
        Long userId = SecurityUtils.getLoginUserId();
        imMessageService.deleteMessage(messageId, userId);
        return Result.success("消息已删除");
    }

    /**
     * 清空会话聊天记录
     * 删除指定会话中的所有消息
     *
     * @param conversationId 会话ID
     * @return 操作结果
     */
    @Operation(summary = "清空会话聊天记录", description = "清空指定会话中的所有消息")
    @DeleteMapping("/clear/{conversationId}")
    public Result<Void> clearConversationMessages(@PathVariable @Positive(message = "会话ID必须为正数") Long conversationId) {
        Long userId = SecurityUtils.getLoginUserId();

        try {
            // 验证用户是否有权限访问该会话
            imMessageService.clearConversationMessages(conversationId, userId);
            log.info("清空会话聊天记录成功: conversationId={}, userId={}", conversationId, userId);
            return Result.success("聊天记录已清空");
        } catch (Exception e) {
            log.error("清空会话聊天记录失败: conversationId={}, userId={}", conversationId, userId, e);
            return Result.fail("清空聊天记录失败");
        }
    }

    /**
     * 编辑消息
     * 编辑已发送的文本消息
     *
     * @param messageId 消息ID
     * @param request 编辑请求参数
     * @return 编辑结果
     * @apiNote 仅文本消息可编辑，编辑后会显示"消息已编辑"标记
     */
    @Operation(summary = "编辑消息", description = "编辑已发送的文本消息")
    @PutMapping("/{messageId}/edit")
    public Result<Void> edit(
            @PathVariable @Positive(message = "消息ID必须为正数") Long messageId,
            @Valid @RequestBody MessageEditRequest request) {
        Long userId = SecurityUtils.getLoginUserId();
        log.info("编辑消息: userId={}, messageId={}", userId, messageId);
        imMessageService.editMessage(messageId, request.getNewContent(), userId);
        log.info("消息编辑成功: userId={}, messageId={}", userId, messageId);
        return Result.success("消息已编辑");
    }

    // ==================== 消息转发回复接口 ====================

    /**
     * 转发消息
     * 将消息转发到其他会话或用户
     *
     * @param request 转发请求参数，包含原消息ID、目标会话ID、目标用户ID等
     * @return 转发结果，包含新消息ID
     * @apiNote 转发时会创建新消息，原消息内容会被复制到新消息中
     */
    @Operation(summary = "转发消息", description = "将消息转发到其他会话或用户")
    @PostMapping("/forward")
    public Result<Long> forward(@Valid @RequestBody ImMessageForwardRequest request) {
        Long userId = SecurityUtils.getLoginUserId();
        log.info("转发消息: userId={}, messageId={}, toConversationId={}",
                userId, request.getMessageId(), request.getToConversationId());
        Long newMessageId = imMessageService.forwardMessage(
                request.getMessageId(),
                request.getToConversationId(),
                request.getToUserId(),
                request.getContent(),
                userId);
        log.info("消息转发成功: userId={}, originalMessageId={}, newMessageId={}",
                userId, request.getMessageId(), newMessageId);
        return Result.success("转发成功", newMessageId);
    }

    /**
     * 批量转发消息
     * 支持逐条转发或合并转发多条消息
     *
     * @param request 批量转发请求参数
     * @return 转发结果，包含新消息ID列表
     * @apiNote batch=逐条转发, combine=合并转发为一条聊天记录消息
     */
    @Operation(summary = "批量转发消息", description = "批量转发消息，支持逐条转发或合并转发")
    @PostMapping("/forward/batch")
    public Result<List<Long>> batchForward(@Valid @RequestBody ImMessageBatchForwardRequest request) {
        Long userId = SecurityUtils.getLoginUserId();
        log.info("批量转发消息: userId={}, messageIds={}, toConversationId={}, forwardType={}",
                userId, request.getMessageIds().size(), request.getToConversationId(), request.getForwardType());
        List<Long> newMessageIds = imMessageService.batchForwardMessages(
                request.getMessageIds(),
                request.getToConversationId(),
                request.getForwardType(),
                request.getContent(),
                userId);
        log.info("批量消息转发成功: userId={}, count={}", userId, newMessageIds.size());
        return Result.success("转发成功", newMessageIds);
    }

    /**
     * 回复消息
     * 引用原消息进行回复
     *
     * @param request 回复请求参数，包含原消息ID和回复内容
     * @return 回复结果，包含新消息ID
     * @apiNote 回复消息会关联原消息ID，通过parentId字段建立消息引用关系
     */
    @Operation(summary = "回复消息", description = "引用原消息进行回复")
    @PostMapping("/reply")
    public Result<Long> reply(@Valid @RequestBody ImMessageReplyRequest request) {
        Long userId = SecurityUtils.getLoginUserId();
        log.info("回复消息: userId={}, parentMessageId={}", userId, request.getMessageId());
        Long newMessageId = imMessageService.replyMessage(
                request.getMessageId(),
                request.getContent(),
                userId);
        log.info("消息回复成功: userId={}, parentMessageId={}, newMessageId={}",
                userId, request.getMessageId(), newMessageId);
        return Result.success("回复成功", newMessageId);
    }

    // ==================== 消息已读标记接口 ====================

    /**
     * 标记消息已读
     * 批量标记指定消息为已读状态，并更新会话未读消息数
     *
     * @param request 包含conversationId和messageIds的请求数据
     * @return 标记结果
     * @apiNote 标记已读后会更新会话的未读消息数，并通过WebSocket推送已读回执给发送方
     */
    @Operation(summary = "标记消息已读", description = "批量标记指定消息为已读状态")
    @PutMapping("/mark-read")
    public Result<Void> markAsRead(@Valid @RequestBody ImMessageMarkReadRequest request) {
        Long userId = SecurityUtils.getLoginUserId();
        imMessageService.markAsRead(request.getConversationId(), userId, request.getMessageIds());
        return Result.success("已标记为已读");
    }

    /**
     * 标记会话消息已读（兼容前端API）
     * 将会话中指定消息ID之前的所有消息标记为已读
     *
     * @param conversationId    会话ID
     * @param lastReadMessageId 最后已读消息ID（该消息之前的所有消息都标记为已读）
     * @return 操作结果
     */
    @Operation(summary = "标记会话已读", description = "将会话中指定消息之前的所有消息标记为已读")
    @PutMapping("/read")
    public Result<Void> markConversationRead(
            @RequestParam @Positive(message = "会话ID必须为正数") Long conversationId,
            @RequestParam(required = false) Long lastReadMessageId) {
        Long userId = SecurityUtils.getLoginUserId();
        // 通过 Service 层处理会话已读标记，符合分层架构
        messageReadService.markConversationAsRead(conversationId, lastReadMessageId, userId);
        return Result.success("已标记为已读");
    }
}
