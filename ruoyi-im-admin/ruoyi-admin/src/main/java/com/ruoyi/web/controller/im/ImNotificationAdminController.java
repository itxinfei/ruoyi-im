package com.ruoyi.web.controller.im;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.im.domain.ImSystemNotification;
import com.ruoyi.im.service.ImSystemNotificationService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * IM通知管理控制器
 * 
 * @author ruoyi
 */
@RestController("adminImNotificationController")
@RequestMapping("/api/admin/im/notification")
public class ImNotificationAdminController extends BaseController {

    @Autowired
    private ImSystemNotificationService notificationService;

    /**
     * 获取通知列表
     */
    @RequiresPermissions("im:notification:list")
    @GetMapping("/list")
    public AjaxResult getNotifications(@RequestParam(required = false) String type,
                                      @RequestParam(required = false) Long receiverId) {
        List<ImSystemNotification> list = notificationService.getNotificationsForAdmin(type, receiverId);
        return AjaxResult.success(list);
    }

    /**
     * 获取未读通知数量（针对特定用户）
     */
    @RequiresPermissions("im:notification:query")
    @GetMapping("/unread-count")
    public AjaxResult getUnreadCount(@RequestParam(required = false) Long receiverId) {
        int count = notificationService.getUnreadCountForAdmin(receiverId);
        return AjaxResult.success(count);
    }

    /**
     * 标记通知为已读
     */
    @RequiresPermissions("im:notification:edit")
    @PutMapping("/{id}/read")
    public AjaxResult markAsRead(@PathVariable Long id) {
        notificationService.markAsReadForAdmin(id);
        return AjaxResult.success("已标记为已读");
    }

    /**
     * 标记所有通知为已读
     */
    @RequiresPermissions("im:notification:edit")
    @PutMapping("/read-all")
    public AjaxResult markAllAsRead(@RequestParam(required = false) Long receiverId) {
        notificationService.markAllAsReadForAdmin(receiverId);
        return AjaxResult.success("已全部标记为已读");
    }

    /**
     * 删除通知
     */
    @RequiresPermissions("im:notification:remove")
    @DeleteMapping("/{id}")
    public AjaxResult deleteNotification(@PathVariable Long id) {
        notificationService.deleteNotificationForAdmin(id);
        return AjaxResult.success("删除成功");
    }

    /**
     * 批量删除通知
     */
    @RequiresPermissions("im:notification:remove")
    @DeleteMapping("/batch")
    public AjaxResult batchDeleteNotifications(@RequestParam Long[] ids) {
        for (Long id : ids) {
            notificationService.deleteNotificationForAdmin(id);
        }
        return AjaxResult.success("批量删除成功");
    }

    /**
     * 发送系统通知
     */
    @RequiresPermissions("im:notification:send")
    @PostMapping("/send")
    public AjaxResult sendNotification(@RequestParam Long receiverId,
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
    @PostMapping("/send-batch")
    public AjaxResult sendBatchNotification(@RequestParam String receiverIds, // 逗号分隔的用户ID
                                           @RequestParam String type,
                                           @RequestParam String title,
                                           @RequestParam String content,
                                           @RequestParam(required = false) Long relatedId,
                                           @RequestParam(required = false) String relatedType) {
        String[] ids = receiverIds.split(",");
        for (String idStr : ids) {
            Long receiverId = Long.parseLong(idStr.trim());
            notificationService.sendNotification(receiverId, type, title, content, relatedId, relatedType);
        }
        return AjaxResult.success("批量发送成功");
    }
}