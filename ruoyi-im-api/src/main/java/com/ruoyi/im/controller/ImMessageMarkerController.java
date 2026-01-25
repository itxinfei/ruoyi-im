package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.service.ImMessageMarkerService;
import com.ruoyi.im.util.SecurityUtils;
import com.ruoyi.im.vo.marker.ImMessageMarkerVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 消息标记控制器
 * 提供消息标记、待办提醒等功能
 *
 * @author ruoyi
 */
@Tag(name = "消息标记", description = "消息标记管理接口")
@RestController
@RequestMapping("/api/im/message/marker")
public class ImMessageMarkerController {

    private static final Logger log = LoggerFactory.getLogger(ImMessageMarkerController.class);

    @Autowired
    private ImMessageMarkerService messageMarkerService;

    /**
     * 标记消息
     */
    @Operation(summary = "标记消息", description = "标记消息（FLAG标记/IMPORTANT重要）")
    @PostMapping("/mark")
    public Result<Long> markMessage(
            @RequestParam Long messageId,
            @RequestParam(defaultValue = "FLAG") String markerType,
            @RequestParam(required = false) String color) {
        Long userId = SecurityUtils.getLoginUserId();

        try {
            Long markerId = messageMarkerService.markMessage(messageId, markerType, color, userId);
            return Result.success("标记成功", markerId);
        } catch (Exception e) {
            log.error("标记消息失败: messageId={}, userId={}", messageId, userId, e);
            return Result.fail("标记失败: " + e.getMessage());
        }
    }

    /**
     * 取消标记
     */
    @Operation(summary = "取消标记", description = "取消消息标记")
    @DeleteMapping("/unmark")
    public Result<Void> unmarkMessage(
            @RequestParam Long messageId,
            @RequestParam(required = false) String markerType) {
        Long userId = SecurityUtils.getLoginUserId();

        try {
            messageMarkerService.unmarkMessage(messageId, markerType, userId);
            return Result.success("取消成功");
        } catch (Exception e) {
            log.error("取消标记失败: messageId={}, userId={}", messageId, userId, e);
            return Result.fail("取消失败: " + e.getMessage());
        }
    }

    /**
     * 设置待办提醒
     */
    @Operation(summary = "设置待办", description = "设置消息为待办并设置提醒时间")
    @PostMapping("/todo")
    public Result<Long> setTodoReminder(
            @RequestParam Long messageId,
            @RequestParam LocalDateTime remindTime,
            @RequestParam(required = false) String remark) {
        Long userId = SecurityUtils.getLoginUserId();

        try {
            Long markerId = messageMarkerService.setTodoReminder(messageId, remindTime, remark, userId);
            return Result.success("设置成功", markerId);
        } catch (Exception e) {
            log.error("设置待办失败: messageId={}, userId={}", messageId, userId, e);
            return Result.fail("设置失败: " + e.getMessage());
        }
    }

    /**
     * 完成待办
     */
    @Operation(summary = "完成待办", description = "标记待办为已完成")
    @PostMapping("/todo/{markerId}/complete")
    public Result<Void> completeTodo(
            @PathVariable Long markerId) {
        Long userId = SecurityUtils.getLoginUserId();

        try {
            messageMarkerService.completeTodo(markerId, userId);
            return Result.success("已完成");
        } catch (Exception e) {
            log.error("完成待办失败: markerId={}, userId={}", markerId, userId, e);
            return Result.fail("操作失败: " + e.getMessage());
        }
    }

    /**
     * 重启待办
     */
    @Operation(summary = "重启待办", description = "将已完成的待办重新设为待办")
    @PostMapping("/todo/{markerId}/reopen")
    public Result<Void> reopenTodo(
            @PathVariable Long markerId) {
        Long userId = SecurityUtils.getLoginUserId();

        try {
            messageMarkerService.reopenTodo(markerId, userId);
            return Result.success("已重启");
        } catch (Exception e) {
            log.error("重启待办失败: markerId={}, userId={}", markerId, userId, e);
            return Result.fail("操作失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户的标记列表
     */
    @Operation(summary = "获取标记列表", description = "获取用户的标记/待办列表")
    @GetMapping("/list")
    public Result<List<ImMessageMarkerVO>> getUserMarkers(
            @RequestParam(required = false) String markerType) {
        Long userId = SecurityUtils.getLoginUserId();

        try {
            List<ImMessageMarkerVO> markers = messageMarkerService.getUserMarkers(userId, markerType);
            return Result.success(markers);
        } catch (Exception e) {
            log.error("获取标记列表失败: userId={}", userId, e);
            return Result.fail("获取失败: " + e.getMessage());
        }
    }

    /**
     * 获取消息的标记列表
     */
    @Operation(summary = "获取消息标记", description = "获取消息的所有标记")
    @GetMapping("/message/{messageId}")
    public Result<List<ImMessageMarkerVO>> getMessageMarkers(
            @PathVariable Long messageId) {
        try {
            List<ImMessageMarkerVO> markers = messageMarkerService.getMessageMarkers(messageId);
            return Result.success(markers);
        } catch (Exception e) {
            log.error("获取消息标记失败: messageId={}", messageId, e);
            return Result.fail("获取失败: " + e.getMessage());
        }
    }

    /**
     * 获取待办数量
     */
    @Operation(summary = "待办数量", description = "获取用户待办消息数量")
    @GetMapping("/todo/count")
    public Result<Integer> getUserTodoCount() {
        Long userId = SecurityUtils.getLoginUserId();

        try {
            Integer count = messageMarkerService.getUserTodoCount(userId);
            return Result.success(count);
        } catch (Exception e) {
            log.error("获取待办数量失败: userId={}", userId, e);
            return Result.fail("获取失败: " + e.getMessage());
        }
    }
}
