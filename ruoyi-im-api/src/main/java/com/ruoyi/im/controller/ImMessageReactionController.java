package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.dto.reaction.ImMessageReactionAddRequest;
import com.ruoyi.im.service.ImMessageReactionService;
import com.ruoyi.im.util.SecurityUtils;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

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
     * 添加表情回应 (内部处理 Toggle)
     */
    @PostMapping("/add")
    public Result<Void> addReaction(@Valid @RequestBody ImMessageReactionAddRequest request) {
        Long userId = SecurityUtils.getLoginUserId();
        reactionService.addReaction(request, userId);
        return Result.success();
    }
}
