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
import com.ruoyi.im.service.ImMessageMentionService;
import com.ruoyi.im.service.ImMessageReactionService;
import com.ruoyi.im.service.ImMessageService;
import com.ruoyi.im.service.ImWebSocketBroadcastService;
import com.ruoyi.im.vo.message.ImMessageSearchResultVO;
import com.ruoyi.im.vo.message.ImMessageVO;
import com.ruoyi.im.vo.reaction.ImMessageReactionVO;
import com.ruoyi.im.websocket.ImWebSocketEndpoint;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
@Tag(name = "消息管理", description = "消息发送、查询、撤回、转发、回复、表情反应等接口")
@RestController
@RequestMapping("/api/im/message")
public class ImMessageController {

    private static final Logger log = LoggerFactory.getLogger(ImMessageController.class);
    private static final Long DEFAULT_USER_ID = 1L;

    private final ImMessageService imMessageService;
    private final ImMessageReactionService reactionService;
    private final ImMessageMentionService mentionService;
    private final ImWebSocketBroadcastService broadcastService;

    @Autowired
    private com.ruoyi.im.mapper.ImConversationMemberMapper conversationMemberMapper;

    @Autowired
    private com.ruoyi.im.mapper.ImMessageMapper imMessageMapper;

    public ImMessageController(
            ImMessageService imMessageService,
            ImMessageReactionService reactionService,
            ImMessageMentionService mentionService,
            ImWebSocketBroadcastService broadcastService) {
        this.imMessageService = imMessageService;
        this.reactionService = reactionService;
        this.mentionService = mentionService;
        this.broadcastService = broadcastService;
    }

    @PostMapping("/send")
    public Result<ImMessageVO> send(@Valid @RequestBody ImMessageSendRequest request,
            @RequestHeader(value = "userId", required = false) Long userId) {
        userId = getUserIdOrDefault(userId);
        ImMessageVO messageVO = imMessageService.sendMessage(request, userId);
        return Result.success(messageVO);
    }

    private Long getUserIdOrDefault(Long userId) {
        return userId != null ? userId : DEFAULT_USER_ID;
    }

    @GetMapping("/list/{conversationId}")
    public Result<List<ImMessageVO>> getMessages(
            @PathVariable Long conversationId,
            @RequestParam(required = false) Long lastId,
            @RequestParam(required = false, defaultValue = "20") Integer limit,
            @RequestHeader(value = "userId", required = false) Long userId) {
        // 调试日志：记录接收到的请求头 userId
        log.info("getMessages API - 收到请求 header userId={}, 原始值={}, conversationId={}", userId, userId, conversationId);
        userId = getUserIdOrDefault(userId);
        log.info("getMessages API - 使用 userId={}", userId);
        List<ImMessageVO> list = imMessageService.getMessages(conversationId, userId, lastId, limit);
        return Result.success(list);
    }

    @DeleteMapping("/{messageId}/recall")
    public Result<Void> recall(@PathVariable Long messageId,
            @RequestHeader(value = "userId", required = false) Long userId) {
        userId = getUserIdOrDefault(userId);
        imMessageService.recallMessage(messageId, userId);
        return Result.success("消息已撤回");
    }

    @DeleteMapping("/{messageId}")
    public Result<Void> deleteMessage(@PathVariable Long messageId,
            @RequestHeader(value = "userId", required = false) Long userId) {
        userId = getUserIdOrDefault(userId);
        imMessageService.deleteMessage(messageId, userId);
        return Result.success("消息已删除");
    }

    @PutMapping("/{messageId}/edit")
    public Result<Void> edit(
            @PathVariable Long messageId,
            @RequestBody MessageEditRequest request,
            @RequestHeader(value = "userId", required = false) Long userId) {
        userId = getUserIdOrDefault(userId);
        imMessageService.editMessage(messageId, request.getNewContent(), userId);
        return Result.success("消息已编辑");
    }

