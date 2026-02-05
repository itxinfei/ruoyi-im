package com.ruoyi.im.controller;

import com.ruoyi.im.annotation.RateLimit;
import com.ruoyi.im.common.Result;
import com.ruoyi.im.domain.ImMessageMention;
import com.ruoyi.im.dto.message.ImMessageBatchReadStatusRequest;
import com.ruoyi.im.dto.message.ImMessageBatchForwardRequest;
import com.ruoyi.im.dto.message.ImMessageForwardRequest;
import com.ruoyi.im.dto.message.ImMessageMarkReadRequest;
import com.ruoyi.im.dto.message.ImMessageReplyRequest;
import com.ruoyi.im.dto.message.ImMessageSendRequest;
import com.ruoyi.im.dto.message.ImMessageSearchRequest;
import com.ruoyi.im.dto.message.MessageEditRequest;
import com.ruoyi.im.dto.reaction.ImMessageReactionAddRequest;
import com.ruoyi.im.service.*;
import com.ruoyi.im.service.ImMessageSyncService;
import com.ruoyi.im.util.SecurityUtils;
import com.ruoyi.im.vo.message.ImMessageSearchResultVO;
import com.ruoyi.im.vo.message.ImMessageVO;
import com.ruoyi.im.vo.message.SyncMessageResponse;
import com.ruoyi.im.vo.reaction.ImMessageReactionVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 消息控制器
 * 提供消息发送、消息列表查询、消息撤回、消息已读标记、表情反应等功能
 *
 * @author ruoyi
 */
@Tag(name = "消息管理", description = "消息发送、查询、撤回、转发、回复、表情反应等接口")
@RestController
@RequestMapping("/api/im/message")
public class ImMessageController {

    private static final Logger log = LoggerFactory.getLogger(ImMessageController.class);

    private final ImMessageService imMessageService;
    private final ImMessageReactionService reactionService;
    private final ImMessageMentionService mentionService;
    private final ImWebSocketBroadcastService broadcastService;
    private final ImMessageReadService messageReadService;
    private final ImConversationService conversationService;
    private final ImMessageRetryService retryService;
    private final ImMessageSyncService syncService;

    public ImMessageController(
            ImMessageService imMessageService,
            ImMessageReactionService reactionService,
            ImMessageMentionService mentionService,
            ImWebSocketBroadcastService broadcastService,
            ImMessageReadService messageReadService,
            ImConversationService conversationService,
            ImMessageRetryService retryService,
            ImMessageSyncService syncService) {
        this.imMessageService = imMessageService;
        this.reactionService = reactionService;
        this.mentionService = mentionService;
        this.broadcastService = broadcastService;
        this.messageReadService = messageReadService;
        this.conversationService = conversationService;
        this.retryService = retryService;
        this.syncService = syncService;
    }

    @PostMapping("/send")
    @RateLimit(key = "send_message", time = 60, count = 300, limitType = RateLimit.LimitType.USER)
    public Result<ImMessageVO> send(@Valid @RequestBody ImMessageSendRequest request) {
        Long userId = SecurityUtils.getLoginUserId();
        ImMessageVO messageVO = imMessageService.sendMessage(request, userId);
        return Result.success(messageVO);
    }

    /**
     * 重试发送失败的消息
     * 支持自动重试（最多3次，采用指数退避策略：1s, 2s, 4s）
     *
     * @param clientMsgId 客户端消息ID
     * @return 重试结果
     */
    @PostMapping("/retry/{clientMsgId}")
    @Operation(summary = "重试发送消息", description = "重试发送失败的消息，最多重试3次，采用指数退避策略（1s, 2s, 4s）")
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

    @GetMapping("/list/{conversationId}")
    public Result<List<ImMessageVO>> getMessages(
            @PathVariable Long conversationId,
            @RequestParam(required = false) Long lastId,
            @RequestParam(required = false, defaultValue = "20") Integer limit) {
        Long userId = SecurityUtils.getLoginUserId();
        log.info("getMessages API - 当前登录用户 userId={}, conversationId={}", userId, conversationId);
        List<ImMessageVO> list = imMessageService.getMessages(conversationId, userId, lastId, limit);
        return Result.success(list);
    }

    @DeleteMapping("/{messageId}/recall")
    public Result<Void> recall(@PathVariable Long messageId) {
        Long userId = SecurityUtils.getLoginUserId();
        imMessageService.recallMessage(messageId, userId);
        return Result.success("消息已撤回");
    }

