package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.service.ImMessageReadService;
import com.ruoyi.im.vo.message.ImMessageReadDetailVO;
import com.ruoyi.im.util.SecurityUtils;
import org.springframework.web.bind.annotation.*;

/**
 * 消息读取状态控制器
 */
@RestController
@RequestMapping("/api/im/message/read")
public class ImMessageReadController {

    private final ImMessageReadService messageReadService;

    public ImMessageReadController(ImMessageReadService messageReadService) {
        this.messageReadService = messageReadService;
    }

    /**
     * 获取指定消息的已读详情
     */
    @GetMapping("/detail/{messageId}")
    public Result<ImMessageReadDetailVO> getReadDetail(@PathVariable Long messageId) {
        Long currentUserId = SecurityUtils.getLoginUserId();
        // 严格对齐接口：getMessageReadDetail(Long messageId, Long senderId)
        ImMessageReadDetailVO detail = messageReadService.getMessageReadDetail(messageId, currentUserId);
        return Result.success(detail);
    }
}
