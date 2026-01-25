package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.dto.ding.DingQueryRequest;
import com.ruoyi.im.dto.ding.DingSendRequest;
import com.ruoyi.im.service.ImDingService;
import com.ruoyi.im.util.SecurityUtils;
import com.ruoyi.im.vo.ding.DingMessageVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * DING消息控制器
 *
 * 提供DING强提醒消息的发送、查询、已读标记等功能
 *
 * @author ruoyi
 */
@Tag(name = "DING强提醒", description = "DING强提醒消息接口")
@RestController
@RequestMapping("/api/im/ding")
public class ImDingController {

    private static final Logger log = LoggerFactory.getLogger(ImDingController.class);

    private final ImDingService dingService;

    public ImDingController(ImDingService dingService) {
        this.dingService = dingService;
    }

    /**
     * 发送DING消息
     *
     * @param request DING发送请求
     * @return DING消息VO
     */
    @Operation(summary = "发送DING消息", description = "发送DING强提醒消息")
    @PostMapping("/send")
    public Result<DingMessageVO> send(@Valid @RequestBody DingSendRequest request) {
        Long userId = SecurityUtils.getLoginUserId();
        log.info("发送DING消息: userId={}, conversationId={}", userId, request.getConversationId());
        DingMessageVO vo = dingService.sendDing(request, userId);
        return Result.success("DING消息发送成功", vo);
    }

    /**
     * 查询DING消息列表
     *
     * @param request 查询请求
     * @return DING消息列表
     */
    @Operation(summary = "查询DING消息列表", description = "查询DING消息列表")
    @PostMapping("/list")
    public Result<List<DingMessageVO>> list(@RequestBody DingQueryRequest request) {
        Long userId = SecurityUtils.getLoginUserId();
        List<DingMessageVO> list = dingService.queryDings(request, userId);
        return Result.success(list);
    }

    /**
     * 获取DING消息详情
     *
     * @param dingId DING消息ID
     * @return DING消息VO
     */
    @Operation(summary = "获取DING消息详情", description = "获取DING消息详情")
    @GetMapping("/{dingId}")
    public Result<DingMessageVO> getDetail(@PathVariable Long dingId) {
        Long userId = SecurityUtils.getLoginUserId();
        DingMessageVO vo = dingService.getDingDetail(dingId, userId);
        return Result.success(vo);
    }

    /**
     * 标记DING消息为已读
     *
     * @param dingId DING消息ID
     * @return 操作结果
     */
    @Operation(summary = "标记DING已读", description = "标记DING消息为已读")
    @PutMapping("/{dingId}/read")
    public Result<Void> markAsRead(@PathVariable Long dingId) {
        Long userId = SecurityUtils.getLoginUserId();
        dingService.markAsRead(dingId, userId);
        return Result.success("已标记为已读");
    }

    /**
     * 批量标记DING消息为已读
     *
     * @param dingIds DING消息ID列表
     * @return 操作结果
     */
    @Operation(summary = "批量标记DING已读", description = "批量标记DING消息为已读")
    @PutMapping("/read/batch")
    public Result<Void> batchMarkAsRead(@RequestBody List<Long> dingIds) {
        Long userId = SecurityUtils.getLoginUserId();
        for (Long dingId : dingIds) {
            dingService.markAsRead(dingId, userId);
        }
        return Result.success("已批量标记为已读");
    }

    /**
     * 取消DING消息
     *
     * @param dingId DING消息ID
     * @return 操作结果
     */
    @Operation(summary = "取消DING消息", description = "取消DING消息")
    @PutMapping("/{dingId}/cancel")
    public Result<Void> cancel(@PathVariable Long dingId) {
        Long userId = SecurityUtils.getLoginUserId();
        dingService.cancelDing(dingId, userId);
        return Result.success("DING消息已取消");
    }

    /**
     * 获取未读DING消息数量
     *
     * @return 未读数量
     */
    @Operation(summary = "获取未读DING数量", description = "获取当前用户未读DING消息数量")
    @GetMapping("/unread/count")
    public Result<Integer> getUnreadCount() {
        Long userId = SecurityUtils.getLoginUserId();
        int count = dingService.getUnreadCount(userId);
        return Result.success(count);
    }

    /**
     * 获取DING消息已读状态
     *
     * @param dingId DING消息ID
     * @return 已读用户ID列表
     */
    @Operation(summary = "获取DING已读状态", description = "获取DING消息的已读用户列表")
    @GetMapping("/{dingId}/status")
    public Result<List<Long>> getReadStatus(@PathVariable Long dingId) {
        Long userId = SecurityUtils.getLoginUserId();
        List<Long> readUserIds = dingService.getReadUserIds(dingId, userId);
        return Result.success(readUserIds);
    }
}
