package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.domain.ImConversationMember;
import com.ruoyi.im.domain.ImMessage;
import com.ruoyi.im.domain.ImMessageReaction;
import com.ruoyi.im.domain.ImMessageReadReceipt;
import com.ruoyi.im.dto.MessageSendRequest;
import com.ruoyi.im.exception.BusinessException;
import com.ruoyi.im.service.ImConversationMemberService;
import com.ruoyi.im.service.ImConversationService;
import com.ruoyi.im.service.ImMessageReactionService;
import com.ruoyi.im.service.ImMessageReadReceiptService;
import com.ruoyi.im.service.ImMessageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
@RequestMapping("/api/im/message")
@Validated
public class MessageController {

    private static final Logger log = LoggerFactory.getLogger(MessageController.class);

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

    @ApiOperation("发送消息")
    @PostMapping("/send")
    public Result<ImMessage> sendMessage(@Valid @RequestBody MessageSendRequest request) {
        log.info("发送消息请求: conversationId={}, type={}", request.getConversationId(), request.getMessageType());
        
        Long currentUserId = 1L;
        
        Long messageId = imMessageService.sendMessage(
            request.getConversationId(), 
            currentUserId, 
            request.getMessageType().toString(), 
            request.getContent()
        );
        
        if (messageId == null) {
            log.error("消息发送失败: conversationId={}", request.getConversationId());
            throw new BusinessException(500, "消息发送失败");
        }
        
        ImMessage message = imMessageService.selectImMessageById(messageId);
        log.info("消息发送成功: messageId={}", messageId);
        return Result.success("消息发送成功", message);
    }

    @ApiOperation("获取会话消息列表")
    @GetMapping("/history")
    public Result<List<ImMessage>> listMessage(
            @ApiParam("会话ID") @RequestParam @NotNull Long sessionId,
            @ApiParam("每页数量") @RequestParam(defaultValue = "20") Integer size,
            @ApiParam("最后一条消息ID") @RequestParam(required = false) Long lastMessageId) {
        log.info("获取消息历史请求: sessionId={}, size={}", sessionId, size);
        
        Long currentUserId = 1L;
        
        ImConversationMember member = imConversationMemberService
            .selectImConversationMemberByConversationIdAndUserId(sessionId, currentUserId);
        if (member == null) {
            log.warn("用户不在会话中: sessionId={}, userId={}", sessionId, currentUserId);
            throw new BusinessException(403, "用户不在会话中");
        }
        
        List<ImMessage> messages = imMessageService.selectImMessageListByConversationId(sessionId);
        List<ImMessage> sortedMessages = messages.stream()
            .sorted((m1, m2) -> m2.getCreateTime().compareTo(m1.getCreateTime()))
            .limit(size)
            .collect(Collectors.toList());
        
        log.info("获取消息历史成功: sessionId={}, count={}", sessionId, sortedMessages.size());
        return Result.success(sortedMessages);
    }

    @ApiOperation("标记消息已读")
    @PutMapping("/read/{conversationId}")
    public Result<Void> markMessageRead(
            @PathVariable Long conversationId, 
            @RequestBody Map<String, Object> data) {
        log.info("标记消息已读请求: conversationId={}", conversationId);
        
        Long currentUserId = 1L;
        List<Long> messageIds = (List<Long>) data.get("messageIds");
        
        if (messageIds != null && !messageIds.isEmpty()) {
            for (Long messageId : messageIds) {
                imConversationMemberService.markMessageAsRead(conversationId, currentUserId, messageId);
            }
        } else {
            List<ImMessage> messages = imMessageService.selectImMessageListByConversationId(conversationId);
            if (!messages.isEmpty()) {
                ImMessage lastMessage = messages.get(messages.size() - 1);
                imConversationMemberService.markMessageAsRead(conversationId, currentUserId, lastMessage.getId());
            }
        }
        
        log.info("标记消息已读成功: conversationId={}", conversationId);
        return Result.success("消息已读状态更新成功", null);
    }

    @ApiOperation("撤回消息")
    @PutMapping("/recall/{messageId}")
    public Result<ImMessage> recallMessage(@PathVariable @Positive Long messageId) {
        log.info("撤回消息请求: messageId={}", messageId);
        
        Long currentUserId = 1L;
        
        ImMessage message = imMessageService.selectImMessageById(messageId);
        if (message == null) {
            log.warn("消息不存在: messageId={}", messageId);
            throw new BusinessException(404, "消息不存在");
        }
        
        if (!message.getSenderId().equals(currentUserId)) {
            log.warn("用户无权撤回消息: messageId={}, userId={}", messageId, currentUserId);
            throw new BusinessException(403, "只能撤回自己发送的消息");
        }
        
        int recallResult = imMessageService.revokeMessage(messageId, currentUserId);
        if (recallResult <= 0) {
            log.error("消息撤回失败: messageId={}", messageId);
            throw new BusinessException(500, "消息撤回失败");
        }
        
        log.info("消息撤回成功: messageId={}", messageId);
        return Result.success("消息撤回成功", message);
    }

