package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.domain.ImMessageMention;
import com.ruoyi.im.service.ImMessageMentionService;
import com.ruoyi.im.util.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @提及控制器
 *
 * @author ruoyi
 */

@RestController
@RequestMapping("/api/im/mention")
public class ImMentionController {

    private static final Logger log = LoggerFactory.getLogger(ImMentionController.class);

    private final ImMessageMentionService mentionService;

    /**
     * 构造器注入依赖
     *
     * @param mentionService 提及服务
     */
    public ImMentionController(ImMessageMentionService mentionService) {
        this.mentionService = mentionService;
    }

    /**
     * 获取会话中可以@的用户列表
     *
     * @param conversationId 会话ID
     * @param keyword        搜索关键词
     * @return 可@的用户列表
     */
    
    @GetMapping("/users/{conversationId}")
    public Result<List<Map<String, Object>>> getMentionableUsers(
            @PathVariable Long conversationId,
            @RequestParam(required = false) String keyword) {
        Long userId = SecurityUtils.getLoginUserId();
        List<Map<String, Object>> users = mentionService.getMentionableUsers(conversationId, keyword);
        return Result.success(users);
    }

    /**
     * 检查用户是否可以@所有人
     *
     * @param conversationId 会话ID
     * @return 是否可以@所有人
     */
    
    @GetMapping("/can-mention-all/{conversationId}")
    public Result<Boolean> canMentionAll(
            @PathVariable Long conversationId) {
        Long userId = SecurityUtils.getLoginUserId();
        boolean canMention = mentionService.canMentionAll(conversationId, userId);
        return Result.success(canMention);
    }

    /**
     * 获取用户未读的@提及列表
     *
     * @return 未读@提及列表
     */
    
    @GetMapping("/unread")
    public Result<List<ImMessageMention>> getUnreadMentions() {
        Long userId = SecurityUtils.getLoginUserId();
        List<ImMessageMention> mentions = mentionService.getUnreadMentions(userId);
        return Result.success(mentions);
    }

    /**
     * 获取用户未读的@提及数量
     *
     * @return 未读数量
     */
    
    @GetMapping("/unread/count")
    public Result<Integer> getUnreadMentionCount() {
        Long userId = SecurityUtils.getLoginUserId();
        int count = mentionService.getUnreadMentionCount(userId);
        return Result.success(count);
    }

    /**
     * 标记@提及为已读
     *
     * @param messageId 消息ID
     * @return 操作结果
     */
    
    @PutMapping("/{messageId}/read")
    public Result<Void> markMentionAsRead(
            @PathVariable Long messageId) {
        Long userId = SecurityUtils.getLoginUserId();
        mentionService.markAsRead(messageId, userId);
        return Result.success("已标记为已读");
    }

    /**
     * 批量标记@提及为已读
     *
     * @param mentionIds 提及ID列表
     * @return 操作结果
     */
    
    @PutMapping("/read/batch")
    public Result<Void> batchMarkMentionsAsRead(@RequestBody List<Long> mentionIds) {
        mentionService.batchMarkAsRead(mentionIds);
        return Result.success("已批量标记为已读");
    }
}

