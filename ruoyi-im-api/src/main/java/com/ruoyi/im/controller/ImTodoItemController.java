package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.domain.ImTodoItem;
import com.ruoyi.im.service.ImTodoItemService;
import com.ruoyi.im.util.SecurityUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 待办事项控制器
 */

@RestController
@RequestMapping("/api/im/todo")
public class ImTodoItemController {

    private final ImTodoItemService todoItemService;

    public ImTodoItemController(ImTodoItemService todoItemService) {
        this.todoItemService = todoItemService;
    }

    /**
     * 获取待办列表
     */
    @GetMapping("/list")
    public Result<List<ImTodoItem>> getList(
            @RequestParam(required = false) String status) {
        Long userId = SecurityUtils.getLoginUserId();
        List<ImTodoItem> todos = todoItemService.getUserTodos(userId);
        // 按状态过滤（如果有）
        if (status != null && !status.isEmpty()) {
            todos = todos.stream()
                    .filter(t -> status.equals(t.getStatus()))
                    .collect(java.util.stream.Collectors.toList());
        }
        return Result.success(todos);
    }

    /**
     * 创建待办
     */
    @PostMapping
    public Result<Long> create(@RequestBody CreateTodoRequest request) {
        Long userId = SecurityUtils.getLoginUserId();
        Long todoId = todoItemService.createTodo(
                request.getTitle(),
                request.getDescription(),
                request.getType(),
                request.getRelatedId(),
                userId
        );
        return Result.success("创建成功", todoId);
    }

    /**
     * 切换待办状态
     */
    @PutMapping("/{id}/toggle")
    public Result<Void> toggleStatus(@PathVariable Long id) {
        Long userId = SecurityUtils.getLoginUserId();
        todoItemService.markAsCompleted(id, userId);
        return Result.success("状态更新成功");
    }

    /**
     * 删除待办
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Long userId = SecurityUtils.getLoginUserId();
        todoItemService.deleteTodo(id, userId);
        return Result.success("删除成功");
    }

    /**
     * 获取未完成数量
     */
    @GetMapping("/uncompleted/count")
    public Result<Integer> getUncompletedCount() {
        Long userId = SecurityUtils.getLoginUserId();
        int count = todoItemService.getUncompletedCount(userId);
        return Result.success(count);
    }

    /**
     * 更新待办
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody UpdateTodoRequest request) {
        Long userId = SecurityUtils.getLoginUserId();
        todoItemService.updateTodo(id, request.getTitle(), request.getDescription(), userId);
        return Result.success("更新成功");
    }

    /**
     * 请求体：创建待办
     */
    public static class CreateTodoRequest {
        private String title;
        private String description;
        private String type;
        private Long relatedId;

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public Long getRelatedId() { return relatedId; }
        public void setRelatedId(Long relatedId) { this.relatedId = relatedId; }
    }

    /**
     * 请求体：更新待办
     */
    public static class UpdateTodoRequest {
        private String title;
        private String description;

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }
}
