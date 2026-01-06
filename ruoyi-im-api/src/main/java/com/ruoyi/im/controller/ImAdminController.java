package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.service.ImMessagePushService;
import com.ruoyi.im.service.ImUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/im/admin")
public class ImAdminController {

    @Autowired
    private ImMessagePushService messagePushService;

    @Autowired
    private ImUserService userService;

    @GetMapping("/stats")
    public Result<Map<String, Object>> getSystemStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("onlineUserCount", messagePushService.getOnlineUserCount());
        stats.put("totalUsers", userService.getTotalUserCount());
        stats.put("timestamp", System.currentTimeMillis());
        return Result.success(stats);
    }

    @GetMapping("/online-users")
    public Result<Set<Long>> getOnlineUsers() {
        Set<Long> onlineUsers = messagePushService.getOnlineUserIds();
        return Result.success(onlineUsers);
    }

    @GetMapping("/online-check/{userId}")
    public Result<Boolean> checkOnline(@PathVariable Long userId) {
        boolean online = messagePushService.isUserOnline(userId);
        return Result.success(online);
    }

    @PostMapping("/disconnect/{userId}")
    public Result<Void> disconnectUser(@PathVariable Long userId) {
        messagePushService.disconnectUser(userId);
        return Result.success("User disconnected");
    }

    @PostMapping("/notify/user")
    public Result<Void> notifyUser(
            @RequestParam Long userId,
            @RequestParam String title,
            @RequestParam String content,
            @RequestParam(defaultValue = "info") String type) {
        messagePushService.sendSystemNotification(userId, title, content, type);
        return Result.success("Notification sent");
    }

    @PostMapping("/notify/all")
    public Result<Void> notifyAllUsers(
            @RequestParam String title,
            @RequestParam String content,
            @RequestParam(defaultValue = "info") String type) {
        messagePushService.broadcastSystemNotification(title, content, type);
        return Result.success("Notification broadcasted");
    }
}
