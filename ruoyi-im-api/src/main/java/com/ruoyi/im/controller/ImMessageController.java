package com.ruoyi.im.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.im.common.Result;
import com.ruoyi.im.domain.ImConversationMember;
import com.ruoyi.im.domain.ImMessage;
import com.ruoyi.im.domain.ImMessageMention;
import com.ruoyi.im.dto.message.*;
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

/**
 * 消息控制器
 * 提供消息发送、消息列表查询、消息撤回、消息已读标记、表情反应等功能
 *
 * @author ruoyi
 */

@Validated
@RestController
@RequestMapping("/api/im/message")
public class ImMessageController {

    private static final Logger log = LoggerFactory.getLogger(ImMessageController.class);

    private final ImMessageService imMessageService;
    private final ImMessageReactionService reactionService;
    private final ImMessageMentionService mentionService;
    private final ImMessageReadService messageReadService;
    private final ImWebSocketBroadcastService broadcastService;
    private final ImMessageMapper imMessageMapper;
    private final ImConversationMemberMapper imConversationMemberMapper;

    public ImMessageController(
            ImMessageService imMessageService,
            ImMessageReactionService reactionService,
            ImMessageMentionService mentionService,
            ImMessageReadService messageReadService,
            ImWebSocketBroadcastService broadcastService,
            ImMessageMapper imMessageMapper,
            ImConversationMemberMapper imConversationMemberMapper) {
        this.imMessageService = imMessageService;
        this.reactionService = reactionService;
        this.mentionService = mentionService;
        this.messageReadService = messageReadService;
        this.broadcastService = broadcastService;
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
            @Valid @RequestBody MessageEditRequest request) {
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
    
    @PutMapping("/mark-read")
    public Result<Void> markAsRead(@Valid @RequestBody MarkAsReadRequest request) {
        Long userId = SecurityUtils.getLoginUserId();
        imMessageService.markAsRead(request.getConversationId(), userId, request.getMessageIds(), true);
        return Result.success("已标记为已读");
    }

    /**
     * 转发消息
     */
    @PostMapping("/forward")
    public Result<Void> forward(@Valid @RequestBody ImMessageForwardRequest request) {
        Long userId = SecurityUtils.getLoginUserId();
        imMessageService.forwardMessage(request.getMessageId(), request.getToConversationId(),
                request.getToUserId(), request.getContent(), userId);
        return Result.success("转发成功");
    }

    /**
     * 批量/合并转发消息
     */
    @PostMapping("/batch-forward")
    public Result<Void> batchForward(@Valid @RequestBody ImMessageBatchForwardRequest request) {
        Long userId = SecurityUtils.getLoginUserId();
        imMessageService.batchForward(request, userId);
        return Result.success("转发成功");
    }

    /**
     * 引用/回复消息
     */
    @PostMapping("/reply")
    public Result<Long> reply(@Valid @RequestBody ImMessageReplyRequest request) {
        Long userId = SecurityUtils.getLoginUserId();
        Long replyId = imMessageService.replyMessage(request.getMessageId(), request.getContent(), userId);
        return Result.success("回复成功", replyId);
    }

    /**
     * 更新正在输入状态
     */
    @PostMapping("/typing/{conversationId}")
    public Result<Void> setTyping(@PathVariable Long conversationId, @RequestParam boolean typing) {
        Long userId = SecurityUtils.getLoginUserId();
        // 直接调用广播服务通知其他成员
        broadcastService.broadcastTypingStatus(conversationId, userId, typing);
        return Result.success();
    }

    /**
     * 搜索消息
     * @param request 搜索请求参数
     */
    @PostMapping("/search")
    public Result<ImMessageSearchResultVO> searchMessages(@Valid @RequestBody ImMessageSearchRequest request) {
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
}