    @DeleteMapping("/{messageId}")
    public Result<Void> deleteMessage(@PathVariable Long messageId) {
        Long userId = SecurityUtils.getLoginUserId();
        imMessageService.deleteMessage(messageId, userId);
        return Result.success("消息已删除");
    }

    @PutMapping("/{messageId}/edit")
    public Result<Void> edit(
            @PathVariable Long messageId,
            @RequestBody MessageEditRequest request) {
        Long userId = SecurityUtils.getLoginUserId();
        imMessageService.editMessage(messageId, request.getNewContent(), userId);
        return Result.success("消息已编辑");
    }

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
            @RequestParam Long conversationId,
            @RequestParam(required = false) Long lastReadMessageId) {
        Long userId = SecurityUtils.getLoginUserId();
        // 通过 Service 层处理会话已读标记，符合分层架构
        messageReadService.markConversationAsRead(conversationId, lastReadMessageId, userId);
        return Result.success("已标记为已读");
    }

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
     * 支持逐条转发或合并转发多条消息
     *
     * @param request 批量转发请求参数
     * @return 转发结果，包含新消息ID列表
     * @apiNote batch=逐条转发, combine=合并转发为一条聊天记录消息
     */
    @Operation(summary = "批量转发消息", description = "批量转发消息，支持逐条转发或合并转发")
    @PostMapping("/forward/batch")
    public Result<java.util.List<Long>> batchForward(@Valid @RequestBody com.ruoyi.im.dto.message.ImMessageBatchForwardRequest request) {
        Long userId = SecurityUtils.getLoginUserId();
        java.util.List<Long> newMessageIds = imMessageService.batchForwardMessages(
                request.getMessageIds(),
                request.getToConversationId(),
                request.getForwardType(),
                request.getContent(),
                userId);
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
        Long newMessageId = imMessageService.replyMessage(
                request.getMessageId(),
                request.getContent(),
                userId);
        return Result.success("回复成功", newMessageId);
    }

    /**
     * 添加消息表情反应
     * 对消息添加emoji表情反应（类似微信点赞、钉钉表情回复）
     * 再次使用相同表情会取消反应
     *
     * @param request 反应请求参数
     * @return 反应结果
     * @apiNote 支持多个用户对同一条消息添加不同表情反应
     */
    @Operation(summary = "添加表情反应", description = "对消息添加emoji表情反应")
    @PostMapping("/{messageId}/reaction")
    public Result<ImMessageReactionVO> addReaction(
            @PathVariable Long messageId,
            @Valid @RequestBody ImMessageReactionAddRequest request) {
        Long userId = SecurityUtils.getLoginUserId();
        request.setMessageId(messageId);
        ImMessageReactionVO result = reactionService.addReaction(request, userId);

        if (result == null) {
            return Result.success("已取消反应", (ImMessageReactionVO) null);
        }

        // 通过WebSocket推送反应更新通知
        broadcastService.broadcastReactionUpdate(result.getConversationId(), messageId, userId,
                request.getEmoji(), "add");

        return Result.success("反应成功", result);
    }

    /**
     * 删除消息表情反应
     * 取消对消息的emoji表情反应
     *
     * @param messageId 消息ID
     * @return 删除结果
     */
    @Operation(summary = "删除表情反应", description = "取消对消息的emoji表情反应")
    @DeleteMapping("/{messageId}/reaction")
    public Result<Void> removeReaction(@PathVariable Long messageId) {
        Long userId = SecurityUtils.getLoginUserId();
        reactionService.removeReaction(messageId, userId);

        // 通过WebSocket推送反应更新通知
        ImMessageVO message = imMessageService.getMessageById(messageId);
        if (message != null) {
            broadcastService.broadcastReactionUpdate(message.getConversationId(), messageId, userId, null, "remove");
        }

        return Result.success("已取消反应");
    }

    /**
     * 获取消息的表情反应列表
     * 获取指定消息的所有表情反应
     *
     * @param messageId 消息ID
     * @return 反应列表
     */
    @Operation(summary = "获取表情反应列表", description = "获取消息的所有表情反应")
    @GetMapping("/{messageId}/reactions")
    public Result<List<ImMessageReactionVO>> getMessageReactions(@PathVariable Long messageId) {
        Long userId = SecurityUtils.getLoginUserId();
        List<ImMessageReactionVO> reactions = reactionService.getMessageReactions(messageId, userId);
        return Result.success(reactions);
    }

    /**
     * 获取消息的表情反应统计
     * 获取消息的表情反应统计信息
     *
     * @param messageId 消息ID
     * @return 反应统计列表
     */
    @Operation(summary = "获取表情反应统计", description = "获取消息的表情反应统计")
    @GetMapping("/{messageId}/reactions/stats")
    public Result<List<ImMessageReactionVO>> getReactionStats(@PathVariable Long messageId) {
        Long userId = SecurityUtils.getLoginUserId();
        List<ImMessageReactionVO> stats = reactionService.getReactionStats(messageId, userId);
        return Result.success(stats);
    }

    // ==================== @提及功能接口 ====================

    /**
     * 获取用户未读的@提及列表
     * 查询当前用户被@的所有未读消息
     *
     * @return 未读@提及列表
     * @apiNote 返回包含消息详情、@发送者信息的列表
     */
    @Operation(summary = "获取未读@提及", description = "获取当前用户被@的所有未读消息")
    @GetMapping("/mention/unread")
    public Result<List<ImMessageMention>> getUnreadMentions() {
        Long userId = SecurityUtils.getLoginUserId();
        List<ImMessageMention> mentions = mentionService.getUnreadMentions(userId);
        return Result.success(mentions);
    }

    /**
     * 获取用户未读的@提及数量
     * 获取当前用户被@的未读消息总数
     *
     * @return 未读数量
     * @apiNote 用于在界面上显示@提醒角标
     */
    @Operation(summary = "获取未读@提及数量", description = "获取当前用户被@的未读消息总数")
    @GetMapping("/mention/unread/count")
    public Result<Integer> getUnreadMentionCount() {
        Long userId = SecurityUtils.getLoginUserId();
        int count = mentionService.getUnreadMentionCount(userId);
        return Result.success(count);
    }

    /**
     * 标记@提及为已读
     * 标记指定消息的@提及为已读状态
     *
     * @param messageId 消息ID
     * @return 标记结果
     * @apiNote 标记已读后该消息不会在未读@提及列表中显示
     */
    @Operation(summary = "标记@提及已读", description = "标记指定消息的@提及为已读状态")
    @PutMapping("/{messageId}/mention/read")
    public Result<Void> markMentionAsRead(@PathVariable Long messageId) {
        Long userId = SecurityUtils.getLoginUserId();
        mentionService.markAsRead(messageId, userId);
        return Result.success("已标记为已读");
    }

    /**
     * 批量标记@提及为已读
     * 批量标记多条@提及记录为已读状态
     *
     * @param mentionIds 提及ID列表
     * @return 标记结果
     * @apiNote 用于一键清空所有未读@提及
     */
    @Operation(summary = "批量标记@提及已读", description = "批量标记多条@提及记录为已读状态")
    @PutMapping("/mention/read/batch")
    public Result<Void> batchMarkMentionsAsRead(@RequestBody List<Long> mentionIds) {
        mentionService.batchMarkAsRead(mentionIds);
        return Result.success("已批量标记为已读");
    }

    // ==================== 消息搜索接口 ====================

    /**
     * 搜索消息
     * 支持关键词搜索、时间范围筛选、消息类型筛选等多种搜索方式
     *
     * @param request 搜索请求参数
     * @return 搜索结果
     * @apiNote 支持模糊搜索、精确匹配、时间范围过滤等功能
     */
    @Operation(summary = "搜索消息", description = "支持关键词搜索、时间范围筛选、消息类型筛选等")
    @PostMapping("/search")
    public Result<ImMessageSearchResultVO> searchMessages(
            @RequestBody ImMessageSearchRequest request) {
        Long userId = SecurityUtils.getLoginUserId();
        ImMessageSearchResultVO result = imMessageService.searchMessages(
                request.getConversationId(),
                request.getKeyword(),
                request.getMessageType(),
                request.getSenderId(),
                request.getStartTime(),
                request.getEndTime(),
                request.getPageNum(),
                request.getPageSize(),
                request.getIncludeRevoked(),
                request.getExactMatch(),
                userId);
        return Result.success(result);
    }

    // ==================== 消息已读回执功能 ====================

    /**
     * 获取会话未读消息数
     *
     * @param conversationId 会话ID
     * @return 未读消息数
     */
    @Operation(summary = "获取会话未读消息数", description = "获取指定会话中当前用户的未读消息数量")
    @GetMapping("/unread/count/{conversationId}")
    public Result<Integer> getUnreadCount(@PathVariable Long conversationId) {
        Long userId = SecurityUtils.getLoginUserId();

        try {
            Integer unreadCount = conversationService.getUnreadCount(conversationId, userId);
            return Result.success(unreadCount != null ? unreadCount : 0);
        } catch (Exception e) {
            log.error("获取未读消息数失败: conversationId={}", conversationId, e);
            return Result.fail("获取未读消息数失败");
        }
    }

    /**
     * 获取会话已读状态
     *
     * @param conversationId 会话ID
     * @param messageId      消息ID
     * @return 已读用户列表
     */
    @Operation(summary = "获取消息已读状态", description = "获取指定消息的已读用户列表")
    @GetMapping("/read/status/{conversationId}/{messageId}")
    public Result<List<Map<String, Object>>> getReadStatus(
            @PathVariable Long conversationId,
            @PathVariable Long messageId) {
        Long userId = SecurityUtils.getLoginUserId();

        try {
            List<Map<String, Object>> readUsers = conversationService.getReadStatus(conversationId, messageId);
            return Result.success(readUsers);
        } catch (Exception e) {
            log.error("获取已读状态失败: conversationId={}, messageId={}", conversationId, messageId, e);
            return Result.fail("获取已读状态失败");
        }
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
    public Result<Void> clearConversationMessages(@PathVariable Long conversationId) {
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
            @PathVariable Long conversationId,
            @PathVariable String category,
            @RequestParam(required = false) Long lastId,
            @RequestParam(required = false, defaultValue = "20") Integer limit) {
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

    // ==================== 消息同步功能（参考野火IM）====================

    /**
     * 同步消息
     * 获取从lastSyncTime之后的新消息，支持断线重连后的消息补发
     *
     * @param lastSyncTime 上次同步时间戳（毫秒），首次同步传0
     * @param deviceId 设备ID（客户端生成，如：web_abc123）
     * @param limit 单次同步最大条数（默认100，最大500）
     * @return 同步响应（包含消息列表和新的同步点）
     * @apiNote 客户端需要在WebSocket连接成功后调用此接口同步消息
     */
    @Operation(summary = "同步消息", description = "获取从指定时间之后的所有新消息，支持断线重连补发")
    @GetMapping("/sync")
    public Result<SyncMessageResponse> syncMessages(
            @RequestParam(required = false) Long lastSyncTime,
            @RequestParam String deviceId,
            @RequestParam(required = false, defaultValue = "100") Integer limit) {
        Long userId = SecurityUtils.getLoginUserId();

        // 参数校验
        if (deviceId == null || deviceId.trim().isEmpty()) {
            return Result.fail("设备ID不能为空");
        }

        try {
            SyncMessageResponse response = syncService.syncMessages(userId, deviceId, lastSyncTime, limit);

            // 更新同步点
            if (response.getNewSyncTime() != null) {
                syncService.updateSyncPoint(userId, deviceId, response.getNewSyncTime(), response.getLastMessageId());
            }

            log.info("消息同步成功: userId={}, deviceId={}, count={}", userId, deviceId, response.getTotalCount());
            return Result.success(response);
        } catch (Exception e) {
            log.error("消息同步失败: userId={}, deviceId={}", userId, deviceId, e);
            return Result.fail("消息同步失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户同步点
     * 查询当前用户的所有设备同步点信息
     *
     * @return 同步点列表
     */
    @Operation(summary = "获取同步点", description = "查询当前用户的所有设备同步点")
    @GetMapping("/sync/points")
    public Result<List<com.ruoyi.im.domain.ImUserSyncPoint>> getSyncPoints() {
        Long userId = SecurityUtils.getLoginUserId();

        try {
            List<com.ruoyi.im.domain.ImUserSyncPoint> points = syncService.getUserSyncPoints(userId);
            return Result.success(points);
        } catch (Exception e) {
            log.error("获取同步点失败: userId={}", userId, e);
            return Result.fail("获取同步点失败");
        }
    }

    /**
     * 重置同步点
     * 删除设备的同步点，下次同步将从头开始
     *
     * @param deviceId 设备ID
     * @return 操作结果
     */
    @Operation(summary = "重置同步点", description = "删除设备的同步点，下次同步将从头开始")
    @DeleteMapping("/sync/point/{deviceId}")
    public Result<Void> resetSyncPoint(@PathVariable String deviceId) {
        Long userId = SecurityUtils.getLoginUserId();

        try {
            syncService.deleteSyncPoint(userId, deviceId);
            log.info("重置同步点成功: userId={}, deviceId={}", userId, deviceId);
            return Result.success("同步点已重置");
        } catch (Exception e) {
            log.error("重置同步点失败: userId={}, deviceId={}", userId, deviceId, e);
            return Result.fail("重置同步点失败");
        }
    }
}
