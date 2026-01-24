package com.ruoyi.project.im.controller.im;

import com.ruoyi.framework.aspectj.lang.annotation.Log;
import com.ruoyi.framework.web.controller.BaseController;
import com.ruoyi.framework.web.domain.AjaxResult;
import com.ruoyi.framework.aspectj.lang.enums.BusinessType;
import com.ruoyi.project.im.domain.ImTodoItem;
import com.ruoyi.project.im.service.ImTodoItemService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * IM工作台管理控制器（管理后台）
 *
 * @author ruoyi
 * @date 2025-01-07
 */
@RestController
@RequestMapping("api/admin/im/workbench")
public class ImWorkbenchAdminController extends BaseController {

    @Autowired
    private ImTodoItemService todoItemService;

    /**
     * 获取工作台数据概览
     */
    @RequiresPermissions("im:workbench:overview")
    @GetMapping("/overview")
    public AjaxResult getOverview() {
        java.util.Map<String, Object> overview = new java.util.HashMap<>();

        // 待办数量
        int todoCount = todoItemService.countTodos(null, false);
        overview.put("todoCount", todoCount);

        // 消息数量
        int messageCount = todoItemService.countRecentMessages(null);
        overview.put("messageCount", messageCount);

        // 审批数量
        int approvalCount = todoItemService.countPendingApprovals(null);
        overview.put("approvalCount", approvalCount);

        // 通知数量
        int noticeCount = todoItemService.countUnreadNotices(null);
        overview.put("noticeCount", noticeCount);

        return AjaxResult.success(overview);
    }

    /**
     * 获取待办列表
     */
    @RequiresPermissions("im:workbench:todo")
    @GetMapping("/todos")
    public AjaxResult getTodos(@RequestParam(required = false) Long userId,
                             @RequestParam(required = false, defaultValue = "false") Boolean completed) {
        List<ImTodoItem> todos = todoItemService.getTodosForAdmin(userId, completed);
        return AjaxResult.success(todos);
    }

    /**
     * 创建待办
     */
    @RequiresPermissions("im:workbench:add")
    @Log(title = "创建待办", businessType = BusinessType.INSERT)
    @PostMapping("/todos")
    public AjaxResult createTodo(@RequestParam String title,
                              @RequestParam(required = false) String description,
                              @RequestParam(required = false) String type,
                              @RequestParam(required = false) Long relatedId,
                              @RequestParam(required = false) String relatedType,
                              @RequestParam Long userId) {
        Long todoId = todoItemService.createTodo(title, description, type, relatedId, relatedType, userId);
        return AjaxResult.success("创建成功", todoId);
    }

    /**
     * 完成待办
     */
    @RequiresPermissions("im:workbench:complete")
    @Log(title = "完成待办", businessType = BusinessType.UPDATE)
    @PutMapping("/todos/{id}/complete")
    public AjaxResult completeTodo(@PathVariable Long id) {
        todoItemService.markAsCompleted(id, null);
        return AjaxResult.success("已完成");
    }

    /**
     * 删除待办
     */
    @RequiresPermissions("im:workbench:delete")
    @Log(title = "删除待办", businessType = BusinessType.DELETE)
    @DeleteMapping("/todos/{id}")
    public AjaxResult deleteTodo(@PathVariable Long id) {
        todoItemService.deleteTodo(id, null);
        return AjaxResult.success("删除成功");
    }

    /**
     * 获取近期消息
     */
    @RequiresPermissions("im:workbench:message")
    @GetMapping("/recent-messages")
    public AjaxResult getRecentMessages(@RequestParam(required = false) Long userId,
                                          @RequestParam(defaultValue = "10") Integer limit) {
        List<?> messages = todoItemService.getRecentMessages(userId, limit);
        return AjaxResult.success(messages);
    }

    /**
     * 获取待审批列表
     */
    @RequiresPermissions("im:workbench:approval")
    @GetMapping("/pending-approvals")
    public AjaxResult getPendingApprovals(@RequestParam(required = false) Long userId) {
        List<?> approvals = todoItemService.getPendingApprovals(userId);
        return AjaxResult.success(approvals);
    }

    /**
     * 获取未读通知
     */
    @RequiresPermissions("im:workbench:notice")
    @GetMapping("/unread-notices")
    public AjaxResult getUnreadNotices(@RequestParam(required = false) Long userId) {
        List<?> notices = todoItemService.getUnreadNotices(userId);
        return AjaxResult.success(notices);
    }
}
