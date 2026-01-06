package com.ruoyi.web.controller.im;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.im.domain.ImTodoItem;
import com.ruoyi.im.service.ImTodoItemService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * IM工作台管理控制器
 * 
 * @author ruoyi
 */
@RestController("adminImWorkbenchController")
@RequestMapping("/api/admin/im/workbench")
public class ImWorkbenchAdminController extends BaseController {

    @Autowired
    private ImTodoItemService todoItemService;

    /**
     * 获取工作台数据概览
     */
    @RequiresPermissions("im:workbench:overview")
    @GetMapping("/overview")
    public AjaxResult getOverview() {
        Map<String, Object> overview = new HashMap<>();
        // TODO: 添加统计信息
        overview.put("todoCount", 0);
        overview.put("messageCount", 0);
        overview.put("approvalCount", 0);
        overview.put("noticeCount", 0);
        return AjaxResult.success(overview);
    }

    /**
     * 获取待办列表
     */
    @RequiresPermissions("im:workbench:todo")
    @GetMapping("/todos")
    public AjaxResult getTodos() {
        List<ImTodoItem> todos = todoItemService.getAllTodos();
        return AjaxResult.success(todos);
    }

    /**
     * 创建待办
     */
    @RequiresPermissions("im:workbench:add")
    @PostMapping("/todos")
    public AjaxResult createTodo(@RequestParam String title,
                                 @RequestParam(required = false) String description,
                                 @RequestParam(required = false, defaultValue = "TASK") String type,
                                 @RequestParam(required = false) Long relatedId,
                                 @RequestParam Long userId) {
        Long todoId = todoItemService.createTodo(title, description, type, relatedId, userId);
        return AjaxResult.success("创建成功", todoId);
    }

    /**
     * 完成待办
     */
    @RequiresPermissions("im:workbench:complete")
    @PutMapping("/todos/{id}/complete")
    public AjaxResult completeTodo(@PathVariable Long id) {
        todoItemService.markAsCompleted(id, null); // 管理员操作
        return AjaxResult.success("已完成");
    }

    /**
     * 删除待办
     */
    @RequiresPermissions("im:workbench:delete")
    @DeleteMapping("/todos/{id}")
    public AjaxResult deleteTodo(@PathVariable Long id) {
        todoItemService.deleteTodo(id, null); // 管理员操作
        return AjaxResult.success("删除成功");
    }

    /**
     * 更新待办
     */
    @RequiresPermissions("im:workbench:edit")
    @PutMapping("/todos/{id}")
    public AjaxResult updateTodo(@PathVariable Long id,
                                 @RequestParam String title,
                                 @RequestParam(required = false) String description) {
        todoItemService.updateTodo(id, title, description, null); // 管理员操作
        return AjaxResult.success("更新成功");
    }
}