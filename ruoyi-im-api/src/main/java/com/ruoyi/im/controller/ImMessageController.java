package com.ruoyi.im.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.im.common.Result;
import com.ruoyi.im.domain.ImConversationMember;
import com.ruoyi.im.domain.ImMessage;
import com.ruoyi.im.domain.ImMessageMention;
import com.ruoyi.im.dto.message.ImMessageForwardRequest;
import com.ruoyi.im.dto.message.ImMessageReplyRequest;
import com.ruoyi.im.dto.message.ImMessageSendRequest;
import com.ruoyi.im.dto.message.ImMessageSearchRequest;
import com.ruoyi.im.dto.message.MessageEditRequest;
import com.ruoyi.im.dto.reaction.ImMessageReactionAddRequest;
import com.ruoyi.im.exception.BusinessException;
import com.ruoyi.im.mapper.ImConversationMemberMapper;
import com.ruoyi.im.mapper.ImMessageMapper;
import com.ruoyi.im.service.*;
import com.ruoyi.im.util.SecurityUtils;
import com.ruoyi.im.vo.message.ImMessageSearchResultVO;
import com.ruoyi.im.vo.message.ImMessageVO;
import com.ruoyi.im.vo.reaction.ImMessageReactionVO;
import com.ruoyi.im.websocket.ImWebSocketEndpoint;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

/**
 * 消息控制器
 * 提供消息发送、消息列表查询、消息撤回、消息已读标记、表情反应等功能
 *
 * @author ruoyi
 */
@Tag(name = "消息管理", description = "消息发送、查询、撤回、转发、回复、表情反应等接口")
@Validated
@RestController
@RequestMapping("/api/im/message")
public class ImMessageController {

    private static final Logger log = LoggerFactory.getLogger(ImMessageController.class);

    private final ImMessageService imMessageService;
    private final ImMessageReactionService reactionService;
    private final ImMessageMentionService mentionService;
    private final ImWebSocketBroadcastService broadcastService;
    private final ImMessageReadService messageReadService;
    private final ImMessageMapper imMessageMapper;
    private final ImConversationMemberMapper imConversationMemberMapper;

    public ImMessageController(
            ImMessageService imMessageService,
            ImMessageReactionService reactionService,
            ImMessageMentionService mentionService,
            ImWebSocketBroadcastService broadcastService,
            ImMessageReadService messageReadService, ImMessageMapper imMessageMapper, ImConversationMemberMapper imConversationMemberMapper) {
        this.imMessageService = imMessageService;
        this.reactionService = reactionService;
        this.mentionService = mentionService;
        this.broadcastService = broadcastService;
        this.messageReadService = messageReadService;
        this.imMessageMapper = imMessageMapper;
        this.imConversationMemberMapper = imConversationMemberMapper;
    }

    @PostMapping("/send")
    public Result<ImMessageVO> send(@Valid @RequestBody ImMessageSendRequest request) {
        Long userId = SecurityUtils.getLoginUserId();
        ImMessageVO messageVO = imMessageService.sendMessage(request, userId);
        return Result.success(messageVO);
    }

    @GetMapping("/list/{conversationId}")
    public Result<List<ImMessageVO>> getMessages(
            @PathVariable Long conversationId,
            @RequestParam(required = false) Long lastId,
            @RequestParam(required = false) Long lastMessageId,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) Integer pageSize) {
        Long userId = SecurityUtils.getLoginUserId();
        Long effectiveLastId = lastId != null ? lastId : lastMessageId;
        Integer effectiveLimit = limit != null ? limit : pageSize;
        if (effectiveLimit == null) {
            effectiveLimit = 20;
        }
        log.info("getMessages API - 当前登录用户 userId={}, conversationId={}", userId, conversationId);
        List<ImMessageVO> list = imMessageService.getMessages(conversationId, userId, effectiveLastId, effectiveLimit);
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
     * @param data 包含conversationId和messageIds的请求数据
     * @return 标记结果
     * @apiNote 标记已读后会更新会话的未读消息数，并通过WebSocket推送已读回执给发送方
     * @throws BusinessException 当消息不存在或会话不存在时抛出业务异常
     */
    @Operation(summary = "标记消息已读", description = "批量标记指定消息为已读状态")
    @PutMapping("/mark-read")
    public Result<Void> markAsRead(@RequestBody java.util.Map<String, Object> data) {
        Long userId = SecurityUtils.getLoginUserId();
        Long conversationId = data.get("conversationId") != null ? Long.valueOf(data.get("conversationId").toString())
                : null;
        @SuppressWarnings("unchecked")
        List<Long> messageIds = (List<Long>) data.get("messageIds");
        imMessageService.markAsRead(conversationId, userId, messageIds);

        // [分层对齐] 广播已读通知（取最大的消息ID作为标记位）
        if (messageIds != null && !messageIds.isEmpty()) {
            messageIds.stream().max(Long::compare).ifPresent(maxId -> {
                try {
                    broadcastService.broadcastReadReceipt(conversationId, maxId, userId);
                } catch (Exception e) {
                    log.error("广播已读回执失败", e);
                }
            });
        }

        return Result.success("已标记为已读");
    }
}
