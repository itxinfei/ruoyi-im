package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.domain.ImMessageMention;
import com.ruoyi.im.dto.message.ImMessageForwardRequest;
import com.ruoyi.im.dto.message.ImMessageRecallRequest;
import com.ruoyi.im.dto.message.ImMessageReplyRequest;
import com.ruoyi.im.dto.message.ImMessageSendRequest;
import com.ruoyi.im.dto.message.ImMessageSearchRequest;
import com.ruoyi.im.dto.reaction.ImMessageReactionAddRequest;
import com.ruoyi.im.service.ImMessageMentionService;
import com.ruoyi.im.service.ImMessageReactionService;
import com.ruoyi.im.service.ImMessageService;
import com.ruoyi.im.vo.message.ImMessageSearchResultVO;
import com.ruoyi.im.vo.message.ImMessageVO;
import com.ruoyi.im.vo.reaction.ImMessageReactionVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

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

    @Autowired
    private ImMessageService imMessageService;

    @Autowired
    private ImMessageReactionService reactionService;

    @Autowired
    private ImMessageMentionService mentionService;

    /**
     * 发送消息
     * 发送文本、图片、文件等类型的消息到指定会话
     *
     * @param request 消息发送请求参数，包含会话ID、消息类型、消息内容等
     * @param userId 当前登录用户ID，从请求头中获取
     * @return 发送结果，包含新消息ID
     * @apiNote 使用 @Valid 注解进行参数校验，消息发送后会通过WebSocket推送给接收方
     * @throws BusinessException 当会话不存在或发送失败时抛出业务异常
     */
    @PostMapping("/send")
    public Result<Long> send(@Valid @RequestBody ImMessageSendRequest request,
                            @RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 1L;
        }
        Long messageId = imMessageService.sendMessage(request, userId);
        return Result.success("发送成功", messageId);
    }

    /**
     * 获取会话消息列表
     * 分页查询指定会话的历史消息，支持从指定消息ID开始查询
     *
     * @param conversationId 会话ID
     * @param lastId 最后一条消息ID，用于分页查询（可选）
     * @param limit 每页消息数量，默认20条
     * @param userId 当前登录用户ID，从请求头中获取
     * @return 消息列表，按发送时间倒序排列
     * @apiNote 返回的消息会标记是否为当前用户发送的消息（isSelf字段）
     * @throws BusinessException 当会话不存在时抛出业务异常
     */
    @GetMapping("/list/{conversationId}")
    public Result<List<ImMessageVO>> getMessages(
            @PathVariable Long conversationId,
            @RequestParam(required = false) Long lastId,
            @RequestParam(required = false, defaultValue = "20") Integer limit,
            @RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 1L;
        }
        List<ImMessageVO> list = imMessageService.getMessages(conversationId, userId, lastId, limit);
        return Result.success(list);
    }

    /**
     * 撤回消息
     * 撤回已发送的消息，撤回后消息内容将显示为"消息已撤回"
     *
     * @param messageId 消息ID
     * @param userId 当前登录用户ID，从请求头中获取
     * @return 撤回结果
     * @apiNote 消息撤回有时间限制（如2分钟），超时后无法撤回；撤回后会通过WebSocket通知接收方
     * @throws BusinessException 当消息不存在、无权限撤回或超时时抛出业务异常
     */
    @DeleteMapping("/{messageId}/recall")
    public Result<Void> recall(@PathVariable Long messageId,
                               @RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 1L;
        }
        imMessageService.recallMessage(messageId, userId);
        return Result.success("消息已撤回");
    }

    /**
     * 标记消息已读
     * 批量标记指定消息为已读状态，并更新会话未读消息数
     *
     * @param messageIds 消息ID列表
     * @param userId 当前登录用户ID，从请求头中获取
     * @return 标记结果
     * @apiNote 标记已读后会更新会话的未读消息数，并通过WebSocket推送已读回执给发送方
     * @throws BusinessException 当消息不存在或会话不存在时抛出业务异常
     */
    @PutMapping("/read")
    public Result<Void> markAsRead(@RequestBody List<Long> messageIds,
                                  @RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 1L;
        }
        Long conversationId = 1L;
        imMessageService.markAsRead(conversationId, userId, messageIds);
        return Result.success("已标记为已读");
    }

    /**
     * 转发消息
     * 将消息转发到其他会话或用户
     *
     * @param request 转发请求参数，包含原消息ID、目标会话ID、目标用户ID等
     * @param userId 当前登录用户ID，从请求头中获取
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
                userId
        );
        return Result.success("转发成功", newMessageId);
    }

    /**
     * 回复消息
     * 引用原消息进行回复
     *
     * @param request 回复请求参数，包含原消息ID和回复内容
     * @param userId 当前登录用户ID，从请求头中获取
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
                userId
        );
        return Result.success("回复成功", newMessageId);
    }

    /**
     * 添加消息表情反应
     * 对消息添加emoji表情反应（类似微信点赞、钉钉表情回复）
     * 再次使用相同表情会取消反应
     *
     * @param request 反应请求参数
     * @param userId 当前登录用户ID
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
        // TODO: 广播消息反应更新给会话中的其他用户

        return Result.success("反应成功", result);
    }

    /**
     * 删除消息表情反应
     * 取消对消息的emoji表情反应
     *
     * @param messageId 消息ID
     * @param userId 当前登录用户ID
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
        // TODO: 广播消息反应更新给会话中的其他用户

        return Result.success("已取消反应");
    }

    /**
     * 获取消息的表情反应列表
     * 获取指定消息的所有表情反应
     *
     * @param messageId 消息ID
     * @param userId 当前登录用户ID
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
     * @param userId 当前登录用户ID
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
     * @param userId 当前登录用户ID
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
     * @param userId 当前登录用户ID
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
                userId
        );
        return Result.success(result);
    }
}
