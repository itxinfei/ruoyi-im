package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.domain.ImMessageMention;
import com.ruoyi.im.service.ImMessageMentionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @提及控制器
 *
 * @author ruoyi
 */
@Tag(name = "@提及管理", description = "群组@用户、@所有人等功能接口")
@RestController
@RequestMapping("/api/im/mention")
public class ImMentionController {

    private static final Logger log = LoggerFactory.getLogger(ImMentionController.class);

    @Autowired
    private ImMessageMentionService mentionService;

    /**
     * 获取会话中可以@的用户列表
     *
     * @param conversationId 会话ID
     * @param keyword        搜索关键词
     * @param userId         当前用户ID
     * @return 可@的用户列表
     */
    @Operation(summary = "获取可@用户列表", description = "获取会话中可以被@的用户列表，支持关键词搜索")
    @GetMapping("/users/{conversationId}")
    public Result<List<Map<String, Object>>> getMentionableUsers(
            @PathVariable Long conversationId,
            @RequestParam(required = false) String keyword,
            @RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 1L;
        }
        List<Map<String, Object>> users = mentionService.getMentionableUsers(conversationId, keyword);
        return Result.success(users);
    }

    /**
     * 检查用户是否可以@所有人
     *
     * @param conversationId 会话ID
     * @param userId         用户ID
     * @return 是否可以@所有人
     */
    @Operation(summary = "检查是否可@所有人", description = "检查当前用户是否有权限@所有人")
    @GetMapping("/can-mention-all/{conversationId}")
    public Result<Boolean> canMentionAll(
            @PathVariable Long conversationId,
            @RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 1L;
        }
        boolean canMention = mentionService.canMentionAll(conversationId, userId);
        return Result.success(canMention);
    }

    /**
     * 获取用户未读的@提及列表
     *
     * @param userId 用户ID
     * @return 未读@提及列表
     */
    @Operation(summary = "获取未读@提及", description = "获取当前用户被@的所有未读消息")
    @GetMapping("/unread")
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
     *
     * @param userId 用户ID
     * @return 未读数量
     */
    @Operation(summary = "获取未读@提及数量", description = "获取当前用户被@的未读消息总数")
    @GetMapping("/unread/count")
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
     *
     * @param messageId 消息ID
     * @param userId    用户ID
     * @return 操作结果
     */
    @Operation(summary = "标记@提及已读", description = "标记指定消息的@提及为已读状态")
    @PutMapping("/{messageId}/read")
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
     *
     * @param mentionIds 提及ID列表
     * @return 操作结果
     */
    @Operation(summary = "批量标记@提及已读", description = "批量标记多条@提及记录为已读状态")
    @PutMapping("/read/batch")
    public Result<Void> batchMarkMentionsAsRead(@RequestBody List<Long> mentionIds) {
        mentionService.batchMarkAsRead(mentionIds);
        return Result.success("已批量标记为已读");
    }
}
