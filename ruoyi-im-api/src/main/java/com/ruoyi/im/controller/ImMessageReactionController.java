package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.domain.ImMessageMention;
import com.ruoyi.im.dto.reaction.ImMessageReactionAddRequest;
import com.ruoyi.im.service.ImMessageReactionService;
import com.ruoyi.im.service.ImMessageMentionService;
import com.ruoyi.im.service.ImMessageService;
import com.ruoyi.im.service.ImWebSocketBroadcastService;
import com.ruoyi.im.util.SecurityUtils;
import com.ruoyi.im.vo.message.ImMessageVO;
import com.ruoyi.im.vo.reaction.ImMessageReactionVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import java.util.List;

/**
 * 消息反应控制器
 * 提供消息表情反应、@提及等功能
 *
 * @author ruoyi
 */
@Tag(name = "消息反应管理", description = "消息表情反应、@提及等接口")
@RestController
@RequestMapping("/api/im/messages/reactions")
@Validated
public class ImMessageReactionController {

    private static final Logger log = LoggerFactory.getLogger(ImMessageReactionController.class);

    private final ImMessageReactionService reactionService;
    private final ImMessageMentionService mentionService;
    private final ImWebSocketBroadcastService broadcastService;
    private final ImMessageService imMessageService;

    /**
     * 构造器注入依赖
     */
    public ImMessageReactionController(
            ImMessageReactionService reactionService,
            ImMessageMentionService mentionService,
            ImWebSocketBroadcastService broadcastService,
            ImMessageService imMessageService) {
        this.reactionService = reactionService;
        this.mentionService = mentionService;
        this.broadcastService = broadcastService;
        this.imMessageService = imMessageService;
    }

    // ==================== 表情反应接口 ====================

    /**
     * 添加消息表情反应
     * 对消息添加emoji表情反应（类似微信点赞、钉钉表情回复）
     * 再次使用相同表情会取消反应
     *
     * @param messageId 消息ID
     * @param request 反应请求参数
     * @return 反应结果
     * @apiNote 支持多个用户对同一条消息添加不同表情反应
     */
    @Operation(summary = "添加表情反应", description = "对消息添加emoji表情反应")
    @PostMapping("/{messageId}")
    public Result<ImMessageReactionVO> addReaction(
            @PathVariable @Positive(message = "消息ID必须为正数") Long messageId,
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
    @DeleteMapping("/{messageId}")
    public Result<Void> removeReaction(@PathVariable @Positive(message = "消息ID必须为正数") Long messageId) {
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
    @GetMapping("/{messageId}/list")
    public Result<List<ImMessageReactionVO>> getMessageReactions(
            @PathVariable @Positive(message = "消息ID必须为正数") Long messageId) {
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
    @GetMapping("/{messageId}/stats")
    public Result<List<ImMessageReactionVO>> getReactionStats(
            @PathVariable @Positive(message = "消息ID必须为正数") Long messageId) {
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
    @PutMapping("/mention/{messageId}/read")
    public Result<Void> markMentionAsRead(@PathVariable @Positive(message = "消息ID必须为正数") Long messageId) {
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
}
