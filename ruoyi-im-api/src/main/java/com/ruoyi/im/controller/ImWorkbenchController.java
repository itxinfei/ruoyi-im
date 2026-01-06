package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.domain.ImTodoItem;
import com.ruoyi.im.service.ImTodoItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 工作台控制器
 *
 * @author ruoyi
 */
@Tag(name = "工作台管理")
@RestController
@RequestMapping("/api/im/workbench")
public class ImWorkbenchController {

    @Autowired
    private ImTodoItemService todoItemService;

    /**
     * 获取工作台数据概览
     */
    @Operation(summary = "获取工作台数据概览")
    @GetMapping("/overview")
    public Result<Map<String, Object>> getOverview(@RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 1L;
        }
        Map<String, Object> overview = new HashMap<>();
        // 待办数量
        int todoCount = todoItemService.getUncompletedCount(userId);
        overview.put("todoCount", todoCount);
        // TODO: 添加其他统计信息
        overview.put("messageCount", 0);
        overview.put("approvalCount", 0);
        overview.put("noticeCount", 0);
        return Result.success(overview);
    }

    /**
     * 获取待办列表
     */
    @Operation(summary = "获取待办列表")
    @GetMapping("/todos")
    public Result<List<ImTodoItem>> getTodos(@RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 1L;
        }
        List<ImTodoItem> todos = todoItemService.getUserTodos(userId);
        return Result.success(todos);
    }

    /**
     * 创建待办
     */
    @Operation(summary = "创建待办")
    @PostMapping("/todos")
    public Result<Long> createTodo(@RequestParam String title,
                                   @RequestParam(required = false) String description,
                                   @RequestParam(required = false, defaultValue = "TASK") String type,
                                   @RequestParam(required = false) Long relatedId,
                                   @RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 1L;
        }
        Long todoId = todoItemService.createTodo(title, description, type, relatedId, userId);
        return Result.success("创建成功", todoId);
    }

    /**
     * 完成待办
     */
    @Operation(summary = "完成待办")
    @PutMapping("/todos/{id}/complete")
    public Result<Void> completeTodo(@PathVariable Long id,
                                    @RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 1L;
        }
        todoItemService.markAsCompleted(id, userId);
        return Result.success("已完成");
    }

    /**
     * 删除待办
     */
    @Operation(summary = "删除待办")
    @DeleteMapping("/todos/{id}")
    public Result<Void> deleteTodo(@PathVariable Long id,
                                  @RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 1L;
        }
        todoItemService.deleteTodo(id, userId);
        return Result.success("删除成功");
    }

    /**
     * 更新待办
     */
    @Operation(summary = "更新待办")
    @PutMapping("/todos/{id}")
    public Result<Void> updateTodo(@PathVariable Long id,
                                  @RequestParam String title,
                                  @RequestParam(required = false) String description,
                                  @RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 1L;
        }
        todoItemService.updateTodo(id, title, description, userId);
        return Result.success("更新成功");
    }
}
