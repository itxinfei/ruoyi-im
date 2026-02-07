package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.service.ImConversationService;
import com.ruoyi.im.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Positive;
import java.util.List;
import java.util.Map;

/**
 * 消息已读回执控制器
 * 提供消息已读状态查询、未读数统计等功能
 *
 * @author ruoyi
 */
@Tag(name = "消息已读回执", description = "消息已读状态查询、未读数统计等接口")
@RestController
@RequestMapping("/api/im/message/receipt")
@Validated
public class ImMessageReceiptController {

    private static final Logger log = LoggerFactory.getLogger(ImMessageReceiptController.class);

    private final ImConversationService conversationService;

    /**
     * 构造器注入依赖
     */
    public ImMessageReceiptController(ImConversationService conversationService) {
        this.conversationService = conversationService;
    }

    /**
     * 获取会话未读消息数
     *
     * @param conversationId 会话ID
     * @return 未读消息数
     */
    @Operation(summary = "获取会话未读消息数", description = "获取指定会话中当前用户的未读消息数量")
    @GetMapping("/unread/{conversationId}")
    public Result<Integer> getUnreadCount(@PathVariable @Positive(message = "会话ID必须为正数") Long conversationId) {
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
     * 获取消息已读状态
     *
     * @param conversationId 会话ID
     * @param messageId      消息ID
     * @return 已读用户列表
     */
    @Operation(summary = "获取消息已读状态", description = "获取指定消息的已读用户列表")
    @GetMapping("/read-status/{conversationId}/{messageId}")
    public Result<List<Map<String, Object>>> getReadStatus(
            @PathVariable @Positive(message = "会话ID必须为正数") Long conversationId,
            @PathVariable @Positive(message = "消息ID必须为正数") Long messageId) {
        Long userId = SecurityUtils.getLoginUserId();

        try {
            List<Map<String, Object>> readUsers = conversationService.getReadStatus(conversationId, messageId);
            return Result.success(readUsers);
        } catch (Exception e) {
            log.error("获取已读状态失败: conversationId={}, messageId={}", conversationId, messageId, e);
            return Result.fail("获取已读状态失败");
        }
    }
}