    @ApiOperation("搜索消息")
    @GetMapping("/search")
    public Result<Map<String, Object>> searchMessages(
            @ApiParam("关键词") @RequestParam(required = false) String keywords,
            @ApiParam("会话ID") @RequestParam(required = false) Long conversationId,
            @ApiParam("消息类型") @RequestParam(required = false) String type,
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam("每页数量") @RequestParam(defaultValue = "10") Integer pageSize) {
        log.info("搜索消息请求: keywords={}, conversationId={}, type={}", keywords, conversationId, type);
        
        Long currentUserId = 1L;
        
        if (conversationId != null) {
            ImConversationMember member = imConversationMemberService
                .selectImConversationMemberByConversationIdAndUserId(conversationId, currentUserId);
            if (member == null) {
                log.warn("用户不在会话中: conversationId={}, userId={}", conversationId, currentUserId);
                throw new BusinessException(403, "用户不在会话中");
            }
            
            List<ImMessage> messages = imMessageService.selectImMessageListByConversationId(conversationId);
            
            List<ImMessage> filteredMessages = messages.stream()
                .filter(msg -> type == null || msg.getType().equals(type))
                .filter(msg -> keywords == null || 
                    (msg.getContent() != null && msg.getContent().contains(keywords)))
                .collect(Collectors.toList());
            
            int start = (pageNum - 1) * pageSize;
            int end = Math.min(start + pageSize, filteredMessages.size());
            
            List<ImMessage> pagedMessages = start < filteredMessages.size() ? 
                filteredMessages.subList(start, end) : java.util.Collections.emptyList();
            
            Map<String, Object> pageResult = new HashMap<>();
            pageResult.put("rows", pagedMessages);
            pageResult.put("total", filteredMessages.size());
            pageResult.put("pageNum", pageNum);
            pageResult.put("pageSize", pageSize);
            
            log.info("搜索消息成功: count={}", filteredMessages.size());
            return Result.success(pageResult);
        }
        
        return Result.success(new HashMap<>());
    }

    @ApiOperation("获取消息详情")
    @GetMapping("/{messageId}")
    public Result<ImMessage> getMessageDetail(@PathVariable @Positive Long messageId) {
        log.info("获取消息详情请求: messageId={}", messageId);
        
        ImMessage message = imMessageService.selectImMessageById(messageId);
        if (message == null) {
            log.warn("消息不存在: messageId={}", messageId);
            throw new BusinessException(404, "消息不存在");
        }
        
        return Result.success(message);
    }

    @ApiOperation("获取未读消息数量")
    @GetMapping("/unread/count")
    public Result<Integer> getUnreadCount() {
        log.info("获取未读消息数量请求");
        
        Long currentUserId = 1L;
        
        List<ImConversationMember> members = imConversationMemberService.selectImConversationMemberListByUserId(currentUserId);
        int unreadCount = members.stream()
            .mapToInt(member -> member.getUnreadCount() != null ? member.getUnreadCount() : 0)
            .sum();
        
        log.info("获取未读消息数量成功: count={}", unreadCount);
        return Result.success(unreadCount);
    }

    @ApiOperation("获取会话未读消息数量")
    @GetMapping("/unread/conversation/{conversationId}")
    public Result<Integer> getConversationUnreadCount(@PathVariable @Positive Long conversationId) {
        log.info("获取会话未读消息数量请求: conversationId={}", conversationId);
        
        Long currentUserId = 1L;
        
        ImConversationMember member = imConversationMemberService
            .selectImConversationMemberByConversationIdAndUserId(conversationId, currentUserId);
        
        if (member == null) {
            log.warn("用户不在会话中: conversationId={}, userId={}", conversationId, currentUserId);
            throw new BusinessException(404, "用户不在会话中");
        }
        
        int unreadCount = member.getUnreadCount() != null ? member.getUnreadCount() : 0;
        log.info("获取会话未读消息数量成功: conversationId={}, count={}", conversationId, unreadCount);
        return Result.success(unreadCount);
    }

    @ApiOperation("回复消息")
    @PostMapping("/reply")
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
    public Result<List<ImMessageReaction>> getReactionList(@PathVariable @Positive Long messageId) {
        log.info("获取消息互动列表请求: messageId={}", messageId);
        
        List<ImMessageReaction> reactions = imMessageReactionService.selectImMessageReactionListByMessageId(messageId);
        log.info("获取消息互动列表成功: messageId={}, count={}", messageId, reactions.size());
        return Result.success(reactions);
    }

    @ApiOperation("发送消息已读回执")
    @PostMapping("/readReceipt/send")
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
    public Result<List<ImMessageReadReceipt>> getReadReceiptList(@PathVariable @Positive Long messageId) {
        log.info("获取消息已读回执列表请求: messageId={}", messageId);
        
        List<ImMessageReadReceipt> readReceipts = imMessageReadReceiptService.selectImMessageReadReceiptListByMessageId(messageId);
        log.info("获取消息已读回执列表成功: messageId={}, count={}", messageId, readReceipts.size());
        return Result.success(readReceipts);
    }

    @ApiOperation("获取会话已读回执列表")
    @GetMapping("/readReceipt/conversation/{conversationId}")
    public Result<List<ImMessageReadReceipt>> getConversationReadReceiptList(@PathVariable @Positive Long conversationId) {
        log.info("获取会话已读回执列表请求: conversationId={}", conversationId);
        
        List<ImMessageReadReceipt> readReceipts = imMessageReadReceiptService.selectImMessageReadReceiptListByConversationId(conversationId);
        log.info("获取会话已读回执列表成功: conversationId={}, count={}", conversationId, readReceipts.size());
        return Result.success(readReceipts);
    }
}
