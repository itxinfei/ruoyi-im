package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.service.ImSystemNotificationService;
import com.ruoyi.im.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 系统通知控制器
 * 提供系统通知相关接口，包括通知列表查询、已读标记、删除等功能
 *
 * @author ruoyi
 */
@Tag(name = "通知管理", description = "系统通知接口")
@RestController
@RequestMapping("/api/im/notification")
public class ImNotificationController {

    private final ImSystemNotificationService notificationService;

    /**
     * 构造器注入依赖
     *
     * @param notificationService 通知服务
     */
    public ImNotificationController(ImSystemNotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * 获取通知列表
     * 查询用户的通知列表，支持按类型筛选
     *
     * @param type 通知类型筛选（可选），SYSTEM/APPROVAL/MESSAGE/REMINDER
     * @return 通知列表
     */
    @Operation(summary = "获取通知列表", description = "查询用户的通知列表，支持按类型筛选")
    @GetMapping("/list")
    public Result<List<com.ruoyi.im.domain.ImSystemNotification>> getNotifications(
            @RequestParam(required = false) String type) {
        Long userId = SecurityUtils.getLoginUserId();
        List<com.ruoyi.im.domain.ImSystemNotification> list = notificationService.getUserNotifications(userId, type);
        return Result.success(list);
    }

    /**
     * 获取通知详情
     * 查询指定通知的详细信息
     *
     * @param id 通知ID
     * @return 通知详情
     */
    @Operation(summary = "获取通知详情", description = "查询指定通知的详细信息")
    @GetMapping("/{id}")
    public Result<com.ruoyi.im.domain.ImSystemNotification> getNotificationById(
            @PathVariable Long id) {
        Long userId = SecurityUtils.getLoginUserId();
        com.ruoyi.im.domain.ImSystemNotification notification = notificationService.getNotificationById(id);
        return Result.success(notification);
    }

    /**
     * 获取未读通知数量
     * 统计当前用户的未读通知数量
     *
     * @return 未读通知数量
     */
    @Operation(summary = "获取未读通知数量", description = "统计当前用户的未读通知数量")
    @GetMapping("/unread/count")
    public Result<Integer> getUnreadCount() {
        Long userId = SecurityUtils.getLoginUserId();
        Integer count = notificationService.getUnreadCount(userId);
        return Result.success(count);
    }

    /**
     * 标记通知为已读
     * 将指定通知标记为已读
     *
     * @param id 通知ID
     * @return 操作结果
     */
    @Operation(summary = "标记通知为已读", description = "将指定通知标记为已读")
    @PutMapping("/{id}/read")
    public Result<Void> markAsRead(
            @PathVariable Long id) {
        Long userId = SecurityUtils.getLoginUserId();
        notificationService.markAsRead(id, userId);
        return Result.success("标记已读成功");
    }

    /**
     * 标记所有通知为已读
     * 将当前用户的所有未读通知标记为已读
     *
     * @return 操作结果
     */
    @Operation(summary = "标记所有通知为已读", description = "将当前用户的所有未读通知标记为已读")
    @PutMapping("/read/all")
    public Result<Void> markAllAsRead() {
        Long userId = SecurityUtils.getLoginUserId();
        notificationService.markAllAsRead(userId);
        return Result.success("全部标记已读成功");
    }

    /**
     * 删除通知
     * 删除指定的通知
     *
     * @param id 通知ID
     * @return 操作结果
     */
    @Operation(summary = "删除通知", description = "删除指定的通知")
    @DeleteMapping("/{id}")
    public Result<Void> deleteNotification(
            @PathVariable Long id) {
        Long userId = SecurityUtils.getLoginUserId();
        notificationService.deleteNotification(id, userId);
        return Result.success("删除成功");
    }

    /**
     * 批量删除通知
     * 批量删除多条通知
     *
     * @param ids 通知ID列表
     * @return 操作结果
     */
    @Operation(summary = "批量删除通知", description = "批量删除多条通知")
    @DeleteMapping("/batch")
    public Result<Void> batchDeleteNotifications(
            @RequestBody List<Long> ids) {
        Long userId = SecurityUtils.getLoginUserId();
        for (Long id : ids) {
            notificationService.deleteNotification(id, userId);
        }
        return Result.success("批量删除成功");
    }

    /**
     * 清空所有通知
     * 清空当前用户的所有通知
     *
     * @return 操作结果
     */
    @Operation(summary = "清空所有通知", description = "清空当前用户的所有通知")
    @DeleteMapping("/clear")
    public Result<Void> clearAllNotifications() {
        Long userId = SecurityUtils.getLoginUserId();
        notificationService.clearAllNotifications(userId);
        return Result.success("清空成功");
    }

    /**
     * 发送通知
     * 向指定用户发送通知（管理员或系统调用）
     *
     * @param receiverId 接收者ID
     * @param type 通知类型
     * @param title 通知标题
     * @param content 通知内容
     * @param relatedId 关联ID
     * @param relatedType 关联类型
     * @return 通知ID
     */
    @Operation(summary = "发送通知", description = "向指定用户发送通知")
    @PostMapping("/send")
    public Result<Long> sendNotification(
            @RequestParam Long receiverId,
            @RequestParam String type,
            @RequestParam String title,
            @RequestParam String content,
            @RequestParam(required = false) Long relatedId,
            @RequestParam(required = false) String relatedType) {
        Long notificationId = notificationService.sendNotification(receiverId, type, title, content, relatedId, relatedType);
        return Result.success("发送成功", notificationId);
    }
}
