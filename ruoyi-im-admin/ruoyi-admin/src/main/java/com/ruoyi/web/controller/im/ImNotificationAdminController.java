package com.ruoyi.web.controller.im;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.im.domain.ImSystemNotification;
import com.ruoyi.web.service.ImSystemNotificationService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

/**
 * IM通知管理控制器（管理后台）
 *
 * @author ruoyi
 * @date 2025-01-07
 */
@RestController
@RequestMapping("api/admin/im/notification")
public class ImNotificationAdminController extends BaseController {

    @Autowired
    private ImSystemNotificationService notificationService;

    /**
     * 获取所有通知列表（管理员可查看所有通知）
     */
    @RequiresPermissions("im:notification:list")
    @GetMapping("/list")
    public AjaxResult list(ImSystemNotification notification) {
        startPage();
        List<ImSystemNotification> list = notificationService.selectNotificationList(notification);
        return AjaxResult.success(list);
    }

    /**
     * 获取指定用户的通知列表
     */
    @RequiresPermissions("im:notification:list")
    @GetMapping("/user/{userId}")
    public AjaxResult getUserNotifications(@PathVariable Long userId,
                                       @RequestParam(required = false) String type) {
        List<ImSystemNotification> list = notificationService.getUserNotifications(userId, type);
        return AjaxResult.success(list);
    }

    /**
     * 获取通知详细信息
     */
    @RequiresPermissions("im:notification:query")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        ImSystemNotification notification = notificationService.getNotificationById(id);
        return AjaxResult.success(notification);
    }

    /**
     * 获取用户未读通知数量
     */
    @RequiresPermissions("im:notification:query")
    @GetMapping("/unread-count/{userId}")
    public AjaxResult getUnreadCount(@PathVariable Long userId) {
        int count = notificationService.getUnreadCount(userId);
        return AjaxResult.success(count);
    }

    /**
     * 发送系统通知
     */
    @RequiresPermissions("im:notification:send")
    @Log(title = "发送系统通知", businessType = BusinessType.INSERT)
    @PostMapping("/send")
    public AjaxResult send(@RequestParam Long receiverId,
                          @RequestParam String type,
                          @RequestParam String title,
                          @RequestParam String content,
                          @RequestParam(required = false) Long relatedId,
                          @RequestParam(required = false) String relatedType) {
        Long notificationId = notificationService.sendNotification(receiverId, type, title, content, relatedId, relatedType);
        return AjaxResult.success("发送成功", notificationId);
    }

    /**
     * 批量发送系统通知
     */
    @RequiresPermissions("im:notification:send")
    @Log(title = "批量发送系统通知", businessType = BusinessType.INSERT)
    @PostMapping("/send-batch")
    public AjaxResult sendBatch(@RequestParam String receiverIds,
                              @RequestParam String type,
                              @RequestParam String title,
                              @RequestParam String content,
                              @RequestParam(required = false) Long relatedId,
                              @RequestParam(required = false) String relatedType) {
        String[] idArray = receiverIds.split(",");
        List<Long> ids = new java.util.ArrayList<>();
        for (String idStr : idArray) {
            try {
                ids.add(Long.parseLong(idStr.trim()));
            } catch (NumberFormatException e) {
                continue;
            }
        }
        notificationService.batchSendNotification(ids, type, title, content);
        return AjaxResult.success("批量发送成功，共发送" + ids.size() + "条");
    }

    /**
     * 标记通知为已读
     */
    @RequiresPermissions("im:notification:edit")
    @Log(title = "标记通知已读", businessType = BusinessType.UPDATE)
    @PutMapping("/{id}/read")
    public AjaxResult markAsRead(@PathVariable Long id,
                               @RequestParam Long userId) {
        notificationService.markAsRead(id, userId);
        return AjaxResult.success("已标记为已读");
    }

    /**
     * 标记用户所有通知为已读
     */
    @RequiresPermissions("im:notification:edit")
    @Log(title = "标记全部已读", businessType = BusinessType.UPDATE)
    @PutMapping("/read-all/{userId}")
    public AjaxResult markAllAsRead(@PathVariable Long userId) {
        notificationService.markAllAsRead(userId);
        return AjaxResult.success("已全部标记为已读");
    }

    /**
     * 删除通知
     */
    @RequiresPermissions("im:notification:remove")
    @Log(title = "删除通知", businessType = BusinessType.DELETE)
    @DeleteMapping("/{id}")
    public AjaxResult remove(@PathVariable Long id) {
        notificationService.deleteNotification(id, null);
        return AjaxResult.success("删除成功");
    }

    /**
     * 批量删除通知
     */
    @RequiresPermissions("im:notification:remove")
    @Log(title = "批量删除通知", businessType = BusinessType.DELETE)
    @DeleteMapping("/batch/{ids}")
    public AjaxResult removeBatch(@PathVariable Long[] ids) {
        for (Long id : ids) {
            notificationService.deleteNotification(id, null);
        }
        return AjaxResult.success("批量删除成功，共删除" + ids.length + "条");
    }

    /**
     * 清空指定用户所有通知
     */
    @RequiresPermissions("im:notification:remove")
    @Log(title = "清空用户通知", businessType = BusinessType.DELETE)
    @DeleteMapping("/clear/{userId}")
    public AjaxResult clearAll(@PathVariable Long userId) {
        notificationService.clearAllNotifications(userId);
        return AjaxResult.success("已清空该用户的所有通知");
    }
}
