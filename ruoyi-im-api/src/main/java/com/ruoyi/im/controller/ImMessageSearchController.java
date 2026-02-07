package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.dto.message.ImMessageSearchRequest;
import com.ruoyi.im.service.ImMessageService;
import com.ruoyi.im.util.SecurityUtils;
import com.ruoyi.im.vo.message.ImMessageSearchResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 消息搜索控制器
 * 提供消息搜索功能
 *
 * @author ruoyi
 */
@Tag(name = "消息搜索", description = "消息搜索接口")
@RestController
@RequestMapping("/api/im/message/search")
@Validated
public class ImMessageSearchController {

    private final ImMessageService imMessageService;

    /**
     * 构造器注入依赖
     */
    public ImMessageSearchController(ImMessageService imMessageService) {
        this.imMessageService = imMessageService;
    }

    /**
     * 搜索消息
     * 支持关键词搜索、时间范围筛选、消息类型筛选等多种搜索方式
     *
     * @param request 搜索请求参数
     * @return 搜索结果
     * @apiNote 支持模糊搜索、精确匹配、时间范围过滤等功能
     */
    @Operation(summary = "搜索消息", description = "支持关键词搜索、时间范围筛选、消息类型筛选等")
    @PostMapping
    public Result<ImMessageSearchResultVO> searchMessages(
            @Valid @RequestBody ImMessageSearchRequest request) {
        Long userId = SecurityUtils.getLoginUserId();
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
                userId);
        return Result.success(result);
    }
}