    /**
     * 标记消息已读
     * 批量标记指定消息为已读状态，并更新会话未读消息数
     *
     * @param data   包含conversationId和messageIds的请求数据
     * @param userId 当前登录用户ID，从请求头中获取
     * @return 标记结果
     * @apiNote 标记已读后会更新会话的未读消息数，并通过WebSocket推送已读回执给发送方
     * @throws BusinessException 当消息不存在或会话不存在时抛出业务异常
     */
    @Operation(summary = "标记消息已读", description = "批量标记指定消息为已读状态")
    @PutMapping("/mark-read")
    public Result<Void> markAsRead(@RequestBody java.util.Map<String, Object> data,
            @RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 1L;
        }
        Long conversationId = data.get("conversationId") != null ? Long.valueOf(data.get("conversationId").toString())
                : null;
        @SuppressWarnings("unchecked")
        List<Long> messageIds = (List<Long>) data.get("messageIds");
        imMessageService.markAsRead(conversationId, userId, messageIds);
        return Result.success("已标记为已读");
    }

    /**
     * 标记会话消息已读（兼容前端API）
     * 将会话中指定消息ID之前的所有消息标记为已读
     *
     * @param conversationId    会话ID
     * @param lastReadMessageId 最后已读消息ID（该消息之前的所有消息都标记为已读）
     * @param userId            当前登录用户ID
     * @return 操作结果
     */
    @Operation(summary = "标记会话已读", description = "将会话中指定消息之前的所有消息标记为已读")
    @PutMapping("/read")
    public Result<Void> markConversationRead(
            @RequestParam Long conversationId,
            @RequestParam(required = false) Long lastReadMessageId,
            @RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 1L;
        }
        // 更新会话成员的最后已读消息ID
        ImConversationMember member = conversationMemberMapper.selectByConversationIdAndUserId(conversationId, userId);
        if (member != null) {
            if (lastReadMessageId != null) {
                member.setLastReadMessageId(lastReadMessageId);
            } else {
                // 获取会话最新消息ID
                List<ImMessageVO> messages = imMessageService.getMessages(conversationId, userId, null, 1);
                if (messages != null && !messages.isEmpty()) {
                    member.setLastReadMessageId(messages.get(0).getId());
                }
            }
            member.setLastReadTime(LocalDateTime.now());
            conversationMemberMapper.updateById(member);
        }
        return Result.success("已标记为已读");
    }

    /**
     * 转发消息
     * 将消息转发到其他会话或用户
     *
     * @param request 转发请求参数，包含原消息ID、目标会话ID、目标用户ID等
     * @param userId  当前登录用户ID，从请求头中获取
     * @return 转发结果，包含新消息ID
     * @apiNote 转发时会创建新消息，原消息内容会被复制到新消息中
     */
    @Operation(summary = "转发消息", description = "将消息转发到其他会话或用户")
    @PostMapping("/forward")
    public Result<Long> forward(@Valid @RequestBody ImMessageForwardRequest request,
            @RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 1L;
        }
        Long newMessageId = imMessageService.forwardMessage(
                request.getMessageId(),
                request.getToConversationId(),
                request.getToUserId(),
                request.getContent(),
                userId);
        return Result.success("转发成功", newMessageId);
    }

    /**
     * 回复消息
     * 引用原消息进行回复
     *
     * @param request 回复请求参数，包含原消息ID和回复内容
     * @param userId  当前登录用户ID，从请求头中获取
     * @return 回复结果，包含新消息ID
     * @apiNote 回复消息会关联原消息ID，通过parentId字段建立消息引用关系
     */
    @Operation(summary = "回复消息", description = "引用原消息进行回复")
    @PostMapping("/reply")
    public Result<Long> reply(@Valid @RequestBody ImMessageReplyRequest request,
            @RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 1L;
        }
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
     * @param userId  当前登录用户ID
     * @return 反应结果
     * @apiNote 支持多个用户对同一条消息添加不同表情反应
     */
    @Operation(summary = "添加表情反应", description = "对消息添加emoji表情反应")
    @PostMapping("/{messageId}/reaction")
    public Result<ImMessageReactionVO> addReaction(
            @PathVariable Long messageId,
            @Valid @RequestBody ImMessageReactionAddRequest request,
            @RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 1L;
        }
        request.setMessageId(messageId);
        ImMessageReactionVO result = reactionService.addReaction(request, userId);

        if (result == null) {
            return Result.success("已取消反应", (ImMessageReactionVO) null);
        }

        // 通过WebSocket推送反应更新通知
        ImMessage message = imMessageMapper.selectImMessageById(messageId);
        if (message != null) {
            // TODO: Implement broadcastReactionUpdate method
            // broadcastReactionUpdate(message.getConversationId(), messageId, userId,
            // request.getEmoji(), "add");
        }

        return Result.success("反应成功", result);
    }

    /**
     * 删除消息表情反应
     * 取消对消息的emoji表情反应
     *
     * @param messageId 消息ID
     * @param userId    当前登录用户ID
     * @return 删除结果
     */
    @Operation(summary = "删除表情反应", description = "取消对消息的emoji表情反应")
    @DeleteMapping("/{messageId}/reaction")
    public Result<Void> removeReaction(
            @PathVariable Long messageId,
            @RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 1L;
        }
        reactionService.removeReaction(messageId, userId);

        // 通过WebSocket推送反应更新通知
        ImMessage message = imMessageMapper.selectImMessageById(messageId);
        if (message != null) {
            // TODO: Implement broadcastReactionUpdate method
            // broadcastReactionUpdate(message.getConversationId(), messageId, userId, null,
            // "remove");
        }

        return Result.success("已取消反应");
    }

    /**
     * 获取消息的表情反应列表
     * 获取指定消息的所有表情反应
     *
     * @param messageId 消息ID
     * @param userId    当前登录用户ID
     * @return 反应列表
     */
    @Operation(summary = "获取表情反应列表", description = "获取消息的所有表情反应")
    @GetMapping("/{messageId}/reactions")
    public Result<List<ImMessageReactionVO>> getMessageReactions(
            @PathVariable Long messageId,
            @RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 1L;
        }
        List<ImMessageReactionVO> reactions = reactionService.getMessageReactions(messageId, userId);
        return Result.success(reactions);
    }

    /**
     * 获取消息的表情反应统计
     * 获取消息的表情反应统计信息
     *
     * @param messageId 消息ID
     * @param userId    当前登录用户ID
     * @return 反应统计列表
     */
    @Operation(summary = "获取表情反应统计", description = "获取消息的表情反应统计")
    @GetMapping("/{messageId}/reactions/stats")
    public Result<List<ImMessageReactionVO>> getReactionStats(
            @PathVariable Long messageId,
            @RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 1L;
        }
        List<ImMessageReactionVO> stats = reactionService.getReactionStats(messageId, userId);
        return Result.success(stats);
    }

    // ==================== @提及功能接口 ====================

    /**
     * 获取用户未读的@提及列表
     * 查询当前用户被@的所有未读消息
     *
     * @param userId 当前登录用户ID
     * @return 未读@提及列表
     * @apiNote 返回包含消息详情、@发送者信息的列表
     */
    @Operation(summary = "获取未读@提及", description = "获取当前用户被@的所有未读消息")
    @GetMapping("/mention/unread")
    public Result<List<ImMessageMention>> getUnreadMentions(
            @RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 1L;
        }
        List<ImMessageMention> mentions = mentionService.getUnreadMentions(userId);
        return Result.success(mentions);
    }

    /**
     * 获取用户未读的@提及数量
     * 获取当前用户被@的未读消息总数
     *
     * @param userId 当前登录用户ID
     * @return 未读数量
     * @apiNote 用于在界面上显示@提醒角标
     */
    @Operation(summary = "获取未读@提及数量", description = "获取当前用户被@的未读消息总数")
    @GetMapping("/mention/unread/count")
    public Result<Integer> getUnreadMentionCount(
            @RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 1L;
        }
        int count = mentionService.getUnreadMentionCount(userId);
        return Result.success(count);
    }

    /**
     * 标记@提及为已读
     * 标记指定消息的@提及为已读状态
     *
     * @param messageId 消息ID
     * @param userId    当前登录用户ID
     * @return 标记结果
     * @apiNote 标记已读后该消息不会在未读@提及列表中显示
     */
    @Operation(summary = "标记@提及已读", description = "标记指定消息的@提及为已读状态")
    @PutMapping("/{messageId}/mention/read")
    public Result<Void> markMentionAsRead(
            @PathVariable Long messageId,
            @RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 1L;
        }
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
    public Result<Void> batchMarkMentionsAsRead(
            @RequestBody List<Long> mentionIds) {
        mentionService.batchMarkAsRead(mentionIds);
        return Result.success("已批量标记为已读");
    }

    // ==================== 消息搜索接口 ====================

    /**
     * 搜索消息
     * 支持关键词搜索、时间范围筛选、消息类型筛选等多种搜索方式
     *
     * @param request 搜索请求参数
     * @param userId  当前登录用户ID
     * @return 搜索结果
     * @apiNote 支持模糊搜索、精确匹配、时间范围过滤等功能
     */
    @Operation(summary = "搜索消息", description = "支持关键词搜索、时间范围筛选、消息类型筛选等")
    @PostMapping("/search")
    public Result<ImMessageSearchResultVO> searchMessages(
            @RequestBody ImMessageSearchRequest request,
            @RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 1L;
        }
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
     * 标记会话消息已读
     * 将会话中指定消息之前的所有消息标记为已读
     *
     * @param conversationId    会话ID
     * @param lastReadMessageId 最后已读消息ID（该消息之前的所有消息都标记为已读）
     * @param userId            当前登录用户ID
     * @return 操作结果
     */

    /**
     * 获取会话未读消息数
     *
     * @param conversationId 会话ID
     * @param userId         当前登录用户ID
     * @return 未读消息数
     */
    @Operation(summary = "获取会话未读消息数", description = "获取指定会话中当前用户的未读消息数量")
    @GetMapping("/unread/count/{conversationId}")
    public Result<Integer> getUnreadCount(
            @PathVariable Long conversationId,
            @RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 1L;
        }

        try {
            // 获取用户在会话中的信息
            ImConversationMember member = conversationMemberMapper.selectByConversationIdAndUserId(conversationId,
                    userId);

            if (member == null || member.getLastReadMessageId() == null) {
                // 从未读过的会话，未读数 = 会话总消息数
                Long totalCount = imMessageMapper.countByConversationId(conversationId);
                return Result.success(totalCount != null ? totalCount.intValue() : 0);
            }

            // 查询最后已读消息之后的消息数量
            Long unreadCount = imMessageMapper.countUnreadMessages(conversationId, member.getLastReadMessageId());
            return Result.success(unreadCount != null ? unreadCount.intValue() : 0);
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
     * @param userId         当前登录用户ID
     * @return 已读用户列表
     */
    @Operation(summary = "获取消息已读状态", description = "获取指定消息的已读用户列表")
    @GetMapping("/read/status/{conversationId}/{messageId}")
    public Result<List<Map<String, Object>>> getReadStatus(
            @PathVariable Long conversationId,
            @PathVariable Long messageId,
            @RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 1L;
        }

        try {
            // 获取会话所有成员
            List<ImConversationMember> members = conversationMemberMapper.selectByConversationId(conversationId);
            List<Map<String, Object>> readUsers = new java.util.ArrayList<>();

            for (ImConversationMember member : members) {
                // 如果成员的最后已读消息ID >= 当前消息ID，则表示已读
                if (member.getLastReadMessageId() != null &&
                        member.getLastReadMessageId() >= messageId) {
                    Map<String, Object> userInfo = new HashMap<>();
                    userInfo.put("userId", member.getUserId());
                    userInfo.put("readTime", member.getLastReadTime());
                    readUsers.add(userInfo);
                }
            }

            return Result.success(readUsers);
        } catch (Exception e) {
            log.error("获取已读状态失败: conversationId={}, messageId={}", conversationId, messageId, e);
            return Result.fail("获取已读状态失败");
        }
    }

    /**
     * 广播已读回执给会话中的其他用户
     */
    private void broadcastReadReceipt(Long conversationId, Long lastReadMessageId, Long userId) {
        try {
            // 获取会话中的所有成员
            List<ImConversationMember> members = conversationMemberMapper.selectByConversationId(conversationId);
            if (members == null || members.isEmpty()) {
                return;
            }

            // 构建WebSocket消息
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> readReceipt = new HashMap<>();
            readReceipt.put("type", "read");
            readReceipt.put("conversationId", conversationId);
            readReceipt.put("lastReadMessageId", lastReadMessageId);
            readReceipt.put("userId", userId);
            readReceipt.put("timestamp", System.currentTimeMillis());

            String messageJson = mapper.writeValueAsString(readReceipt);

            // 从 WebSocket 端点获取在线用户集合
            Map<Long, javax.websocket.Session> onlineUsers = ImWebSocketEndpoint.getOnlineUsers();

            // 向会话中的每个在线用户发送已读回执（不包括操作者自己）
            for (ImConversationMember member : members) {
                Long targetUserId = member.getUserId();

                // 不发送给操作者自己
                if (targetUserId.equals(userId)) {
                    continue;
                }

                javax.websocket.Session targetSession = onlineUsers.get(targetUserId);
                if (targetSession != null && targetSession.isOpen()) {
                    try {
                        targetSession.getBasicRemote().sendText(messageJson);
                        log.debug("已读回执已推送给用户: userId={}", targetUserId);
                    } catch (Exception e) {
                        log.error("发送已读回执给用户失败: userId={}", targetUserId, e);
                    }
                }
            }
        } catch (Exception e) {
            log.error("广播已读回执异常: conversationId={}", conversationId, e);
        }
    }
}
