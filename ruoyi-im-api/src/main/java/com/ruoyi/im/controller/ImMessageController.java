package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.dto.message.ImMessageForwardRequest;
import com.ruoyi.im.dto.message.ImMessageRecallRequest;
import com.ruoyi.im.dto.message.ImMessageReplyRequest;
import com.ruoyi.im.dto.message.ImMessageSendRequest;
import com.ruoyi.im.service.ImMessageService;
import com.ruoyi.im.vo.message.ImMessageVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 消息控制器
 * 提供消息发送、消息列表查询、消息撤回、消息已读标记等功能
 *
 * @author ruoyi
 */
@Tag(name = "消息管理", description = "消息发送、查询、撤回、转发、回复等接口")
@RestController
@RequestMapping("/api/im/message")
public class ImMessageController {

    @Autowired
    private ImMessageService imMessageService;

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
     * @param sessionId 会话ID
     * @param lastId 最后一条消息ID，用于分页查询（可选）
     * @param limit 每页消息数量，默认20条
     * @param userId 当前登录用户ID，从请求头中获取
     * @return 消息列表，按发送时间倒序排列
     * @apiNote 返回的消息会标记是否为当前用户发送的消息（isSelf字段）
     * @throws BusinessException 当会话不存在时抛出业务异常
     */
    @GetMapping("/list/{sessionId}")
    public Result<List<ImMessageVO>> getMessages(
            @PathVariable Long sessionId,
            @RequestParam(required = false) Long lastId,
            @RequestParam(required = false, defaultValue = "20") Integer limit,
            @RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 1L;
        }
        List<ImMessageVO> list = imMessageService.getMessages(sessionId, userId, lastId, limit);
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
        Long sessionId = 1L;
        imMessageService.markAsRead(sessionId, userId, messageIds);
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
                request.getToSessionId(),
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
}
