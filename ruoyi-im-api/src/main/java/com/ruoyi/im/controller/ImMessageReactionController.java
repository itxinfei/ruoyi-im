package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.dto.reaction.ImMessageReactionAddRequest;
import com.ruoyi.im.service.ImMessageReactionService;
import com.ruoyi.im.util.SecurityUtils;
import com.ruoyi.im.vo.reaction.ImMessageReactionVO;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 消息表情回应控制器
 */
@RestController
@RequestMapping("/api/im/message/reaction")
public class ImMessageReactionController {

    private final ImMessageReactionService reactionService;

    public ImMessageReactionController(ImMessageReactionService reactionService) {
        this.reactionService = reactionService;
    }

    /**
     * 添加表情回应
     */
    @PostMapping("/add")
    public Result<Void> addReaction(@Valid @RequestBody ImMessageReactionAddRequest request) {
        Long userId = SecurityUtils.getLoginUserId();
        reactionService.addReaction(request, userId);
        return Result.success();
    }

    /**
     * 切换表情回应（添加/移除）
     */
    @PostMapping("/toggle")
    public Result<Void> toggleReaction(@RequestBody ImMessageReactionAddRequest request) {
        Long userId = SecurityUtils.getLoginUserId();
        reactionService.addReaction(request, userId);
        return Result.success();
    }

    /**
     * 获取消息的所有表情回应
     */
    @GetMapping("/{messageId}")
    public Result<List<ImMessageReactionVO>> getReactions(@PathVariable Long messageId) {
        Long userId = SecurityUtils.getLoginUserId();
        List<ImMessageReactionVO> reactions = reactionService.getMessageReactions(messageId, userId);
        return Result.success(reactions);
    }

    /**
     * 删除自己的表情回应
     */
    @DeleteMapping("/{messageId}")
    public Result<Void> removeReaction(@PathVariable Long messageId) {
        Long userId = SecurityUtils.getLoginUserId();
        reactionService.removeReaction(messageId, userId);
        return Result.success();
    }
}
