package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.domain.ImSystemNotification;
import com.ruoyi.im.service.ImSystemNotificationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 通知控制器
 *
 * @author ruoyi
 */
@Api(tags = "通知管理")
@RestController
@RequestMapping("/api/im/notification")
public class ImNotificationController {

    @Autowired
    private ImSystemNotificationService notificationService;

    /**
     * 获取用户通知列表
     */
    @ApiOperation("获取用户通知列表")
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
    @ApiOperation("获取未读通知数量")
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
    @ApiOperation("标记通知为已读")
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
    @ApiOperation("标记所有通知为已读")
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
    @ApiOperation("删除通知")
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
    @ApiOperation("发送通知")
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
