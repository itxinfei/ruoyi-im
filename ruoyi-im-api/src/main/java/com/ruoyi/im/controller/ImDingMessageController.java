package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.domain.ImDingMessage;
import com.ruoyi.im.domain.ImDingTemplate;
import com.ruoyi.im.dto.ding.DingSendRequest;
import com.ruoyi.im.service.ImDingMessageService;
import com.ruoyi.im.util.SecurityUtils;
import com.ruoyi.im.vo.ding.DingDetailVO;
import com.ruoyi.im.vo.ding.DingReceiptVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * DING消息控制器
 */
@Tag(name = "DING消息", description = "DING消息管理接口")
@RestController
@RequestMapping("/api/im/ding-messages")
public class ImDingMessageController {

    private final ImDingMessageService dingMessageService;

    /**
     * 构造器注入依赖
     *
     * @param dingMessageService DING消息服务
     */
    public ImDingMessageController(ImDingMessageService dingMessageService) {
        this.dingMessageService = dingMessageService;
    }

    /**
     * 发送DING消息
     */
    @Operation(summary = "发送DING消息")
    @PostMapping("/send")
    public Result<Long> sendDing(
            @Valid @RequestBody DingSendRequest request) {
        Long userId = SecurityUtils.getLoginUserId();
        Long dingId = dingMessageService.sendDing(request, userId);
        return Result.success("DING发送成功", dingId);
    }

    /**
     * 获取接收的DING列表
     */
    @Operation(summary = "获取接收的DING列表")
    @GetMapping("/received")
    public Result<List<ImDingMessage>> getReceivedDingList() {
        Long userId = SecurityUtils.getLoginUserId();
        List<ImDingMessage> list = dingMessageService.getReceivedDingList(userId);
        return Result.success(list);
    }

    /**
     * 获取发送的DING列表
     */
    @Operation(summary = "获取发送的DING列表")
    @GetMapping("/sent")
    public Result<List<ImDingMessage>> getSentDingList() {
        Long userId = SecurityUtils.getLoginUserId();
        List<ImDingMessage> list = dingMessageService.getSentDingList(userId);
        return Result.success(list);
    }

    /**
     * 获取DING详情
     */
    @Operation(summary = "获取DING详情")
    @GetMapping("/{dingId}")
    public Result<DingDetailVO> getDingDetail(
            @PathVariable Long dingId) {
        Long userId = SecurityUtils.getLoginUserId();
        DingDetailVO detail = dingMessageService.getDingDetail(dingId, userId);
        return Result.success(detail);
    }

    /**
     * 阅读DING
     */
    @Operation(summary = "阅读DING")
    @PutMapping("/{dingId}/read")
    public Result<Void> readDing(
            @PathVariable Long dingId) {
        Long userId = SecurityUtils.getLoginUserId();
        dingMessageService.readDing(dingId, userId);
        return Result.success("已标记为已读");
    }

    /**
     * 确认DING
     */
    @Operation(summary = "确认DING")
    @PutMapping("/{dingId}/confirm")
    public Result<Void> confirmDing(
            @PathVariable Long dingId,
            @RequestParam(required = false) String remark) {
        Long userId = SecurityUtils.getLoginUserId();
        dingMessageService.confirmDing(dingId, userId, remark);
        return Result.success("确认成功");
    }

    /**
     * 获取DING回执列表
     */
    @Operation(summary = "获取DING回执列表")
    @GetMapping("/{dingId}/receipts")
    public Result<List<DingReceiptVO>> getDingReceipts(
            @PathVariable Long dingId) {
        Long userId = SecurityUtils.getLoginUserId();
        List<DingReceiptVO> receipts = dingMessageService.getDingReceipts(dingId, userId);
        return Result.success(receipts);
    }

    /**
     * 取消定时DING
     */
    @Operation(summary = "取消定时DING")
    @PutMapping("/{dingId}/cancel")
    public Result<Void> cancelScheduledDing(
            @PathVariable Long dingId) {
        Long userId = SecurityUtils.getLoginUserId();
        dingMessageService.cancelScheduledDing(dingId, userId);
        return Result.success("已取消");
    }

    /**
     * 获取DING模板列表
     */
    @Operation(summary = "获取DING模板列表")
    @GetMapping("/templates")
    public Result<List<ImDingTemplate>> getTemplates(
            @RequestParam(required = false) String category) {
        List<ImDingTemplate> templates = dingMessageService.getTemplateList(category);
        return Result.success(templates);
    }

    /**
     * 使用模板创建DING
     */
    @Operation(summary = "使用模板创建DING")
    @PostMapping("/template/{templateId}")
    public Result<Long> createFromTemplate(
            @PathVariable Long templateId,
            @RequestParam String params) {
        Long userId = SecurityUtils.getLoginUserId();
        Long dingId = dingMessageService.createFromTemplate(templateId, params, userId);
        return Result.success("创建成功", dingId);
    }
}
