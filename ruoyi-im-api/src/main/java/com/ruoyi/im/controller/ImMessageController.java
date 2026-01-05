package com.ruoyi.im.controller;

import com.ruoyi.im.annotation.ImApiVersion;
import com.ruoyi.im.annotation.ImPerformanceMonitor;
import com.ruoyi.im.annotation.ImRateLimit;
import com.ruoyi.im.annotation.RequirePermission;
import com.ruoyi.im.common.Result;
import com.ruoyi.im.domain.ImConversationMember;
import com.ruoyi.im.domain.ImMessage;
import com.ruoyi.im.domain.ImMessageReaction;
import com.ruoyi.im.domain.ImMessageReadReceipt;
import com.ruoyi.im.domain.ImUser;
import com.ruoyi.im.dto.MessageSendRequest;
import com.ruoyi.im.dto.message.ImMessageMarkReadRequest;
import com.ruoyi.im.dto.message.ImMessageQueryRequest;
import com.ruoyi.im.exception.BusinessException;
import com.ruoyi.im.service.ImConversationMemberService;
import com.ruoyi.im.service.ImConversationService;
import com.ruoyi.im.service.ImMessageReactionService;
import com.ruoyi.im.service.ImMessageReadReceiptService;
import com.ruoyi.im.service.ImMessageService;
import com.ruoyi.im.service.ImUserService;
import com.ruoyi.im.vo.message.ImMessageReactionVO;
import com.ruoyi.im.vo.message.ImMessageReadReceiptVO;
import com.ruoyi.im.vo.message.ImMessageVO;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Api(tags = "消息管理")
@RestController
@RequestMapping("/api/{version}/im/messages")
@Validated
@ImApiVersion(value = {"v1", "v2"}, description = "消息管理API，支持v1和v2版本")
public class ImMessageController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(ImMessageController.class);

    @Autowired
    private ImMessageService imMessageService;
    
    @Autowired
    private ImConversationService imConversationService;
    
    @Autowired
    private ImConversationMemberService imConversationMemberService;

    @Autowired
    private ImMessageReactionService imMessageReactionService;

    @Autowired
    private ImMessageReadReceiptService imMessageReadReceiptService;

    @Autowired
    private ImUserService imUserService;

    /**
     * 发送消息
     * 
     * 向指定会话发送文本、图片、文件等类型的消息。
     * 支持文本消息、文件消息、图片消息等多种消息类型。
     * 
     * @param request 消息发送请求，包含会话ID、消息类型和内容
     * @param bindingResult 参数验证结果
     * @return 发送成功的消息详细信息
     */
    @ApiOperation(value = "发送消息", notes = "发送文本、文件等类型的消息到指定会话")
    @ApiResponses({
        @ApiResponse(code = 200, message = "消息发送成功"),
        @ApiResponse(code = 400, message = "参数验证失败"),
        @ApiResponse(code = 401, message = "未授权访问"),
        @ApiResponse(code = 403, message = "权限不足"),
        @ApiResponse(code = 404, message = "会话不存在"),
        @ApiResponse(code = 500, message = "消息发送失败")
    })
    @PostMapping
    @RequirePermission(value = "im:message:create", desc = "发送消息")
    @ImPerformanceMonitor(value = "发送消息", threshold = 500, group = "消息管理")
    @ImRateLimit(key = "message:send", rate = 100, window = 60, message = "消息发送过于频繁，请稍后再试", group = "消息管理")
    public Result<ImMessageVO> createMessage(
            @Valid @ApiParam("消息发送请求，包含会话ID、消息类型和内容") @RequestBody MessageSendRequest request, 
            BindingResult bindingResult) {
        Result<Void> validationResult = handleValidationError(bindingResult);
        if (validationResult != null) {
            return Result.error(validationResult.getCode(), validationResult.getMessage());
        }
        
        log.info("发送消息请求: conversationId={}, messageType={}", request.getConversationId(), request.getMessageType());
        
        Long currentUserId = getCurrentUserId();
        
        Long messageId = imMessageService.sendMessage(
            request.getConversationId(), 
            currentUserId, 
            request.getMessageType().toString(), 
            request.getContent()
        );
        
        if (messageId == null) {
            log.error("消息发送失败: conversationId={}", request.getConversationId());
            return Result.error(500, "消息发送失败");
        }
        
        ImMessage message = imMessageService.selectImMessageById(messageId);
        log.info("消息发送成功: messageId={}", messageId);
        return Result.success("消息发送成功", convertToVO(message));
    }

    @ApiOperation(value = "分页查询会话消息列表", notes = "支持按消息类型和关键词查询，支持时间范围查询")
    @ApiResponses({
        @ApiResponse(code = 200, message = "查询成功"),
        @ApiResponse(code = 400, message = "参数验证失败"),
        @ApiResponse(code = 401, message = "未授权访问"),
        @ApiResponse(code = 403, message = "权限不足")
    })
    @GetMapping
    @RequirePermission(value = "im:message:list", desc = "分页查询会话消息列表")
    @ImPerformanceMonitor(value = "分页查询会话消息列表", threshold = 300, group = "消息管理")
    @ImRateLimit(key = "message:list", rate = 200, window = 60, message = "消息列表查询过于频繁，请稍后再试", group = "消息管理")
    public Result<com.ruoyi.im.common.PageResult<ImMessageVO>> listMessage(
            @Valid ImMessageQueryRequest request,
            BindingResult bindingResult) {
        Result<Void> validationResult = handleValidationError(bindingResult);
        if (validationResult != null) {
            return Result.error(validationResult.getCode(), validationResult.getMessage());
        }
        
        log.info("分页查询会话消息列表请求: conversationId={}, pageNum={}, pageSize={}, messageType={}, keywords={}", 
                request.getConversationId(), request.getPageNum(), request.getPageSize(), request.getMessageType(), request.getKeywords());
        
        Long currentUserId = getCurrentUserId();
        
        ImConversationMember member = imConversationMemberService
            .selectImConversationMemberByConversationIdAndUserId(request.getConversationId(), currentUserId);
        if (member == null) {
            log.warn("用户不在会话中: conversationId={}, userId={}", request.getConversationId(), currentUserId);
            return forbidden("用户不在该会话中");
        }
        
        List<ImMessage> messages = imMessageService.selectImMessageListByConversationId(request.getConversationId());
        
        List<ImMessage> filteredMessages = messages.stream()
            .filter(msg -> request.getMessageType() == null || msg.getType().equals(request.getMessageType()))
            .filter(msg -> request.getKeywords() == null || 
                (msg.getContent() != null && msg.getContent().contains(request.getKeywords())))
            .sorted((m1, m2) -> m2.getCreateTime().compareTo(m1.getCreateTime()))
            .collect(Collectors.toList());
        
        int start = (request.getPageNum() - 1) * request.getPageSize();
        int end = Math.min(start + request.getPageSize(), filteredMessages.size());
        
        List<ImMessage> pagedMessages = start < filteredMessages.size() ? 
            filteredMessages.subList(start, end) : java.util.Collections.emptyList();
        
        List<ImMessageVO> messageVOList = pagedMessages.stream()
            .map(this::convertToVO)
            .collect(Collectors.toList());
        
        log.info("分页查询会话消息列表成功: conversationId={}, total={}, count={}", 
                request.getConversationId(), filteredMessages.size(), messageVOList.size());
        return getDataTable(messageVOList, filteredMessages.size());
    }

    @ApiOperation(value = "标记消息已读", notes = "将指定消息或会话所有消息标记为已读状态")
    @ApiResponses({
        @ApiResponse(code = 200, message = "标记成功"),
        @ApiResponse(code = 400, message = "参数验证失败"),
        @ApiResponse(code = 401, message = "未授权访问"),
        @ApiResponse(code = 403, message = "权限不足")
    })
    @PutMapping("/{conversationId}/read")
    @RequirePermission(value = "im:message:read", desc = "标记消息已读")
    @ImPerformanceMonitor(value = "标记消息已读", threshold = 200, group = "消息管理")
    @ImRateLimit(key = "message:read", rate = 500, window = 60, message = "消息已读标记过于频繁，请稍后再试", group = "消息管理")
    public Result<Void> markMessageRead(
            @PathVariable @Positive(message = "会话ID必须为正数") Long conversationId, 
            @Valid @RequestBody ImMessageMarkReadRequest request,
            BindingResult bindingResult) {
        Result<Void> validationResult = handleValidationError(bindingResult);
        if (validationResult != null) {
            return Result.error(validationResult.getCode(), validationResult.getMessage());
        }
        
        log.info("标记消息已读请求: conversationId={}, messageIds={}, markAllRead={}", 
                conversationId, request.getMessageIds(), request.getMarkAllRead());
        
        Long currentUserId = getCurrentUserId();
        
        ImConversationMember member = imConversationMemberService
            .selectImConversationMemberByConversationIdAndUserId(conversationId, currentUserId);
        if (member == null) {
            log.warn("用户不在会话中: conversationId={}, userId={}", conversationId, currentUserId);
            return forbidden("用户不在该会话中");
        }
        
        if (Boolean.TRUE.equals(request.getMarkAllRead()) || 
            (request.getMessageIds() == null || request.getMessageIds().isEmpty())) {
            List<ImMessage> messages = imMessageService.selectImMessageListByConversationId(conversationId);
            if (!messages.isEmpty()) {
                ImMessage lastMessage = messages.get(messages.size() - 1);
                imConversationMemberService.markMessageAsRead(conversationId, currentUserId, lastMessage.getId());
            }
        } else {
            for (Long messageId : request.getMessageIds()) {
                imConversationMemberService.markMessageAsRead(conversationId, currentUserId, messageId);
            }
        }
        
        log.info("标记消息已读成功: conversationId={}", conversationId);
        return Result.success("消息已读状态更新成功", null);
    }

    @ApiOperation(value = "撤回消息", notes = "撤回指定消息，只能撤回自己发送的消息")
    @ApiResponses({
        @ApiResponse(code = 200, message = "撤回成功"),
        @ApiResponse(code = 400, message = "参数验证失败"),
        @ApiResponse(code = 401, message = "未授权访问"),
        @ApiResponse(code = 403, message = "权限不足"),
        @ApiResponse(code = 404, message = "消息不存在"),
        @ApiResponse(code = 500, message = "消息撤回失败")
    })
    @PutMapping("/{messageId}/recall")
    @RequirePermission(value = "im:message:recall", desc = "撤回消息")
    @ImPerformanceMonitor(value = "撤回消息", threshold = 300, group = "消息管理")
    @ImRateLimit(key = "message:recall", rate = 50, window = 60, message = "消息撤回操作过于频繁，请稍后再试", group = "消息管理")
    public Result<ImMessageVO> recallMessage(@PathVariable @Positive(message = "消息ID必须为正数") Long messageId) {
        log.info("撤回消息请求: messageId={}", messageId);
        
        Long currentUserId = getCurrentUserId();
        
        ImMessage message = imMessageService.selectImMessageById(messageId);
        if (message == null) {
            log.warn("消息不存在: messageId={}", messageId);
            return notFound("消息不存在");
        }
        
        if (!message.getSenderId().equals(currentUserId)) {
            log.warn("用户无权撤回消息: messageId={}, userId={}", messageId, currentUserId);
            return forbidden("只能撤回自己发送的消息");
        }
        
        int recallResult = imMessageService.revokeMessage(messageId, currentUserId);
        if (recallResult <= 0) {
            log.error("消息撤回失败: messageId={}", messageId);
            return Result.error(500, "消息撤回失败");
        }
        
        log.info("消息撤回成功: messageId={}", messageId);
        return Result.success("消息撤回成功", convertToVO(message));
    }

    @ApiOperation(value = "搜索消息", notes = "在指定会话中搜索消息，支持关键词和消息类型过滤")
    @ApiResponses({
        @ApiResponse(code = 200, message = "搜索成功"),
        @ApiResponse(code = 400, message = "参数验证失败"),
        @ApiResponse(code = 401, message = "未授权访问"),
        @ApiResponse(code = 403, message = "权限不足")
    })
    @GetMapping("/search")
    @RequirePermission(value = "im:message:search", desc = "搜索消息")
    @ImPerformanceMonitor(value = "搜索消息", threshold = 400, group = "消息管理")
    @ImRateLimit(key = "message:search", rate = 100, window = 60, message = "消息搜索过于频繁，请稍后再试", group = "消息管理")
    public Result<com.ruoyi.im.common.PageResult<ImMessageVO>> searchMessages(
            @Valid ImMessageQueryRequest request,
            BindingResult bindingResult) {
        Result<Void> validationResult = handleValidationError(bindingResult);
        if (validationResult != null) {
            return Result.error(validationResult.getCode(), validationResult.getMessage());
        }
        
        log.info("搜索消息请求: conversationId={}, keywords={}, messageType={}, pageNum={}, pageSize={}", 
                request.getConversationId(), request.getKeywords(), request.getMessageType(), request.getPageNum(), request.getPageSize());
        
        Long currentUserId = getCurrentUserId();
        
        if (request.getConversationId() != null) {
            ImConversationMember member = imConversationMemberService
                .selectImConversationMemberByConversationIdAndUserId(request.getConversationId(), currentUserId);
            if (member == null) {
                log.warn("用户不在会话中: conversationId={}, userId={}", request.getConversationId(), currentUserId);
                return forbidden("用户不在该会话中");
            }
            
            List<ImMessage> messages = imMessageService.selectImMessageListByConversationId(request.getConversationId());
            
            List<ImMessage> filteredMessages = messages.stream()
                .filter(msg -> request.getMessageType() == null || msg.getType().equals(request.getMessageType()))
                .filter(msg -> request.getKeywords() == null || 
                    (msg.getContent() != null && msg.getContent().contains(request.getKeywords())))
                .sorted((m1, m2) -> m2.getCreateTime().compareTo(m1.getCreateTime()))
                .collect(Collectors.toList());
            
            int start = (request.getPageNum() - 1) * request.getPageSize();
            int end = Math.min(start + request.getPageSize(), filteredMessages.size());
            
            List<ImMessage> pagedMessages = start < filteredMessages.size() ? 
                filteredMessages.subList(start, end) : java.util.Collections.emptyList();
            
            List<ImMessageVO> messageVOList = pagedMessages.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
            
            log.info("搜索消息成功: conversationId={}, total={}, count={}", 
                    request.getConversationId(), filteredMessages.size(), messageVOList.size());
            return getDataTable(messageVOList, filteredMessages.size());
        }
        
        log.info("搜索消息成功: 未指定会话ID");
        return getDataTable(java.util.Collections.emptyList(), 0);
    }

    @ApiOperation(value = "获取消息详情", notes = "获取指定消息的详细信息，包括互动和已读回执")
    @ApiResponses({
        @ApiResponse(code = 200, message = "获取成功"),
        @ApiResponse(code = 400, message = "参数验证失败"),
        @ApiResponse(code = 401, message = "未授权访问"),
        @ApiResponse(code = 404, message = "消息不存在")
    })
    @GetMapping("/{messageId}")
    @RequirePermission(value = "im:message:detail", desc = "获取消息详情")
    @ImPerformanceMonitor(value = "获取消息详情", threshold = 200, group = "消息管理")
    @ImRateLimit(key = "message:detail", rate = 500, window = 60, message = "消息详情查询过于频繁，请稍后再试", group = "消息管理")
    public Result<ImMessageVO> getMessageDetail(@PathVariable @Positive(message = "消息ID必须为正数") Long messageId) {
        log.info("获取消息详情请求: messageId={}", messageId);
        
        ImMessage message = imMessageService.selectImMessageById(messageId);
        if (message == null) {
            log.warn("消息不存在: messageId={}", messageId);
            return notFound("消息不存在");
        }
        
        log.info("获取消息详情成功: messageId={}", messageId);
        return Result.success(convertToVO(message));
    }

    @ApiOperation(value = "获取用户未读消息总数", notes = "获取当前用户所有会话的未读消息总数量")
    @ApiResponses({
        @ApiResponse(code = 200, message = "获取成功"),
        @ApiResponse(code = 401, message = "未授权访问")
    })
    @GetMapping("/unread/count")
    @RequirePermission(value = "im:message:unread:count", desc = "获取用户未读消息总数")
    @ImPerformanceMonitor(value = "获取用户未读消息总数", threshold = 200, group = "消息管理")
    @ImRateLimit(key = "message:unread:count", rate = 300, window = 60, message = "未读消息计数查询过于频繁，请稍后再试", group = "消息管理")
    public Result<Integer> getUnreadCount() {
        log.info("获取用户未读消息总数请求");
        
        Long currentUserId = getCurrentUserId();
        
        List<ImConversationMember> members = imConversationMemberService.selectImConversationMemberListByUserId(currentUserId);
        int unreadCount = members.stream()
            .mapToInt(member -> member.getUnreadCount() != null ? member.getUnreadCount() : 0)
            .sum();
        
        log.info("获取用户未读消息总数成功: count={}", unreadCount);
        return Result.success(unreadCount);
    }

    @ApiOperation(value = "获取会话未读消息数量", notes = "获取指定会话中当前用户的未读消息数量")
    @ApiResponses({
        @ApiResponse(code = 200, message = "获取成功"),
        @ApiResponse(code = 400, message = "参数验证失败"),
        @ApiResponse(code = 401, message = "未授权访问"),
        @ApiResponse(code = 403, message = "权限不足"),
        @ApiResponse(code = 404, message = "用户不在会话中")
    })
    @GetMapping("/unread/conversation/{conversationId}")
    @RequirePermission(value = "im:message:unread:conversation", desc = "获取会话未读消息数量")
    @ImPerformanceMonitor(value = "获取会话未读消息数量", threshold = 200, group = "消息管理")
    @ImRateLimit(key = "message:unread:conversation", rate = 500, window = 60, message = "会话未读消息计数查询过于频繁，请稍后再试", group = "消息管理")
    public Result<Integer> getConversationUnreadCount(@PathVariable @Positive(message = "会话ID必须为正数") Long conversationId) {
        log.info("获取会话未读消息数量请求: conversationId={}", conversationId);
        
        Long currentUserId = getCurrentUserId();
        
        ImConversationMember member = imConversationMemberService
            .selectImConversationMemberByConversationIdAndUserId(conversationId, currentUserId);
        
        if (member == null) {
            log.warn("用户不在会话中: conversationId={}, userId={}", conversationId, currentUserId);
            return forbidden("用户不在该会话中");
        }
        
        int unreadCount = member.getUnreadCount() != null ? member.getUnreadCount() : 0;
        log.info("获取会话未读消息数量成功: conversationId={}, count={}", conversationId, unreadCount);
        return Result.success(unreadCount);
    }

    /**
     * 将ImMessage实体对象转换为ImMessageVO
     * 
     * @param message ImMessage实体对象
     * @return ImMessageVO对象
     */
    private ImMessageVO convertToVO(ImMessage message) {
        if (message == null) {
            return null;
        }
        
        ImMessageVO vo = new ImMessageVO();
        vo.setId(message.getId());
        vo.setConversationId(message.getConversationId());
        vo.setSenderId(message.getSenderId());
        vo.setType(message.getType());
        vo.setContent(message.getContent());
        vo.setStatus(message.getStatus());
        vo.setExtData(message.getExtData());
        vo.setReplyMessageId(message.getReplyMessageId());
        vo.setCreateTime(message.getCreateTime());
        vo.setUpdateTime(message.getUpdateTime());
        
        if (message.getStatus() != null) {
            switch (message.getStatus()) {
                case "NORMAL":
                    vo.setStatusDesc("正常");
                    break;
                case "REVOKED":
                    vo.setStatusDesc("已撤回");
                    break;
                case "DELETED":
                    vo.setStatusDesc("已删除");
                    break;
                default:
                    vo.setStatusDesc(message.getStatus());
                    break;
            }
        }
        
        if (message.getReplyMessageId() != null) {
            ImMessage replyMessage = imMessageService.selectImMessageById(message.getReplyMessageId());
            if (replyMessage != null) {
                vo.setReplyMessageContent(replyMessage.getContent());
            }
        }
        
        if (message.getSenderId() != null) {
            ImUser sender = imUserService.selectImUserById(message.getSenderId());
            if (sender != null) {
                vo.setSenderUsername(sender.getUsername());
                vo.setSenderNickname(sender.getNickname());
            }
        }
        
        List<ImMessageReaction> reactions = imMessageReactionService.selectImMessageReactionListByMessageId(message.getId());
        if (reactions != null && !reactions.isEmpty()) {
            List<ImMessageReactionVO> reactionVOs = reactions.stream()
                .map(this::convertReactionToVO)
                .collect(Collectors.toList());
            vo.setReactions(reactionVOs);
        }
        
        List<ImMessageReadReceipt> readReceipts = imMessageReadReceiptService.selectImMessageReadReceiptListByMessageId(message.getId());
        if (readReceipts != null && !readReceipts.isEmpty()) {
            List<ImMessageReadReceiptVO> receiptVOs = readReceipts.stream()
                .map(this::convertReadReceiptToVO)
                .collect(Collectors.toList());
            vo.setReadReceipts(receiptVOs);
        }
        
        return vo;
    }

    /**
     * 将ImMessageReaction实体对象转换为ImMessageReactionVO
     * 
     * @param reaction ImMessageReaction实体对象
     * @return ImMessageReactionVO对象
     */
    private ImMessageReactionVO convertReactionToVO(ImMessageReaction reaction) {
        if (reaction == null) {
            return null;
        }
        
        ImMessageReactionVO vo = new ImMessageReactionVO();
        vo.setId(reaction.getId());
        vo.setMessageId(reaction.getMessageId());
        vo.setUserId(reaction.getUserId());
        vo.setReactionType(reaction.getReactionType());
        vo.setCreateTime(reaction.getCreateTime());
        
        if (reaction.getReactionType() != null) {
            switch (reaction.getReactionType()) {
                case "LIKE":
                    vo.setReactionTypeDesc("点赞");
                    break;
                case "LOVE":
                    vo.setReactionTypeDesc("爱心");
                    break;
                case "LAUGH":
                    vo.setReactionTypeDesc("笑哭");
                    break;
                case "ANGRY":
                    vo.setReactionTypeDesc("愤怒");
                    break;
                case "SAD":
                    vo.setReactionTypeDesc("伤心");
                    break;
                default:
                    vo.setReactionTypeDesc(reaction.getReactionType());
                    break;
            }
        }
        
        if (reaction.getUserId() != null) {
            ImUser user = imUserService.selectImUserById(reaction.getUserId());
            if (user != null) {
                vo.setUsername(user.getUsername());
                vo.setNickname(user.getNickname());
            }
        }
        
        return vo;
    }

    /**
     * 将ImMessageReadReceipt实体对象转换为ImMessageReadReceiptVO
     * 
     * @param readReceipt ImMessageReadReceipt实体对象
     * @return ImMessageReadReceiptVO对象
     */
    private ImMessageReadReceiptVO convertReadReceiptToVO(ImMessageReadReceipt readReceipt) {
        if (readReceipt == null) {
            return null;
        }
        
        ImMessageReadReceiptVO vo = new ImMessageReadReceiptVO();
        vo.setId(readReceipt.getId());
        vo.setMessageId(readReceipt.getMessageId());
        vo.setConversationId(readReceipt.getConversationId());
        vo.setUserId(readReceipt.getUserId());
        vo.setReadTime(readReceipt.getReadTime());
        
        if (readReceipt.getUserId() != null) {
            ImUser user = imUserService.selectImUserById(readReceipt.getUserId());
            if (user != null) {
                vo.setUsername(user.getUsername());
                vo.setNickname(user.getNickname());
            }
        }
        
        return vo;
    }

    /**
     * 获取当前用户ID
     * 
     * @return 当前用户ID
     */
    private Long getCurrentUserId() {
        return 1L;
    }

    @ApiOperation("回复消息")
    @PostMapping("/reply")
    @ImPerformanceMonitor(value = "回复消息", threshold = 300, group = "消息管理")
    @ImRateLimit(key = "message:reply", rate = 100, window = 60, message = "消息回复过于频繁，请稍后再试", group = "消息管理")
    public Result<ImMessage> replyMessage(@RequestBody Map<String, Object> data) {
        log.info("回复消息请求");
        
        Long originalMessageId = Long.valueOf(data.get("originalMessageId").toString());
        String content = data.get("content").toString();
        Long conversationId = Long.valueOf(data.get("conversationId").toString());
        
        Long currentUserId = 1L;
        
        Long messageId = imMessageService.sendReplyMessage(conversationId, currentUserId, originalMessageId, "TEXT", content);
        
        if (messageId == null) {
            log.error("回复发送失败: conversationId={}", conversationId);
            throw new BusinessException(500, "回复发送失败");
        }
        
        ImMessage message = imMessageService.selectImMessageById(messageId);
        log.info("回复发送成功: messageId={}", messageId);
        return Result.success("回复发送成功", message);
    }

    @ApiOperation("添加消息互动")
    @PostMapping("/reaction/add")
    @ImPerformanceMonitor(value = "添加消息互动", threshold = 200, group = "消息管理")
    @ImRateLimit(key = "message:reaction:add", rate = 200, window = 60, message = "消息互动添加过于频繁，请稍后再试", group = "消息管理")
    public Result<ImMessageReaction> addReaction(@RequestBody Map<String, Object> data) {
        log.info("添加消息互动请求");
        
        Long messageId = Long.valueOf(data.get("messageId").toString());
        String reactionType = data.get("reactionType").toString();
        
        Long currentUserId = 1L;
        
        ImMessageReaction reaction = new ImMessageReaction();
        reaction.setMessageId(messageId);
        reaction.setUserId(currentUserId);
        reaction.setReactionType(reactionType);
        
        int result = imMessageReactionService.insertImMessageReaction(reaction);
        if (result <= 0) {
            log.error("添加消息互动失败: messageId={}, reactionType={}", messageId, reactionType);
            throw new BusinessException(500, "添加消息互动失败");
        }
        
        log.info("添加消息互动成功: messageId={}, reactionType={}", messageId, reactionType);
        return Result.success("添加消息互动成功", reaction);
    }

    @ApiOperation("移除消息互动")
    @DeleteMapping("/reaction/remove")
    @ImPerformanceMonitor(value = "移除消息互动", threshold = 200, group = "消息管理")
    @ImRateLimit(key = "message:reaction:remove", rate = 200, window = 60, message = "消息互动移除过于频繁，请稍后再试", group = "消息管理")
    public Result<Void> removeReaction(@RequestBody Map<String, Object> data) {
        log.info("移除消息互动请求");
        
        Long messageId = Long.valueOf(data.get("messageId").toString());
        String reactionType = data.get("reactionType").toString();
        
        Long currentUserId = 1L;
        
        int result = imMessageReactionService.deleteImMessageReaction(messageId, currentUserId, reactionType);
        if (result <= 0) {
            log.error("移除消息互动失败: messageId={}, reactionType={}", messageId, reactionType);
            throw new BusinessException(500, "移除消息互动失败");
        }
        
        log.info("移除消息互动成功: messageId={}, reactionType={}", messageId, reactionType);
        return Result.success("移除消息互动成功", null);
    }

    @ApiOperation("获取消息互动列表")
    @GetMapping("/reaction/list/{messageId}")
    @ImPerformanceMonitor(value = "获取消息互动列表", threshold = 200, group = "消息管理")
    @ImRateLimit(key = "message:reaction:list", rate = 300, window = 60, message = "消息互动列表查询过于频繁，请稍后再试", group = "消息管理")
    public Result<List<ImMessageReaction>> getReactionList(@PathVariable @Positive Long messageId) {
        log.info("获取消息互动列表请求: messageId={}", messageId);
        
        List<ImMessageReaction> reactions = imMessageReactionService.selectImMessageReactionListByMessageId(messageId);
        log.info("获取消息互动列表成功: messageId={}, count={}", messageId, reactions.size());
        return Result.success(reactions);
    }

    @ApiOperation("发送消息已读回执")
    @PostMapping("/readReceipt/send")
    @ImPerformanceMonitor(value = "发送消息已读回执", threshold = 200, group = "消息管理")
    @ImRateLimit(key = "message:readReceipt:send", rate = 500, window = 60, message = "消息已读回执发送过于频繁，请稍后再试", group = "消息管理")
    public Result<ImMessageReadReceipt> sendReadReceipt(@RequestBody Map<String, Object> data) {
        log.info("发送消息已读回执请求");
        
        Long messageId = Long.valueOf(data.get("messageId").toString());
        Long conversationId = Long.valueOf(data.get("conversationId").toString());
        
        Long currentUserId = 1L;
        
        ImMessageReadReceipt readReceipt = new ImMessageReadReceipt();
        readReceipt.setMessageId(messageId);
        readReceipt.setConversationId(conversationId);
        readReceipt.setUserId(currentUserId);
        readReceipt.setReadTime(java.time.LocalDateTime.now());
        
        int result = imMessageReadReceiptService.insertImMessageReadReceipt(readReceipt);
        if (result <= 0) {
            log.error("发送消息已读回执失败: messageId={}, conversationId={}", messageId, conversationId);
            throw new BusinessException(500, "发送消息已读回执失败");
        }
        
        log.info("发送消息已读回执成功: messageId={}, conversationId={}", messageId, conversationId);
        return Result.success("发送消息已读回执成功", readReceipt);
    }

    @ApiOperation("获取消息已读回执列表")
    @GetMapping("/readReceipt/list/{messageId}")
    @ImPerformanceMonitor(value = "获取消息已读回执列表", threshold = 200, group = "消息管理")
    @ImRateLimit(key = "message:readReceipt:list", rate = 300, window = 60, message = "消息已读回执列表查询过于频繁，请稍后再试", group = "消息管理")
    public Result<List<ImMessageReadReceipt>> getReadReceiptList(@PathVariable @Positive Long messageId) {
        log.info("获取消息已读回执列表请求: messageId={}", messageId);
        
        List<ImMessageReadReceipt> readReceipts = imMessageReadReceiptService.selectImMessageReadReceiptListByMessageId(messageId);
        log.info("获取消息已读回执列表成功: messageId={}, count={}", messageId, readReceipts.size());
        return Result.success(readReceipts);
    }

    @ApiOperation("获取会话已读回执列表")
    @GetMapping("/readReceipt/conversation/{conversationId}")
    @ImPerformanceMonitor(value = "获取会话已读回执列表", threshold = 300, group = "消息管理")
    @ImRateLimit(key = "message:readReceipt:conversation", rate = 200, window = 60, message = "会话已读回执列表查询过于频繁，请稍后再试", group = "消息管理")
    public Result<List<ImMessageReadReceipt>> getConversationReadReceiptList(@PathVariable @Positive Long conversationId) {
        log.info("获取会话已读回执列表请求: conversationId={}", conversationId);
        
        List<ImMessageReadReceipt> readReceipts = imMessageReadReceiptService.selectImMessageReadReceiptListByConversationId(conversationId);
        log.info("获取会话已读回执列表成功: conversationId={}, count={}", conversationId, readReceipts.size());
        return Result.success(readReceipts);
    }
}
