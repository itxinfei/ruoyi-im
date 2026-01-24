package com.ruoyi.project.im.controller.im;

import com.ruoyi.framework.web.controller.BaseController;
import com.ruoyi.framework.web.domain.AjaxResult;
import com.ruoyi.project.im.service.ImMessagePushService;
import com.ruoyi.project.im.service.ImUserService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * IM系统管理控制器
 *
 * @author ruoyi
 * @date 2025-01-18
 */
@RestController("adminImSystemController")
@RequestMapping("/api/admin/im/system")
public class ImSystemAdminController extends BaseController {

    @Autowired
    private ImMessagePushService messagePushService;

    @Autowired
    private ImUserService userService;

    /**
     * 获取系统统计信息
     */
    @RequiresPermissions("im:system:stats")
    @GetMapping("/stats")
    public AjaxResult getSystemStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("onlineUserCount", messagePushService.getOnlineUserCount());
        stats.put("timestamp", System.currentTimeMillis());
        return AjaxResult.success(stats);
    }

    /**
     * 获取在线用户列表
     */
    @RequiresPermissions("im:system:online")
    @GetMapping("/online-users")
    public AjaxResult getOnlineUsers() {
        Set<Long> onlineUsers = messagePushService.getOnlineUserIds();
        return AjaxResult.success(onlineUsers);
    }

    /**
     * 检查用户在线状态
     */
    @RequiresPermissions("im:system:check")
    @GetMapping("/online-check/{userId}")
    public AjaxResult checkOnline(@PathVariable Long userId) {
        boolean online = messagePushService.isUserOnline(userId);
        return AjaxResult.success(online);
    }

    /**
     * 断开用户连接
     */
    @RequiresPermissions("im:system:disconnect")
    @PostMapping("/disconnect/{userId}")
    public AjaxResult disconnectUser(@PathVariable Long userId) {
        messagePushService.disconnectUser(userId);
        return AjaxResult.success("用户已断开");
    }

    /**
     * 发送系统通知给指定用户
     */
    @RequiresPermissions("im:system:notify")
    @PostMapping("/notify/user")
    public AjaxResult notifyUser(
            @RequestParam Long userId,
            @RequestParam String title,
            @RequestParam String content,
            @RequestParam(defaultValue = "info") String type) {
        messagePushService.sendMessageToUser(userId, content);
        return AjaxResult.success("通知已发送");
    }

    /**
     * 广播系统通知给所有用户
     */
    @RequiresPermissions("im:system:broadcast")
    @PostMapping("/notify/all")
    public AjaxResult notifyAllUsers(
            @RequestParam String title,
            @RequestParam String content,
            @RequestParam(defaultValue = "info") String type) {
        messagePushService.broadcastMessage(content);
        return AjaxResult.success("通知已广播");
    }
}
