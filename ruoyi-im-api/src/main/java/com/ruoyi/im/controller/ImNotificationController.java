package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.domain.ImSystemNotification;
import com.ruoyi.im.service.ImSystemNotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 通知控制器
 *
 * @author ruoyi
 */
@Tag(name = "通知管理")
@RestController
@RequestMapping("/api/im/notification")
public class ImNotificationController {

    @Autowired
    private ImSystemNotificationService notificationService;

    /**
     * 获取用户通知列表
     */
    @Operation(summary = "获取用户通知列表")
    @GetMapping("/list")
    public Result<List<ImSystemNotification>> getNotifications(
            @RequestParam(required = false) String type,
            @RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 1L;
        }
        List<ImSystemNotification> list = notificationService.getUserNotifications(userId, type);
        return Result.success(list);
    }

    /**
     * 获取未读通知数量
     */
    @Operation(summary = "获取未读通知数量")
    @GetMapping("/unread-count")
    public Result<Integer> getUnreadCount(@RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 1L;
        }
        int count = notificationService.getUnreadCount(userId);
        return Result.success(count);
    }

    /**
     * 标记通知为已读
     */
    @Operation(summary = "标记通知为已读")
    @PutMapping("/{id}/read")
    public Result<Void> markAsRead(@PathVariable Long id,
                                  @RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 1L;
        }
        notificationService.markAsRead(id, userId);
        return Result.success("已标记为已读");
    }

    /**
     * 标记所有通知为已读
     */
    @Operation(summary = "标记所有通知为已读")
    @PutMapping("/read-all")
    public Result<Void> markAllAsRead(@RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 1L;
        }
        notificationService.markAllAsRead(userId);
        return Result.success("已全部标记为已读");
    }

    /**
     * 删除通知
     */
    @Operation(summary = "删除通知")
    @DeleteMapping("/{id}")
    public Result<Void> deleteNotification(@PathVariable Long id,
                                          @RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 1L;
        }
        notificationService.deleteNotification(id, userId);
        return Result.success("删除成功");
    }

    /**
     * 发送通知（管理员）
     */
    @Operation(summary = "发送通知")
    @PostMapping("/send")
    public Result<Long> sendNotification(@RequestParam Long receiverId,
                                        @RequestParam String type,
                                        @RequestParam String title,
                                        @RequestParam String content,
                                        @RequestParam(required = false) Long relatedId,
                                        @RequestParam(required = false) String relatedType) {
        Long notificationId = notificationService.sendNotification(receiverId, type, title, content, relatedId, relatedType);
        return Result.success("发送成功", notificationId);
    }
}
