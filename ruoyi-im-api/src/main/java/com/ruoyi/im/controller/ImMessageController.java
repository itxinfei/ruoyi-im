package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.dto.message.ImMessageSendRequest;
import com.ruoyi.im.service.ImMessageService;
import com.ruoyi.im.vo.message.ImMessageVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 消息控制器
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/api/im/message")
public class ImMessageController {

    @Autowired
    private ImMessageService imMessageService;

    /**
     * 发送消息
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
     */
    @PutMapping("/read")
    public Result<Void> markAsRead(@RequestBody List<Long> messageIds,
                                  @RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 1L;
        }
        Long sessionId = 1L; // TODO: 从请求中获取会话ID
        imMessageService.markAsRead(sessionId, userId, messageIds);
        return Result.success("已标记为已读");
    }
}
